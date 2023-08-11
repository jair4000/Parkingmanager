package com.example.parkingmanager.scheduled;

import com.amazonaws.services.datasync.model.TaskSchedule;
import com.amazonaws.services.sqs.model.Message;
import com.example.parkingmanager.config.AWSClient;
import com.example.parkingmanager.config.SqsService;
import com.example.parkingmanager.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NewParkingScheduled  extends TaskSchedule {

    @Autowired
    private final SqsService sqsService;

    @Autowired
    private TransactionService transactionService;

    protected NewParkingScheduled(TransactionService transactionService, SqsService sqsService) {
        super();
        this.transactionService = transactionService;
        this.sqsService = sqsService;
    }



    @Scheduled(fixedDelay = 1 * 60 * 1000)
    public void executeTaskNewParkingTransactionsTask() {
        newParkingTransactionsTask();
    }

    protected void newParkingTransactionsTask() {
        List<Message> messages = sqsService.getMessages(AWSClient.NEW_TRANSACTION_PARKING_QUEUE);
        List<Message> messagesToDelete = transactionService.newParkingTransactions(messages);
        sqsService.deleteMessages(messagesToDelete, AWSClient.NEW_TRANSACTION_PARKING_QUEUE);
    }

}
