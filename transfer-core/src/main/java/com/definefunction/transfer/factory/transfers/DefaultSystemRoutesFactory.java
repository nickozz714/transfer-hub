package com.definefunction.transfer.factory.transfers;

import com.definefunction.transfer.factory.endpoint.DynamicResolver;
import com.definefunction.transfer.model.ProgressEventsRecord;
import com.definefunction.transfer.model.TransferRecord;
import com.definefunction.transfer.model.pojo.Direction;
import com.definefunction.transfer.model.pojo.ProgressType;
import com.definefunction.transfer.model.pojo.TransferLogging;
import com.definefunction.transfer.service.InProgressService;
import com.definefunction.transfer.service.ProgressEventService;
import com.definefunction.transfer.service.RecordService;
import com.definefunction.transfer.service.URLBuilderService;
import com.definefunction.transfer.utilities.Utilities;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class DefaultSystemRoutesFactory extends AbstractRouteFactory {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRouteFactory.class);

    public DefaultSystemRoutesFactory(DynamicResolver dynamicResolver, URLBuilderService urlBuilderService, CamelContext camelContext, RecordService recordService, ProgressEventService progressEventService, InProgressService inProgressService, List<TransferLogging> transferLogging) {
        super(dynamicResolver, urlBuilderService, camelContext, recordService, progressEventService, inProgressService, transferLogging);
    }

    @Override
    public RouteBuilder buildRoute(TransferRecord transferRecord) throws Exception {
        return null;
    }

    @Override
    public void handleCreateEvent(TransferRecord transferRecord) {

    }

    public void AddSystemCamelRoutesToContext() throws Exception {
        logger.info("CamelContextEvent | Creating system route: direct:file-in-progress | Status: started");
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:file-in-progress")
                        .routeGroup("system")
                        .process(exchange -> {
                            String fileName = constructAbsoluteFileName(exchange);
                            logger.warn("CamelContextEvent | File: "+fileName+" | Status: skipping| Exception: Already in progress");
                            String routeId = exchange.getFromRouteId();
                            String from = Utilities.sanitizeParametersInURL(transferLogging.stream().filter(e -> Objects.equals(e.getTransferId(), routeId)).findFirst().orElseThrow().getParsedEndpointUrlList().get(0));
                            String to = Utilities.sanitizeParametersInURL(transferLogging.stream().filter(e -> Objects.equals(e.getTransferId(), routeId)).findFirst().orElseThrow().getParsedEndpointUrlList().get(1));
                            handleEvent(exchange, routeId, ProgressType.ALLREADY_IN_PROGRESS, from, to);
                        });
            }
        });
        logger.info("CamelContextEvent | Creating system route: direct:file-in-progress | Status: finished");
    }

    public void handleEvent(Exchange exchange, String routeId, ProgressType progressType, String from, String to) {
        String fileName = constructAbsoluteFileName(exchange);
        String breadCrumb = constructBreadCrumb(exchange);
        progressEventService.saveProgressEventRecord(new ProgressEventsRecord(breadCrumb,routeId, fileName, progressType, from, to));
    }

    @Override
    public void handleFailure(String routeId, String exception) {

    }

    @Override
    public void handleFailedEvent(String routeId, String exception, String from, String to) {

    }
}
