package com.definefunction.transfer.model.pojo;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class TransferLogging {

    public String transferId;

    public List<String> parsedEndpointUrlList;

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public List<String> getParsedEndpointUrlList() {
        return parsedEndpointUrlList;
    }

    public void setParsedEndpointUrlList(List<String> parsedEndpointUrlList) {
        this.parsedEndpointUrlList = parsedEndpointUrlList;
    }
}
