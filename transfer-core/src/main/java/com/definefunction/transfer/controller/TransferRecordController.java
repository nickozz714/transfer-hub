package com.definefunction.transfer.controller;

import com.definefunction.transfer.exception.ObjectDoesNotExistsException;
import com.definefunction.transfer.exception.TransferRecordNotValidException;
import com.definefunction.transfer.model.DTO.ConnectCheck;
import com.definefunction.transfer.model.DTO.TransferRecordDTO;
import com.definefunction.transfer.model.TransferRecord;
import com.definefunction.transfer.model.pojo.Status;
import com.definefunction.transfer.model.response.ResponseType;
import com.definefunction.transfer.model.views.View;
import com.definefunction.transfer.service.RecordService;
import com.definefunction.transfer.service.URLBuilderService;
import com.definefunction.transfer.service.ValidationService;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin("*")
@RestController
@RequestMapping(TransferRecordController.URL)
public class TransferRecordController {

    public static final String URL = "api/transfer";

    @Autowired
    private RecordService recordService;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private URLBuilderService urlBuilderService;

    @PostMapping("/create")
    @JsonView(value= View.UserView.GET.class)
    public ResponseEntity<TransferRecord> save(@RequestBody TransferRecordDTO transferRecordDTO, @RequestHeader(name = "Authorization") String token) throws Exception {
        if (validationService.isRouteValid(transferRecordDTO)) {
            return new ResponseEntity<>(recordService.savedDTO(transferRecordDTO, token), HttpStatus.CREATED);
        } else {
            throw new TransferRecordNotValidException("Transferrecord name: "+transferRecordDTO.getId()+" is not allowed. Please make sure to use uppercase, lowercase, underscores, dashes and numbers only.");
        }
    }

    @GetMapping
    @JsonView(value= View.UserView.GET.class)
    public ResponseEntity<List<TransferRecord>> getAll(@RequestHeader(name = "Authorization") String token) {
        return new ResponseEntity<>(recordService.getAllAuthorizedRecords(token), HttpStatus.OK);
    }

    @GetMapping("/scope/{id}")
    @JsonView(value= View.UserView.GET.class)
    public ResponseEntity<List<TransferRecord>> getAll(@PathVariable long id, @RequestHeader(name = "Authorization") String token) throws Exception {
        return new ResponseEntity<>(recordService.getAllRecordsByScope(id, token), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @JsonView(value= View.UserView.GET.class)
    public ResponseEntity<TransferRecord> getById(@PathVariable String id, @RequestHeader(name = "Authorization") String token) throws Exception {
        if(recordService.exists(id)) {
            return new ResponseEntity<>(recordService.getRecordByIdExternally(id, token), HttpStatus.OK);
        } else {
            throw new ObjectDoesNotExistsException("TransferRecord "+id+" does not exist.");
        }
    }

    @PostMapping("/endpoint/connection")
    public ResponseEntity<Boolean> isConnecting(@RequestBody ConnectCheck connectCheck) {
        return new ResponseEntity<>(urlBuilderService.isHostnameReachable(connectCheck.getHost(), connectCheck.getPort()), HttpStatus.CREATED);
    }

    @PutMapping("/update")
    @JsonView(value= View.UserView.GET.class)
    public ResponseEntity<TransferRecord> update(@RequestBody TransferRecordDTO transferRecordDTO, @RequestHeader(name = "Authorization") String token) throws Exception {
        TransferRecord transferRecord = recordService.update(transferRecordDTO, token);
        if(transferRecord != null) {
            return new ResponseEntity<>(transferRecord, HttpStatus.OK);
        } else {
            throw new ObjectDoesNotExistsException("TransferRecord: "+transferRecordDTO.getId()+" does not exist.");
        }
    }

    @PostMapping("/inactive/{id}")
    @JsonView(value= View.UserView.GET.class)
    public ResponseEntity<TransferRecord> setInactive(@PathVariable String id, @RequestHeader(name = "Authorization") String token) throws Exception {
        TransferRecord transferRecord = recordService.getRecordByIdExternally(id, token);
        if (transferRecord != null) {
            transferRecord.setStatus(Status.INACTIVE);
            recordService.save(transferRecord);
            return new ResponseEntity<>(transferRecord, HttpStatus.CREATED);
        } else {
            throw new ObjectDoesNotExistsException("TransferRecord "+id+" does not exist.");
        }
    }

    @PostMapping("/active/{id}")
    @JsonView(value= View.UserView.GET.class)
    public ResponseEntity<TransferRecord> setActive(@PathVariable String id, @RequestHeader(name = "Authorization") String token) throws Exception {
        TransferRecord transferRecord = recordService.getRecordByIdExternally(id,token);
        if (transferRecord != null) {
            transferRecord.setStatus(Status.ACTIVE);
            recordService.save(transferRecord);
            return new ResponseEntity<>(transferRecord, HttpStatus.CREATED);
        } else {
            throw new ObjectDoesNotExistsException("TransferRecord "+id+" does not exist.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id, @RequestHeader(name = "Authorization") String token) throws Exception {
        recordService.delete(id, token);
        return new ResponseEntity<>("Transfer removed", HttpStatus.NO_CONTENT);
    }
}
