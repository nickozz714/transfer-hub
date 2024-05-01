package com.definefunction.transfer.factory.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class DynamicResolver {

    private final List<CustomResolver> resolverList;

    @Autowired
    public DynamicResolver(List<CustomResolver> resolverList) {
        this.resolverList = Collections.unmodifiableList(resolverList);
    }

    public CustomResolver getResolver(String... endpointUrls) throws Exception {
        return resolverList.stream()
                .filter(c -> c.resolves(endpointUrls))
                .findFirst()
                .orElseThrow(() -> new Exception("Failed to resolve endpoints"));
    }

}
