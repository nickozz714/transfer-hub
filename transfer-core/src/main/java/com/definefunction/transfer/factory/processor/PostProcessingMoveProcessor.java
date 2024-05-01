package com.definefunction.transfer.factory.processor;


import com.definefunction.transfer.factory.processor.model.ProcessingType;
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

public class PostProcessingMoveProcessor extends AbstractProcessorEvents implements Processor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public PostProcessingMoveProcessor(InProgressService inProgressService, RecordService recordService, ProgressEventService progressEventService, URLBuilderService urlBuilderService, List<TransferLogging> transferLogging) {
        super(inProgressService, recordService, progressEventService, urlBuilderService, transferLogging);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        String from = Utilities.sanitizeParametersInURL(getFromEndpointUrl(exchange.getFromRouteId()));
        String to = Utilities.sanitizeParametersInURL(getToEndpointUrl(exchange.getFromRouteId()));
        logger.info("Post-processing move | " + exchange.getFromRouteId() + " | From: "+from+" | To: "+to+" | File: " + constructAbsoluteFileName(exchange));
        setPostProcessingBlobHeaders(exchange, ProcessingType.MOVE);
    }
}
