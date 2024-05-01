package com.definefunction.transfer.service;

import com.definefunction.transfer.controller.ProgressEventsController;
import com.definefunction.transfer.model.ProgressEventsRecord;
import com.definefunction.transfer.repository.ProgressEventsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class ProgressEventService {

    @Autowired
    private ProgressEventsRepository progressEventsRepository;

    public void saveProgressEventRecord(ProgressEventsRecord progressEventsRecord) {
        progressEventsRecord.setProcessed_at(ZonedDateTime.now());
        progressEventsRepository.save(progressEventsRecord);
    }

    public List<ProgressEventsRecord> getAll() {
        return progressEventsRepository.findAll();
    }

    public List<ProgressEventsRecord> getAllByTransfer(String transfer) {
        return progressEventsRepository.findAllByTransferIs(transfer);
    }
}
