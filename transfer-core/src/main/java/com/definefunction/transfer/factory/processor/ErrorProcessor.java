package com.definefunction.transfer.factory.processor;

import com.definefunction.transfer.model.pojo.Direction;
import com.definefunction.transfer.model.pojo.TransferLogging;
import com.definefunction.transfer.service.InProgressService;
import com.definefunction.transfer.service.ProgressEventService;
import com.definefunction.transfer.service.RecordService;
import com.definefunction.transfer.service.URLBuilderService;
import com.definefunction.transfer.utilities.Utilities;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ErrorProcessor extends AbstractProcessorEvents implements Processor {

    private final Logger logger = LoggerFactory.getLogger(ErrorProcessor.class);
    public ErrorProcessor(InProgressService inProgressService, RecordService recordService, ProgressEventService progressEventService, URLBuilderService urlBuilderService, List<TransferLogging> transferLogging) {
        super(inProgressService, recordService, progressEventService, urlBuilderService, transferLogging);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        String routeId = exchange.getFromRouteId();
        String from = Utilities.sanitizeParametersInURL(getFromEndpointUrl(exchange.getFromRouteId()));
        String to = Utilities.sanitizeParametersInURL(getToEndpointUrl(exchange.getFromRouteId()));
        String exception = handleError(exchange, routeId, getFromEndpointUrl(routeId), getToEndpointUrl(routeId));
        logger.error("Processing failed | " + exchange.getFromRouteId() + " | From: "+from+" | To: "+to+" |  Exception: " + exception);
    }
}
