package com.definefunction.transfer.factory.transfers;

import com.definefunction.transfer.factory.endpoint.DynamicResolver;
import com.definefunction.transfer.factory.processor.*;
import com.definefunction.transfer.model.Endpoint;
import com.definefunction.transfer.model.ProgressEventsRecord;
import com.definefunction.transfer.model.TransferRecord;
import com.definefunction.transfer.model.pojo.*;
import com.definefunction.transfer.service.InProgressService;
import com.definefunction.transfer.service.ProgressEventService;
import com.definefunction.transfer.service.RecordService;
import com.definefunction.transfer.service.URLBuilderService;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.OnExceptionDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.UnaryOperator;

import static com.definefunction.transfer.utilities.Utilities.getRouteContextName;
import static org.apache.camel.builder.Builder.constant;
import static org.apache.camel.builder.Builder.header;

public abstract class AbstractRouteFactory {

    private final Logger logger = LoggerFactory.getLogger(AbstractRouteFactory.class);

    private final DynamicResolver DYNAMIC_RESOLVER;
    final URLBuilderService urlBuilderService;

    protected CamelContext camelContext;

    protected ErrorProcessor ERROR_PROCESSOR;

    private final ProcessingProcesser PROCESSING_PROCESSOR;

    private final ProcessCompleteProcessor PROCESS_COMPLETE_PROCESSOR;

    private final InitiateTransferProcessor INITIATE_TRANSFER_PROCESSOR;

    protected final PostProcessingMoveProcessor postProcessingMoveProcessor;

    protected final PostProcessingDeleteProcessor postProcessingDeleteProcessor;

    protected final RecordService recordService;

    protected final ProgressEventService progressEventService;

    protected final InProgressService inProgressService;

    protected final List<TransferLogging> transferLogging;

    protected AbstractRouteFactory(DynamicResolver dynamicResolver, URLBuilderService urlBuilderService, CamelContext camelContext, RecordService recordService, ProgressEventService progressEventService, InProgressService inProgressService, List<TransferLogging> transferLogging) {
        this.DYNAMIC_RESOLVER = dynamicResolver;
        this.urlBuilderService = urlBuilderService;
        this.camelContext = camelContext;
        this.transferLogging = transferLogging;
        this.ERROR_PROCESSOR = new ErrorProcessor(inProgressService, recordService, progressEventService, urlBuilderService, transferLogging);
        this.inProgressService = inProgressService;
        this.PROCESSING_PROCESSOR = new ProcessingProcesser(inProgressService, recordService, progressEventService, urlBuilderService, transferLogging);
        this.PROCESS_COMPLETE_PROCESSOR = new ProcessCompleteProcessor(inProgressService, recordService, progressEventService, urlBuilderService, transferLogging);
        this.INITIATE_TRANSFER_PROCESSOR = new InitiateTransferProcessor(inProgressService, recordService, progressEventService, urlBuilderService, transferLogging);
        this.postProcessingDeleteProcessor = new PostProcessingDeleteProcessor(inProgressService, recordService, progressEventService, urlBuilderService, transferLogging);
        this.postProcessingMoveProcessor = new PostProcessingMoveProcessor(inProgressService, recordService, progressEventService, urlBuilderService, transferLogging);
        this.recordService = recordService;
        this.progressEventService = progressEventService;
    }

    public final void addRouteToContext(TransferRecord transferRecord){
        try {
            logger.info("CamelContextEvent | Adding " + transferRecord.getId() + " | Status: started");
            handleCreateEvent(transferRecord);
            RouteBuilder transfer = buildRoute(transferRecord);
            if (transfer != null) {
                camelContext.addRoutes(transfer);
                logger.info("CamelContextEvent | Adding " + transferRecord.getId() + " | Status: Finished | Exception: None");
            } else {
                throw new NullPointerException("Transfer is null or empty");
            }
        } catch (Exception e) {
            logger.info("CamelContextEvent | Adding " + transferRecord.getId() + " | Updating Transfer to FAILURE | Status: Finished | Exception: True | Failed because: " + e.getMessage() + " \n \n Stacktrace: \n" +
                    Arrays.toString(e.getStackTrace()));
            handleFailure(transferRecord.getId(), e.getMessage());
        }
    }

    ProcessorDefinition<?> createExceptionHandler(TransferRecord transferRecord, ProcessorDefinition processorDefinition, Class<? extends Throwable> exception, boolean handled, UnaryOperator<OnExceptionDefinition> exceptionDefinitionUnaryOperator){
        OnExceptionDefinition onExceptionDefinition = processorDefinition.onException(exception)
                .handled(handled)
                .setProperty("TransferRecord", constant(transferRecord));
        exceptionDefinitionUnaryOperator.apply(onExceptionDefinition);
        return onExceptionDefinition.end();
    }

    UnaryOperator<ProcessorDefinition<?>> addExceptionHandler(TransferRecord transferRecord) {
        return processorDefinition -> createExceptionHandler(transferRecord, processorDefinition, Exception.class, true, onExceptionDefinition ->
                onExceptionDefinition
                        .logRetryAttempted(true)
                        .maximumRedeliveries(3)
                        .maximumRedeliveryDelay(3000)
                        .useExponentialBackOff()
                        .backOffMultiplier(2)
                        .retryAttemptedLogLevel(org.apache.camel.LoggingLevel.ERROR)
                        .process(ERROR_PROCESSOR));
    }

    ProcessorDefinition<?> createRoute(RouteBuilder routeBuilder, TransferRecord transferRecord, ParsedTransferEndpoint parsedTransferFromEndpoint, UnaryOperator<ProcessorDefinition<?>> exceptionHandler) throws Exception {
        return DYNAMIC_RESOLVER.getResolver(parsedTransferFromEndpoint.getEndpointURL())
                .from(routeBuilder, parsedTransferFromEndpoint, exceptionHandler, processorDefinition -> processorDefinition.setProperty("TransferRecord", constant(transferRecord)))
                .routeId(getRouteContextName(transferRecord))
                .process(INITIATE_TRANSFER_PROCESSOR);
    }

    public void defineRouteBehaviourAndAddEndPoints(RouteBuilder routeBuilder, ProcessorDefinition<?> transfer, ParsedTransferEndpoint... toEndpoints) throws Exception {
        transfer.choice()
                .when(header("fileIsInProcess").isEqualTo(false))
                .to("direct:file-in-progress")
                .otherwise()
                .process(PROCESSING_PROCESSOR);

        DYNAMIC_RESOLVER.getResolver(Arrays.stream(toEndpoints)
                        .map(ParsedTransferEndpoint::getEndpointURL)
                        .toArray(String[]::new))
                .to(routeBuilder, transfer, toEndpoints)
                .process(PROCESS_COMPLETE_PROCESSOR);
    }

    public void addOperationalDefaultToEndpoints(RouteBuilder routeBuilder, ProcessorDefinition<?> transfer, Processor processor, ParsedTransferEndpoint parsedTransferEndpoint) throws Exception {
        transfer.process(processor);

        DYNAMIC_RESOLVER.getResolver(parsedTransferEndpoint.getEndpointURL())
                .to(routeBuilder, transfer, parsedTransferEndpoint);
    }



    Endpoint getEndpointByIdAndDirection(String routeId, Direction direction) {
        TransferRecord transferRecord = recordService.getRecordById(routeId).get();
        return transferRecord.getEndpoints().stream().filter(endpoint -> endpoint.getDirection() == direction).findFirst().orElseThrow();
    }


    String constructBreadCrumb(Exchange exchange) {
        try {
            return exchange.getIn().getHeader("BreadCrumb").toString();
        } catch (Exception e) {
            String breadCrumb = createBreadCrumb(exchange.getFromRouteId());
            exchange.getIn().setHeader("BreadCrumb",breadCrumb);
            return breadCrumb;
        }
    }

    String constructAbsoluteFileName(Exchange exchange) {
        try {
            return exchange.getIn().getHeader("CamelFileAbsolutePath").toString();
        } catch (Exception e) {
            return "Filename not found";
        }
    }

    protected String createBreadCrumb(String routeId) {
        Random rand = new Random();
        return "case-"+routeId+"-"+rand.nextInt(10000);
    }

    public abstract RouteBuilder buildRoute(TransferRecord transferRecord) throws Exception;

    public void handleCreateEvent(TransferRecord transferRecord) {
        String routeId = transferRecord.getId();
        ProgressEventsRecord progressEventsRecord = new ProgressEventsRecord(createBreadCrumb(routeId), routeId, "No file", ProgressType.ADDING_ROUTE, "To be determined", "To be determined");
        progressEventService.saveProgressEventRecord(progressEventsRecord);
    }

    public abstract void handleEvent(Exchange exchange, String routeId, ProgressType progressType, String from, String to);

    public void handleFailure(String routeId, String exception){
        handleFailedEvent(routeId, exception, null,null);
        recordService.updateStatusById(Status.FAILED,routeId);
    }
    public void handleFailedEvent(String routeId, String exception, String from, String to) {
        ProgressEventsRecord progressEventsRecord = new ProgressEventsRecord(createBreadCrumb(routeId), routeId, "No file", ProgressType.FAILED, from, to);
        progressEventsRecord.setException_message(exception);
        progressEventService.saveProgressEventRecord(progressEventsRecord);
    }
}
