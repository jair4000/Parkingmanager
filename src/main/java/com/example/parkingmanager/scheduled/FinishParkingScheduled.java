package com.example.parkingmanager.scheduled;

import com.amazonaws.services.datasync.model.TaskSchedule;
import com.amazonaws.services.sqs.model.Message;
import com.example.parkingmanager.config.AWSClient;
import com.example.parkingmanager.config.SqsService;
import com.example.parkingmanager.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FinishParkingScheduled  extends TaskSchedule {

    @Autowired
    private final SqsService sqsService;

    @Autowired
    private TransactionService transactionService;

    protected FinishParkingScheduled(TransactionService transactionService, SqsService sqsService) {
        super();
        this.transactionService = transactionService;
        this.sqsService = sqsService;
    }



    @Scheduled(fixedDelay = 1 * 60 * 1000)
    public void executeTaskFinishParkingTransactionsTask() {
        finishParkingTransactionsTask();
    }

    protected void finishParkingTransactionsTask() {
        List<Message> messages = sqsService.getMessages(AWSClient.FINISH_TRANSACTION_PARKING_QUEUE);
        List<Message> messagesToDelete = transactionService.finishParkingTransactions(messages);
        sqsService.deleteMessages(messagesToDelete, AWSClient.FINISH_TRANSACTION_PARKING_QUEUE);
    }

}
