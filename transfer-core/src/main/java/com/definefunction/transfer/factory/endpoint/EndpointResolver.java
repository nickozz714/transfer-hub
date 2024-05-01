package com.definefunction.transfer.factory.endpoint;

import com.definefunction.transfer.model.pojo.ParsedTransferEndpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class EndpointResolver implements CustomResolver {

    private static final String ENDPOINTS_HEADER = "Endpoints";

    public ProcessorDefinition<?> from(RouteBuilder routeBuilder, String url) {
        ProcessorDefinition<?> transfer = routeBuilder.from(url).streamCaching();
        return transfer;
    }

    @Override
    public ProcessorDefinition<?> from(RouteBuilder routeBuilder, ParsedTransferEndpoint parsedTransferEndpoint, UnaryOperator<ProcessorDefinition<?>> ExceptionHandler, UnaryOperator<ProcessorDefinition<?>> processorDefinitionUnaryOperator) {
        ProcessorDefinition<?> transfer = routeBuilder.from(parsedTransferEndpoint.getEndpointURL()).streamCaching();
        ExceptionHandler.apply(transfer);
        processorDefinitionUnaryOperator.apply(transfer);
        return transfer;
    }

    @Override
    public ProcessorDefinition<?> to(RouteBuilder routeBuilder, ProcessorDefinition<?> transfer, ParsedTransferEndpoint... parsedTransferEndpoints) {
        List<String> toEndpointUris = Stream.of(parsedTransferEndpoints)
                .map(ParsedTransferEndpoint::getEndpointURL)
                .collect(Collectors.toList());
        if(toEndpointUris.size() == 1) {
            return transfer
                    .to(toEndpointUris.get(0));
        } else {
            return transfer
                    .setHeader(ENDPOINTS_HEADER, routeBuilder.constant(toEndpointUris))
                    .recipientList(routeBuilder.header(ENDPOINTS_HEADER));
        }
    }

    @Override
    public boolean resolves(String... endpointUrls) {
        return true;
    }
}
