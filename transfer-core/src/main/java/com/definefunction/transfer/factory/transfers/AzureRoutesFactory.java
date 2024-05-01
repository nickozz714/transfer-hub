package com.definefunction.transfer.factory.transfers;

import com.definefunction.transfer.factory.endpoint.DynamicResolver;
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
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Supports creating transfers for the Azure-Blob component. Possible to add multiple endpoints for a one-to-many route.
 * Should not be used for other protocols.
 */
@Component
public class AzureRoutesFactory extends AbstractRouteFactory {

    private static final String PROTOCOL = "azure-storage-blob";

    @Autowired
    public AzureRoutesFactory(DynamicResolver dynamicResolver, URLBuilderService urlBuilderService, CamelContext camelContext, RecordService recordService, ProgressEventService progressEventService, InProgressService inProgressService, List<TransferLogging> transferLogging) {
        super(dynamicResolver, urlBuilderService, camelContext, recordService, progressEventService, inProgressService, transferLogging);
    }

    @Override
    public RouteBuilder buildRoute(TransferRecord transferRecord) throws Exception {

        List<ParsedTransferEndpoint> parsedTransferEndpoints = urlBuilderService.createParsedEndpoint(transferRecord,  camelContext);

        TransferLogging logging = new TransferLogging();
        logging.setTransferId(transferRecord.getId());
        logging.setParsedEndpointUrlList(parsedTransferEndpoints.stream().map(ParsedTransferEndpoint::getEndpointURL).toList());
        transferLogging.add(logging);

        List<ParsedTransferEndpoint> parsedFromEndpoints = parsedTransferEndpoints.stream()
                .filter(parsedTransferEndpoint -> parsedTransferEndpoint.getEndpoint().getDirection() == Direction.FROM)
                .toList();

        ParsedTransferEndpoint[] parsedToEndpoints = parsedTransferEndpoints
                .stream()
                .filter(parsedTransferEndpoint -> parsedTransferEndpoint.getEndpoint().getDirection() == Direction.TO)
                .toArray(ParsedTransferEndpoint[]::new);

        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                ProcessorDefinition<?> definition = createRoute(this, transferRecord, parsedFromEndpoints.get(0), addExceptionHandler(transferRecord));
                defineRouteBehaviourAndAddEndPoints(this, definition, parsedToEndpoints);
                Endpoint fromEndpoint = transferRecord.getEndpoints().stream().filter(endpoint -> endpoint.getDirection() == Direction.FROM).findFirst().orElseThrow();
                if (Objects.equals(fromEndpoint.getProtocol(), PROTOCOL)) {
                    urlBuilderService.createParsedAzureOperationalEndpoints(fromEndpoint, camelContext).forEach(parsedTransferEndpoint -> {
                        try {
                            Processor processor = parsedTransferEndpoint.getEndpointURL().contains("operation=deleteBlob") ? postProcessingDeleteProcessor : postProcessingMoveProcessor;
                            addOperationalDefaultToEndpoints(this, definition, processor, parsedTransferEndpoint);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

                }
            }
        };
    }

    @Override
    public void handleEvent(Exchange exchange, String routeId, ProgressType progressType, String from, String to) {
        return;
    }
}
