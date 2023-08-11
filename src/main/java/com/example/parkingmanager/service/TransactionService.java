package com.example.parkingmanager.service;

import com.amazonaws.services.sqs.model.Message;
import com.example.parkingmanager.config.SqsService;
import com.example.parkingmanager.constants.Constants;
import com.example.parkingmanager.dto.TransactionDTO;
import com.example.parkingmanager.dto.TransactionInDTO;
import com.example.parkingmanager.dto.TransactionOutDTO;
import com.example.parkingmanager.enums.EnumTransactionStatus;
import com.example.parkingmanager.enums.EnumVehicleType;
import com.example.parkingmanager.model.Transaction;
import com.example.parkingmanager.repository.ParkingmanagerRepository;
import com.example.parkingmanager.utils.JSONUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class TransactionService {

    private static final Logger logger = LogManager.getLogger(TransactionService.class);

    @Autowired
    SqsService sqsService;

    @Autowired
    ParkingmanagerRepository parkingmanagerRepository;

    public List<Message>  newParkingTransactions( List<Message> messages){

        List<Message> messagesToDelete = new ArrayList<>();
            for (Message message : messages) {
                try {
                    TransactionDTO transactionDTO = Optional.ofNullable(JSONUtils.jsonToObject(message.getBody(), TransactionDTO.class)).orElseThrow(() -> new ClassCastException("no es posible castear el mensaje"));
                    Transaction existingTransaction = parkingmanagerRepository.findByPlateAndStatus(transactionDTO.getPlate(), EnumTransactionStatus.STARTED.getId());
                    if (existingTransaction == null) {
                        Transaction transaction = messageToTransactionDTO(transactionDTO);
                        parkingmanagerRepository.save(transaction);
                        logger.info("ingreso del vehiculo  {}", transactionDTO.getPlate());
                    } else {
                        logger.info("ya se encuentra registrado el vehiculo en el parqueadero  {}", transactionDTO.getPlate());
                    }
                    messagesToDelete.add(message);
                } catch (Exception e) {
                    logger.error("An exception occurred: " + e.getMessage());
                }
            }
        return messagesToDelete;
    }


    public Transaction messageToTransactionDTO (TransactionDTO transactionDTO){
     return Transaction.builder()
            .plate(transactionDTO.getPlate().toUpperCase())
            .vehicleType(checkVehicleType(transactionDTO.getVehicleType()))
            .status(EnumTransactionStatus.STARTED.getId())
            .beginDate(new Date())
            .transactionId("P-" + transactionDTO.getVehicleType() + transactionDTO.getPlate() + Instant.now())
            .finishDate(new Date())
            .stayedTime(1l)
            .build();
    }

    public Integer checkVehicleType(Integer vehicleType) {
        if (vehicleType > 3 || vehicleType <= 0) {
        return EnumVehicleType.OTRO.getId();
        } else {
            return vehicleType;
        }
    }


    public  List<Message> finishParkingTransactions(List<Message> messages){

        List<Message> messagesToDelete = new ArrayList<>();
        for (Message message : messages) {
            try {
                TransactionDTO transactionDTO = Optional.ofNullable(JSONUtils.jsonToObject(message.getBody(), TransactionDTO.class)).orElseThrow(() -> new ClassCastException("no es posible castear el mensaje"));
                Transaction existingTransaction = parkingmanagerRepository.findByPlateAndStatus(transactionDTO.getPlate(), EnumTransactionStatus.STARTED.getId());
                if (existingTransaction != null && existingTransaction.getStatus() == EnumTransactionStatus.STARTED.getId()) {
                    existingTransaction.setFinishDate(new Date());
                    existingTransaction.setStatus(EnumTransactionStatus.FINISHED.getId());
                    existingTransaction.setStayedTime(getNewStayedTime(existingTransaction.getBeginDate(),existingTransaction.getFinishDate()));
                    parkingmanagerRepository.save(existingTransaction);
                    logger.info("salida del vehiculo  {}", transactionDTO.getPlate());
                } else {
                    logger.info("no se encuentra registrado el vehiculo en el parqueadero  {}", transactionDTO.getPlate());
                }
                messagesToDelete.add(message);
            } catch (Exception e) {
                logger.error("An exception occurred: " + e.getMessage());
            }
        }
        return messagesToDelete;
    }

    public void sendNewParkingTransactions(TransactionInDTO transactionInDTO) {
        sqsService.sendMessage(JSONUtils.objectToJson(transactionInDTO), Constants.NEW_TRANSACTION_PARKING_QUEUE);
    }

    public void sendFinishedParkingTransactions(TransactionInDTO transactionInDTO){
        sqsService.sendMessage(JSONUtils.objectToJson(transactionInDTO), Constants.FINISH_TRANSACTION_PARKING_QUEUE);
    }

    public Long getNewStayedTime(Date beginDate,  Date finishDate){
        // Calculate the time difference in milliseconds
        long timeDifferenceMillis = finishDate.getTime() - beginDate.getTime();
        Long timeDifferenceMinutes = timeDifferenceMillis / (1000 * 60);
        return timeDifferenceMinutes;
    }

    public  List<TransactionOutDTO> averageStayedTime() throws Exception {

        Map<String, Long> totalStayedTimeByType = new HashMap<>();
        Map<String, Integer> vehicleCountByType = new HashMap<>();
        List<TransactionOutDTO> transactionOutDTOList = new ArrayList<>();

        try{

            List<Transaction> transactionList = (List<Transaction>) parkingmanagerRepository.findAll();

            // Calculate total stayed time and vehicle count by kind
            for (Transaction transaction : transactionList) {
                String vehicleType = transaction.getVehicleType().toString();
                totalStayedTimeByType.put(vehicleType, totalStayedTimeByType.getOrDefault(vehicleType, 0L) + transaction.getStayedTime());
                vehicleCountByType.put(vehicleType, vehicleCountByType.getOrDefault(vehicleType, 0) + 1);
            }

            // Calculate average stayed time by type
            for (Map.Entry<String, Long> entry : totalStayedTimeByType.entrySet()) {
                String vehicleType = entry.getKey();
                long totalStayedTime = entry.getValue();
                int vehicleCount = vehicleCountByType.get(vehicleType);
                double averageStayedTime = (double) totalStayedTime / vehicleCount;

                TransactionOutDTO transactionOutDTO = new TransactionOutDTO();
                transactionOutDTO.setVehicleType(getVehicleTypeName(Integer.parseInt(vehicleType)));
                transactionOutDTO.setAvgStayedTime(averageStayedTime);
                transactionOutDTOList.add(transactionOutDTO);
            }
        }catch (Exception e){
          throw new Exception(e.getMessage());
        }
        return transactionOutDTOList;
    }

    public String getVehicleTypeName(int vehicleNumber) {
        for (EnumVehicleType type : EnumVehicleType.values()) {
            if (type.getId() == vehicleNumber) {
                return type.getName();
            }
        }
        return "Unknown"; // If no matching kind is found
    }

    public TransactionOutDTO longestStayedTime() throws Exception {

        TransactionOutDTO transactionOutDTO = new TransactionOutDTO();
        try{

            List<Transaction> transactionList = (List<Transaction>) parkingmanagerRepository.findAll();


        if (transactionList.isEmpty()) {
            return null; // No vehicles in the list
        }

        Transaction longestStayedVehicle = transactionList.get(0);

            for (Transaction vehicle : transactionList) {
                if (vehicle.getStayedTime() > longestStayedVehicle.getStayedTime()) {
                    longestStayedVehicle = vehicle;
                }
        }
        transactionOutDTO.setPlate(longestStayedVehicle.getPlate());
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return transactionOutDTO;
    }

}
