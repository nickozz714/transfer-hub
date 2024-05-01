package com.definefunction.transfer.factory.processor;

import com.definefunction.transfer.factory.processor.model.ProcessingType;
import com.definefunction.transfer.model.Endpoint;
import com.definefunction.transfer.model.InProgressRecord;
import com.definefunction.transfer.model.ProgressEventsRecord;
import com.definefunction.transfer.model.TransferRecord;
import com.definefunction.transfer.model.pojo.Direction;
import com.definefunction.transfer.model.pojo.ProgressType;
import com.definefunction.transfer.model.pojo.Status;
import com.definefunction.transfer.model.pojo.TransferLogging;
import com.definefunction.transfer.service.InProgressService;
import com.definefunction.transfer.service.ProgressEventService;
import com.definefunction.transfer.service.RecordService;
import com.definefunction.transfer.service.URLBuilderService;
import org.apache.camel.Exchange;
import org.apache.camel.component.azure.storage.blob.BlobConstants;
import org.apache.camel.component.file.GenericFileOperationFailedException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static com.definefunction.transfer.utilities.Utilities.getRouteName;

public abstract class AbstractProcessorEvents {

    protected final InProgressService inProgressService;

    protected final RecordService recordService;

    protected final ProgressEventService progressEventService;

    protected final URLBuilderService urlBuilderService;

    protected final List<TransferLogging> transferLogging;

    protected static final String BLOB_COMPONENT = "azure-storage-blob";

    public AbstractProcessorEvents(InProgressService inProgressService, RecordService recordService, ProgressEventService progressEventService, URLBuilderService urlBuilderService, List<TransferLogging> transferLogging) {
        this.inProgressService = inProgressService;
        this.recordService = recordService;
        this.progressEventService = progressEventService;
        this.urlBuilderService = urlBuilderService;
        this.transferLogging = transferLogging;
    }

    Endpoint getEndpointByIdAndDirection(String routeId, Direction direction) {
        TransferRecord transferRecord = recordService.getRecordById(routeId).get();
        return transferRecord.getEndpoints().stream().filter(endpoint -> endpoint.getDirection() == direction).findFirst().orElseThrow();
    }

    protected String handleError(Exchange exchange, String routeId, String from, String to) {
        String exceptionString = constructException(exchange);
        handleInProgressRecord(exchange, false);
        handleErrorEvent(exchange, routeId, exceptionString, from, to);
        recordService.updateStatusById(Status.ERROR, routeId);
        return exceptionString;
    }

    String constructException(Exchange exchange) {
        try {
            Exception exception = ((GenericFileOperationFailedException) exchange.getAllProperties().get("CamelExceptionCaught"));
            return exception.getMessage() + ": " + exception.getCause();
        } catch (Exception e) {
            return "No clear exception found. Please make sure to check the credentials, permissions, files and structure of the transfer.";
        }
    }

    String constructBreadCrumb(Exchange exchange) {
        try {
            return exchange.getIn().getHeader("BreadCrumb").toString();
        } catch (Exception e) {
            String breadCrumb = createBreadCrumb(exchange.getFromRouteId());
            exchange.getIn().setHeader("BreadCrumb", breadCrumb);
            return breadCrumb;
        }
    }

    String constructAbsoluteFileName(Exchange exchange) {
        try {
            Map<String, Object> headers = exchange.getIn().getHeaders();
            if (headers.containsKey("CamelFileAbsolutePath")) {
                return headers.get("CamelFileAbsolutePath").toString();
            } else if (headers.containsKey("CamelAzureStorageBlobFileName")) {
                return headers.get("CamelAzureStorageBlobFileName").toString();
            } else if (headers.containsKey("CamelAzureStorageBlobBlobName")) {
                return headers.get("CamelAzureStorageBlobBlobName").toString();
            } else {
                throw new Exception("Headers for Blobname not found.");
            }
        } catch (Exception e) {
            return "Filename not found";
        }
    }

    protected void handleProgressEvent(Exchange exchange, String routeId, ProgressType progressType, String from, String to) {
        String fileName = constructAbsoluteFileName(exchange);
        String breadCrumb = constructBreadCrumb(exchange);
        progressEventService.saveProgressEventRecord(new ProgressEventsRecord(breadCrumb, routeId, fileName, progressType, from, to));
    }

    protected void handleErrorEvent(Exchange exchange, String routeId, String exception, String from, String to) {
        String fileName = constructAbsoluteFileName(exchange);
        String breadCrumb = constructBreadCrumb(exchange);
        ProgressEventsRecord progressEventsRecord = new ProgressEventsRecord(breadCrumb, routeId, fileName, ProgressType.FAILED, from, to);
        progressEventsRecord.setException_message(exception);
        progressEventService.saveProgressEventRecord(progressEventsRecord);
    }

    void handleInProgressRecord(Exchange exchange, boolean insert) {
        String transfer = exchange.getFromRouteId();
        String file = constructAbsoluteFileName(exchange);
        if (insert) {
            inProgressService.insert(new InProgressRecord(transfer, file, ZonedDateTime.now()));
        } else {
            inProgressService.delete(transfer, file);
        }
    }

    protected void fileInProgress(Exchange exchange) {
        String routeId = exchange.getFromRouteId();
        String fileName = constructAbsoluteFileName(exchange);
        boolean fileIsInProgress = inProgressService.isInProgress(routeId, fileName);
        exchange.getIn().setHeader("fileIsInProcess", fileIsInProgress);
    }

    protected String createBreadCrumb(String routeId) {
        Random rand = new Random();
        return "case-" + routeId + "-" + rand.nextInt(10000);
    }

    protected void setBlobHeaders(Exchange exchange) throws Exception {
        Map<String, Object> headers = exchange.getIn().getHeaders();
        if (headers.containsKey("CamelFileName")) {
            String fileName = headers.get("CamelFileName").toString();
            exchange.getIn().setHeader(BlobConstants.BLOB_NAME, fileName);
        } else if (headers.containsKey("CamelAzureStorageBlobFileName")) {
            String fileName = headers.get("CamelAzureStorageBlobFileName").toString();
            exchange.getIn().setHeader(BlobConstants.BLOB_NAME, fileName);
        } else if (headers.containsKey("CamelAzureStorageBlobBlobName")) {
            String fileName = headers.get("CamelAzureStorageBlobBlobName").toString();
            exchange.getIn().setHeader(BlobConstants.BLOB_NAME, fileName);
            exchange.getIn().setHeader("CamelFileName", fileName);
        } else {
            throw new Exception("Headers for Blobname not found.");
        }
    }

    /**
     * Because multiple operations are needed for post-processing, multiple headers need to define the BlobName.
     * When moving the Blob to an archive, the container is set, however a virtual directory might be used.
     * When deleting, the Blob must be the one on the original Container and name.
     *
     * @param exchange       is needed to retrieve and set headers for further processing in the route.
     * @param processingType is needed to define the kind of post-processing
     * @throws Exception if headers are not found, something is wrong and should throw an exception.
     */
    protected void setPostProcessingBlobHeaders(Exchange exchange, ProcessingType processingType) throws Exception {
        Map<String, Object> headers = exchange.getIn().getHeaders();
        if (headers.containsKey("CamelAzureStorageBlobBlobName")) {
            String fileName = headers.get("CamelAzureStorageBlobBlobName").toString();
            switch (processingType) {
                case MOVE:
                    // We need to add the virtual directory to be used as a directory in the Blob storage.
                    // Setting the original filename as header, to keep it for the delete operation.
                    exchange.getIn().setHeader("CamelAzureBlobStorageSourceBlob", fileName);
                    Endpoint from = getEndpointByIdAndDirection(exchange.getFromRouteId(), Direction.FROM);
                    Map<String, String> parameters = urlBuilderService.returnMapping(from.getParameter());
                    String moveParameter = parameters.get("move");
                    String fileString = moveParameter.contains("/") ? moveParameter.split("/", 2)[1] : moveParameter;// The first one is a container name, not needed.
                    exchange.getIn().setHeader(BlobConstants.BLOB_NAME, fileString + "/" + fileName);
                    break;
                case DELETE:
                    // When using a delete, we either need the original filename when moved,
                    // Or we need the filename that was in the headers when not moved.
                    String file = exchange.getIn().getHeaders().containsKey("CamelAzureBlobStorageSourceBlob") ?
                            exchange.getIn().getHeaders().get("CamelAzureBlobStorageSourceBlob").toString() :
                            fileName;
                    exchange.getIn().setHeader(BlobConstants.BLOB_NAME, file);
                    break;
            }
        } else {
            throw new Exception("Headers for Blobname not found.");
        }
    }

    public String getFromEndpointUrl(String routeId) {
        TransferLogging log = transferLogging.stream().filter(e -> Objects.equals(e.getTransferId(), getRouteName(routeId))).findFirst().orElseThrow();
        return log.getParsedEndpointUrlList().get(0);
    }

    public String getToEndpointUrl(String routeId) {
        TransferLogging log = transferLogging.stream().filter(e -> Objects.equals(e.getTransferId(), getRouteName(routeId))).findFirst().orElseThrow();
        return log.getParsedEndpointUrlList().get(1);
    }

    public String createEndpointLog(String routeId) {
        StringBuilder stringBuilder = new StringBuilder();
        TransferLogging log = transferLogging.stream().filter(e -> Objects.equals(e.getTransferId(), getRouteName(routeId))).findFirst().orElseThrow();
        log.getParsedEndpointUrlList().forEach(endpoint -> {
            stringBuilder.append(endpoint + " | ");
        });
        return stringBuilder.toString();
    }
}
