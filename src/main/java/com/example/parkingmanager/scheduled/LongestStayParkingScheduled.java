package com.example.parkingmanager.scheduled;

import com.example.parkingmanager.config.SqsService;
import com.example.parkingmanager.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LongestStayParkingScheduled {


    @Autowired
    private final SqsService sqsService;

    @Autowired
    private TransactionService transactionService;

    protected LongestStayParkingScheduled(TransactionService transactionService, SqsService sqsService) {
        super();
        this.transactionService = transactionService;
        this.sqsService = sqsService;
    }



    @Scheduled(fixedDelay = 1 * 60 * 1000)
    public void executeTaskLongestStayParkingScheduledTask() throws Exception {
        longestStayParkingScheduledTask();
    }

    protected void longestStayParkingScheduledTask() throws Exception {
        transactionService.updateLongestStayParking();
    }



}
