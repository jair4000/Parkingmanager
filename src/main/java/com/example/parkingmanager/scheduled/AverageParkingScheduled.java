package com.example.parkingmanager.scheduled;

import com.amazonaws.services.sqs.model.Message;
import com.example.parkingmanager.config.AWSClient;
import com.example.parkingmanager.config.SqsService;
import com.example.parkingmanager.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AverageParkingScheduled {

    @Autowired
    private final SqsService sqsService;

    @Autowired
    private TransactionService transactionService;

    protected AverageParkingScheduled(TransactionService transactionService, SqsService sqsService) {
        super();
        this.transactionService = transactionService;
        this.sqsService = sqsService;
    }



    @Scheduled(fixedDelay = 1 * 60 * 1000)
    public void executeTaskAverageParkingScheduledTask() throws Exception {
        averageParkingScheduledTask();
    }

    protected void averageParkingScheduledTask() throws Exception {
        transactionService.updateAverageParking();
    }

}
