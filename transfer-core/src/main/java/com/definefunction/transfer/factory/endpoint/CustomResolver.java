package com.definefunction.transfer.factory.endpoint;

import com.definefunction.transfer.model.TransferRecord;
import com.definefunction.transfer.model.pojo.ParsedTransferEndpoint;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;

import java.util.function.UnaryOperator;

public interface CustomResolver {
    ProcessorDefinition<?> from(RouteBuilder routeBuilder, ParsedTransferEndpoint parsedTransferEndpoint, UnaryOperator<ProcessorDefinition<?>> ExceptionHandler, UnaryOperator<ProcessorDefinition<?>> processorDefinitionUnaryOperator);

    ProcessorDefinition<?> to(RouteBuilder routeBuilder, ProcessorDefinition<?> transfer, ParsedTransferEndpoint... parsedTransferEndpoints);

    boolean resolves(String... endpointUrls);
}
