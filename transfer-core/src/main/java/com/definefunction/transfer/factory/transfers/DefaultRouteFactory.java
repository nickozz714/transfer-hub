package com.definefunction.transfer.factory.transfers;

import com.definefunction.transfer.factory.endpoint.DynamicResolver;
import com.definefunction.transfer.factory.processor.ErrorProcessor;
import com.definefunction.transfer.factory.processor.InitiateTransferProcessor;
import com.definefunction.transfer.factory.processor.ProcessCompleteProcessor;
import com.definefunction.transfer.factory.processor.ProcessingProcesser;
import com.definefunction.transfer.model.ProgressEventsRecord;
import com.definefunction.transfer.model.TransferRecord;
import com.definefunction.transfer.model.pojo.*;
import com.definefunction.transfer.service.InProgressService;
import com.definefunction.transfer.service.ProgressEventService;
import com.definefunction.transfer.service.RecordService;
import com.definefunction.transfer.service.URLBuilderService;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static com.definefunction.transfer.utilities.Utilities.getRouteName;
import static com.definefunction.transfer.utilities.Utilities.getRouteVersion;
import static org.apache.camel.builder.Builder.constant;

/**
 * Supports creating transfers for the sftp and file component. Possible to add multiple endpoints for a one-to-many route.
 * Should not be used for other protocols.
 */
@Component
public class DefaultRouteFactory extends AbstractRouteFactory {
    private static final Logger logger = LoggerFactory.getLogger(DefaultRouteFactory.class);

    @Autowired
    public DefaultRouteFactory(DynamicResolver dynamicResolver, URLBuilderService urlBuilderService, CamelContext camelContext, RecordService recordService, ProgressEventService progressEventService, InProgressService inProgressService, List<TransferLogging> transferLogging) {
        super(dynamicResolver, urlBuilderService, camelContext, recordService, progressEventService, inProgressService, transferLogging);
    }

    @Override
    public RouteBuilder buildRoute(TransferRecord transferRecord) throws Exception {

        List<ParsedTransferEndpoint> parsedTransferEndpoints = urlBuilderService.createParsedEndpoint(transferRecord, camelContext);
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
            }
        };
    }

    @Override
    public void handleEvent(Exchange exchange, String routeId, ProgressType progressType, String from, String to) {
        return;
    }

    public Map<String, Integer> getActiveRoutes() {
        Map<String, Integer> activeRoutes = new HashMap<>();
        for (Route route: camelContext.getRoutes()){
            if (!Objects.equals(route.getGroup(), "system")) {
                String routeId = route.getRouteId();
                String routeName = getRouteName(routeId);
                int version = getRouteVersion(routeId);
                activeRoutes.put(routeName, version);
            }
        }
        return activeRoutes;
    }

    public void RemoveFromCamelContext(String id) throws Exception {
        String generalId = getRouteName(id);
        progressEventService.saveProgressEventRecord(new ProgressEventsRecord(generalId, ProgressType.TERMINATING_ROUTE));
        List<Route> activeRoutes = camelContext.getRoutes().stream().filter(e -> e.getRouteId().equals(id)).toList();
        if(!activeRoutes.isEmpty()) {
            logger.info("CamelContextService | Terminating route: "+id);
            camelContext.getRouteController().stopRoute(id);
            camelContext.removeRoute(id);
            recordService.removePrivateKeyFileIfNeeded(getRouteName(id));
            progressEventService.saveProgressEventRecord(new ProgressEventsRecord(generalId, ProgressType.COMPLETED));
            progressEventService.saveProgressEventRecord(new ProgressEventsRecord(generalId, ProgressType.CLEANING_IN_PROGRESS));
            inProgressService.deleteAllByTransferRecord(id);
            transferLogging.removeIf(e -> e.getTransferId().equals(id));
        }
    }
}
