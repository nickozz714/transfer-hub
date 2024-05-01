package com.definefunction.transfer.model;

import com.definefunction.transfer.model.pojo.ProgressType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.ZonedDateTime;

@Entity
public class InProgressRecord {
    public InProgressRecord(String transfer, String file, ZonedDateTime processed_at) {
        this.transfer = transfer;
        this.file = file;
        this.processed_at = processed_at;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transfer;

    private String file;

    private ZonedDateTime processed_at;

    public InProgressRecord() {

    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
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

    public ZonedDateTime getProcessed_at() {
        return processed_at;
    }

    public void setProcessed_at(ZonedDateTime processed_at) {
        this.processed_at = processed_at;
    }
}
