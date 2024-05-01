package com.definefunction.transfer;

import com.definefunction.transfer.factory.endpoint.DynamicResolver;
import com.definefunction.transfer.model.TransferRecord;
import com.definefunction.transfer.model.pojo.TransferLogging;
import com.definefunction.transfer.service.*;
import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
public class TransferApplication {

	@Autowired
	private CamelContext camelContext;

	@Autowired
	private RecordService recordService;

	@Autowired
	private URLBuilderService urlBuilderService;

	@Autowired
	private ProgressEventService progressEventService;

	@Autowired
	private InProgressService inProgressService;

	@Autowired
	private List<TransferLogging> transferLogging;

	@Autowired
	DynamicResolver dynamicResolver;

	private CamelService camelService;

	public static void main(String[] args) {
		SpringApplication.run(TransferApplication.class, args);
	}

	@Bean
	CamelContextConfiguration contextConfiguration() {
		return new CamelContextConfiguration() {
			@Override
			public void beforeApplicationStart(CamelContext context) {
				try {
					if (transferLogging.get(0).getTransferId() == null) {
						transferLogging.remove(0);
					}
					context.setLogMask(true);
					context.getStreamCachingStrategy().setSpoolEnabled(true);
					context.getStreamCachingStrategy().setSpoolDirectory("/tmp/cachedir");
					context.getStreamCachingStrategy().setSpoolThreshold(64 * 1024);
					context.getStreamCachingStrategy().setBufferSize(256 * 1024);
					context.setStreamCaching(true);

					// Initial routes from the database
					camelService = new CamelService(urlBuilderService,camelContext,recordService, progressEventService, inProgressService, dynamicResolver, transferLogging);
					camelService.AddSystemRoutes();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			@Override
			public void afterApplicationStart(CamelContext camelContext) {
				// Empty implementation
			}
		};
	}

	@Scheduled(fixedDelay = 60000)
	private void logActiveRoutes(){
		camelService.logActiveRouts();
	}

	@Scheduled(fixedDelay = 20000) // Poll the database every 20 seconds
	private void updateRoutesPeriodically() {
		try {
			// Update routes from the database
			// CamelService camelService = new CamelService(urlBuilderService,camelContext,recordService, progressEventService, inProgressService, dynamicResolver, transferLogging);
			camelService.AddActiveRoutes(recordService.getAllActiveRecords());

			// Remove routes that are no longer in the database
			camelService.removeRemovedRoutes();
			camelService.removeRoutesByStatus(recordService.getAllInactiveRecords());
			camelService.removeRoutesByStatus(recordService.getAllErrorRecords());
			camelService.removeRoutesByStatus(recordService.getAllFailedRecords());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
