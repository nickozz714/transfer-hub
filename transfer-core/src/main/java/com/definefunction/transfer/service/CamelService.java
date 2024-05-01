package com.definefunction.transfer.service;

import com.definefunction.transfer.factory.endpoint.DynamicResolver;
import com.definefunction.transfer.factory.transfers.AzureRoutesFactory;
import com.definefunction.transfer.factory.transfers.DefaultRouteFactory;
import com.definefunction.transfer.factory.transfers.DefaultSystemRoutesFactory;
import com.definefunction.transfer.model.*;
import com.definefunction.transfer.model.pojo.TransferLogging;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.component.azure.storage.blob.BlobConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.*;

import static com.definefunction.transfer.utilities.Utilities.getRouteContextName;

@Service
public class CamelService {

    private static final Logger logger = LoggerFactory.getLogger(CamelService.class);

    private final RecordService recordService;

    private final DefaultRouteFactory defaultRouteFactory;

    private final AzureRoutesFactory azureRoutesFactory;

    private final DefaultSystemRoutesFactory defaultSystemRoutesFactory;

    private final List<TransferLogging> transferLogging;

    @Autowired
    public CamelService(URLBuilderService urlBuilderService, CamelContext context, RecordService recordService, ProgressEventService progressEventService, InProgressService inProgressService, DynamicResolver dynamicResolver, List<TransferLogging> transferLogging) {
        this.recordService = recordService;
        this.transferLogging = transferLogging;
        this.azureRoutesFactory = new AzureRoutesFactory(dynamicResolver, urlBuilderService, context, recordService, progressEventService, inProgressService, transferLogging);
        this.defaultRouteFactory = new DefaultRouteFactory(dynamicResolver, urlBuilderService, context, recordService, progressEventService, inProgressService, transferLogging);
        this.defaultSystemRoutesFactory = new DefaultSystemRoutesFactory(dynamicResolver, urlBuilderService, context, recordService, progressEventService, inProgressService, transferLogging);
    }

    public void logActiveRouts() {
        logger.info("Transfer-hub: Amount of active transfers - " + defaultRouteFactory.getActiveRoutes().size());
    }

    public void AddActiveRoutes(List<TransferRecord> records) throws Exception {
        Map<String, Integer> activeRoutes = defaultRouteFactory.getActiveRoutes();
        for (TransferRecord transferRecord : records) {
            if (activeRoutes.containsKey(transferRecord.getId())) {
                int activeVersion = activeRoutes.get(transferRecord.getId());
                int transferRecordVersion = transferRecord.getVersion();
                if (transferRecordVersion > activeVersion) {
                    // Route has changed, remove and add the updated route
                    defaultRouteFactory.RemoveFromCamelContext(getRouteContextName(transferRecord));
                    AddToCamelContext(transferRecord);
                }
            } else {
                // Route doesn't exist, add it
                AddToCamelContext(transferRecord);
            }
        }
    }

    public void AddSystemRoutes() throws Exception {
        logger.info("CamelContextEvent | Adding System Routes | Status: started");
        defaultSystemRoutesFactory.AddSystemCamelRoutesToContext();
        logger.info("CamelContextEvent | Adding System Routes | Status: finished");
    }

    private void AddToCamelContext(TransferRecord transferRecord) throws Exception {
        // Which Route Factory is needed, should be based on the protocols.
        List<String> protocols = transferRecord.getEndpoints().stream().map(Endpoint::getProtocol).toList();
        if (protocols.contains("azure-storage-blob")) {
            // Contains azure-blob component, needs to be created using the azureRoutesFactory
            azureRoutesFactory.addRouteToContext(transferRecord);
        } else if (protocols.contains("azure-files")) {
            defaultRouteFactory.addRouteToContext(transferRecord);
        } else {
            // Contains sftp and azure-files component, needs to be created using the defaultRoutesFactory
            defaultRouteFactory.addRouteToContext(transferRecord);
        }
    }

    public void removeRemovedRoutes() throws Exception {
        Map<String, Integer> activeRoutes = defaultRouteFactory.getActiveRoutes();
        for (String routeId : activeRoutes.keySet()) {
            if (recordService.getRecordById(routeId).isEmpty()) {
                logger.info("CamelContextEvent | Removing " + routeId + " | Status: started");
                defaultRouteFactory.RemoveFromCamelContext(routeId+"-"+activeRoutes.get(routeId));
                logger.info("CamelContextEvent | Removing " + routeId + " | Status: finished");
            }
        }
    }

    public void removeRoutesByStatus(List<TransferRecord> transferRecords) throws Exception {
        Map<String, Integer> activeRoutes = defaultRouteFactory.getActiveRoutes();
        for (TransferRecord transferRecord : transferRecords) {
            if (activeRoutes.containsKey(transferRecord.getId())) {
                logger.info("CamelContextEvent | Removing " + transferRecord.getId() + " | Status: started");
                defaultRouteFactory.RemoveFromCamelContext(transferRecord.getId()+"-"+activeRoutes.get(transferRecord.getId()));
                logger.info("CamelContextEvent | Removing " + transferRecord.getId() + " | Status: finished");
            }
        }
    }

    @Deprecated
    public boolean isEndpointNotTheSame(TransferRecord existingTransferRecord, TransferRecord transferRecord) throws IllegalAccessException {
        List<Endpoint> existingEndpoints = existingTransferRecord.getEndpoints();
        List<Endpoint> endpoints = transferRecord.getEndpoints();
        List<Class<?>> list = new ArrayList<>();
        list.add(TransferRecord.class);
        list.add(Credential.class);
        list.add(Host.class);
        list.add(List.class);
        if (existingEndpoints.size() == endpoints.size()){
            for (int e = 0; e < existingEndpoints.size(); e++){
                if (!isTheSame(existingEndpoints.get(e), endpoints.get(e), list) ||
                        !isTheSame(existingEndpoints.get(e).getHost(), endpoints.get(e).getHost(), list) ||
                        !isTheSame(existingEndpoints.get(e).getCredential(), endpoints.get(e).getCredential(), list)) {
                    // The endpoint are different and the route needs to be loaded again.
                    return false;
                }
            }
        }
        // All the values are the same, there is no need to define a difference and update the route in the context.
        return false;
    }

    @Deprecated
    public boolean isTheSame(Object a, Object b, List<Class<?>> list) throws IllegalAccessException {
        Field[] aField = a.getClass().getDeclaredFields();
        Field[] bField = b.getClass().getDeclaredFields();
        for (int i = 0; i < aField.length; i++) {
            Field fa = aField[i];
            Field fb = bField[i];
            fa.setAccessible(true);
            fb.setAccessible(true);
            try {
                if (fa.getType() == ZonedDateTime.class) {
                    if (!((ZonedDateTime) fa.get(a)).isEqual((ZonedDateTime) fb.get(b))) {
                        return false;
                    }
                } else {
                    if (fa.getType() == String.class) { // If the field is of String format, it needs to compare using equals.
                        if (!list.contains(fa.getType()) && !fa.get(a).equals(fb.get(b))) {
                            // The value is not the same for this value. The route needs to be updated.
                            return false;
                        }
                    } else { // Anything else is fine.
                        if (!list.contains(fa.getType()) && fa.get(a) != fb.get(b)) {
                            // The value is not the same for this value. The route needs to be updated.
                            return false;
                        }
                    }
                }
            } catch (NullPointerException e) {
                if (fa.get(a) != null || fb.get(b) != null) {
                    return false;
                }
            }
        }
        return true;
    }
}
