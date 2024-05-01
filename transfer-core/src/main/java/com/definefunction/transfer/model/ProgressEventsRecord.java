package com.definefunction.transfer.model;

import com.definefunction.transfer.model.pojo.ProgressType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.ZonedDateTime;

@Entity
public class ProgressEventsRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String caseNumber;

    private String transfer;

    private String fromHost;

    private String toHost;

    private String file;

    private ProgressType progressType;

    private String exception_message;

    private ZonedDateTime processed_at;

    public ProgressEventsRecord(String caseNumber, String transfer, String file, ProgressType progressType, String from, String to) {
        this.caseNumber = caseNumber;
        this.transfer = transfer;
        this.fromHost = from;
        this.toHost = to;
        this.file = file;
        this.progressType = progressType;
    }

    public ProgressEventsRecord() {

    }

    public ProgressEventsRecord(String transfer, ProgressType progressType) {
        this.transfer = transfer;
        this.progressType = progressType;
        this.processed_at = ZonedDateTime.now();
    }

    public ProgressEventsRecord(String transfer, ProgressType progressType, String exception_message) {
        this.transfer = transfer;
        this.progressType = progressType;
        this.exception_message = exception_message;
        this.processed_at = ZonedDateTime.now();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTransfer() {
        return transfer;
    }

    public void setTransfer(String transfer) {
        this.transfer = transfer;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public ProgressType getProgressType() {
        return progressType;
    }

    public void setProgressType(ProgressType progressType) {
        this.progressType = progressType;
    }

    public String getException_message() {
        return exception_message;
    }

    public void setException_message(String exception_message) {
        this.exception_message = exception_message;
    }

    public ZonedDateTime getProcessed_at() {
        return processed_at;
    }

    public void setProcessed_at(ZonedDateTime processed_at) {
        this.processed_at = processed_at;
    }

    public String getFromHost() {
        return fromHost;
    }

    public void setFromHost(String fromHost) {
        this.fromHost = fromHost;
    }

    public String getToHost() {
        return toHost;
    }

    public void setToHost(String toHost) {
        this.toHost = toHost;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }
}
