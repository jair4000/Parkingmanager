package com.example.parkingmanager.service;

import com.amazonaws.services.sqs.model.Message;
import com.example.parkingmanager.config.SqsService;
import com.example.parkingmanager.constants.Constants;
import com.example.parkingmanager.dto.TransactionDTO;
import com.example.parkingmanager.dto.TransactionInDTO;
import com.example.parkingmanager.dto.TransactionOutDTO;
import com.example.parkingmanager.enums.EnumTransactionStatus;
import com.example.parkingmanager.enums.EnumVehicleType;
import com.example.parkingmanager.model.AverageParking;
import com.example.parkingmanager.model.LongestStayParking;
import com.example.parkingmanager.model.Transaction;
import com.example.parkingmanager.repository.ParkingAverageStayRepository;
import com.example.parkingmanager.repository.ParkingTheLongestStayRepository;
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

    @Autowired
    ParkingTheLongestStayRepository parkingTheLongestStayRepository;

    @Autowired
    ParkingAverageStayRepository parkingAverageStayRepository;

    public List<Message>  newParkingTransactions( List<Message> messages){

        List<Message> messagesToDelete = new ArrayList<>();
            for (Message message : messages) {
                try {
                    TransactionDTO transactionDTO = Optional.ofNullable(JSONUtils.jsonToObject(message.getBody(), TransactionDTO.class)).orElseThrow(() -> new ClassCastException("no es posible castear el mensaje"));
                    Transaction existingTransaction = parkingmanagerRepository.findByPlateAndStatus(transactionDTO.getPlate().toUpperCase(), EnumTransactionStatus.STARTED.getId());
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
            .stayedTime(1.0)
             .isAvgSynchronized(Constants.NO)
             .isLongestSynchronized(Constants.NO)
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

    public Double getNewStayedTime(Date beginDate,  Date finishDate){
        // Calculate the time difference in milliseconds
        long timeDifferenceMillis = finishDate.getTime() - beginDate.getTime();
        Double timeDifferenceMinutes = (double) (timeDifferenceMillis / (1000 * 60));
        return timeDifferenceMinutes;
    }

    public  List<TransactionOutDTO> averageStayedTime() throws Exception {

        Map<String, Double> totalStayedTimeByType = new HashMap<>();
        Map<String, Integer> vehicleCountByType = new HashMap<>();
        List<TransactionOutDTO> transactionOutDTOList = new ArrayList<>();

        try{

            List<Transaction> transactionList = (List<Transaction>) parkingmanagerRepository.findAllByIsAvgSynchronized(Constants.NO);

            // Calculate total stayed time and vehicle count by kind
            for (Transaction transaction : transactionList) {
                String vehicleType = transaction.getVehicleType().toString();
                totalStayedTimeByType.put(vehicleType, totalStayedTimeByType.getOrDefault(vehicleType, 0.0) + transaction.getStayedTime());
                vehicleCountByType.put(vehicleType, vehicleCountByType.getOrDefault(vehicleType, 0) + 1);
            }

            // Calculate average stayed time by type
            for (Map.Entry<String, Double> entry : totalStayedTimeByType.entrySet()) {
                String vehicleType = entry.getKey();
                Double totalStayedTime = entry.getValue();
                int vehicleCount = vehicleCountByType.get(vehicleType);
                double averageStayedTime = (double) totalStayedTime / vehicleCount;

                TransactionOutDTO transactionOutDTO = new TransactionOutDTO();
                transactionOutDTO.setVehicleType(getVehicleTypeName(Integer.parseInt(vehicleType)));
                transactionOutDTO.setAvgStayedTime(averageStayedTime);
                transactionOutDTOList.add(transactionOutDTO);
                updateCheckedAverageParkingTransactions(transactionList);
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
        logger.info("########## empieza tarea automatica indentificar vehiculo mas tiempo en parqueadero");
        TransactionOutDTO transactionOutDTO = new TransactionOutDTO();
        try{

            List<Transaction> transactionList = (List<Transaction>) parkingmanagerRepository.findAllByIsLongestSynchronized(Constants.NO);


        if (transactionList.isEmpty()) {
            logger.info("########## no se encontraron nuevos vehiculos");
            return new TransactionOutDTO(); // No vehicles in the list
        }

        Transaction longestStayedVehicle = transactionList.get(0);

            for (Transaction vehicle : transactionList) {
                if (vehicle.getStayedTime() > longestStayedVehicle.getStayedTime()) {
                    longestStayedVehicle = vehicle;
                }
        }
            transactionOutDTO.setPlate(longestStayedVehicle.getPlate());
            transactionOutDTO.setTrasactionId(longestStayedVehicle.getTransactionId());
            transactionOutDTO.setAvgStayedTime(longestStayedVehicle.getStayedTime());
            updateCheckedLongestStayParkingTransactions(transactionList);

            logger.info("########## finaliza tarea, con vehiculo {}", longestStayedVehicle.getPlate());
        }catch (Exception e){
            logger.error("An exception occurred: " + e.getMessage());
            throw new Exception(e.getMessage());
        }
        return transactionOutDTO;
    }

    public void updateCheckedLongestStayParkingTransactions(List<Transaction> transactionList ){
        logger.info("########## se actualiza estado de vehiculos ya evaluados para permanencia");
        for(Transaction transaction: transactionList){
            transaction.setIsLongestSynchronized(Constants.YES);
            parkingmanagerRepository.save(transaction);
        }
    }

    public void updateCheckedAverageParkingTransactions(List<Transaction> transactionList){
        logger.info("########## se actualiza estado de vehiculos ya evaluados para promedio");
        for(Transaction transaction: transactionList){
            transaction.setIsAvgSynchronized(Constants.YES);
            parkingmanagerRepository.save(transaction);
        }
    }

    public void  updateLongestStayParking() throws Exception {
        TransactionOutDTO transactionOutDTO =  longestStayedTime();

        if(transactionOutDTO.getAvgStayedTime() != null) {
            List<LongestStayParking> longestStayParkingList = (List<LongestStayParking>) parkingTheLongestStayRepository.findAll();
            if(longestStayParkingList.isEmpty()){
                LongestStayParking longestStayParking = new LongestStayParking();
                longestStayParking.setPlate(transactionOutDTO.getPlate());
                longestStayParking.setTransactionId("L-" + Instant.now());
                longestStayParking.setAvgStayedTime(transactionOutDTO.getAvgStayedTime());
                parkingTheLongestStayRepository.save(longestStayParking);
            }else if (transactionOutDTO.getAvgStayedTime() > longestStayParkingList.get(0).getAvgStayedTime()) {
                parkingTheLongestStayRepository.deleteAll();
                LongestStayParking longestStayParking = new LongestStayParking();
                longestStayParking.setPlate(transactionOutDTO.getPlate());
                longestStayParking.setTransactionId("L-" + Instant.now());
                longestStayParking.setAvgStayedTime(transactionOutDTO.getAvgStayedTime());
                parkingTheLongestStayRepository.save(longestStayParking);
            }
        }
    }

    public void updateAverageParking() throws Exception {

        List<TransactionOutDTO> transactionOutDTOList = averageStayedTime();

        if (transactionOutDTOList.isEmpty()) {
            logger.info("######### no hay promedios para actualizar.");
        } else {
            List<AverageParking> existingParkingAverages = (List<AverageParking>) parkingAverageStayRepository.findAll();

            // Map existing averages by vehicle type
            Map<String, AverageParking> vehicleTypeToAverageMap = new HashMap<>();
            for (AverageParking existingAverage : existingParkingAverages) {
                vehicleTypeToAverageMap.put(existingAverage.getVehicleType(), existingAverage);
            }

            // Update or add new rows for vehicle types
            for (TransactionOutDTO transactionOutDTO : transactionOutDTOList) {
                String vehicleType = transactionOutDTO.getVehicleType();
                double newAvgStayedTime = transactionOutDTO.getAvgStayedTime();

                if (vehicleTypeToAverageMap.containsKey(vehicleType)) {
                    AverageParking existingAverage = vehicleTypeToAverageMap.get(vehicleType);
                    double existingAvgStayedTime = existingAverage.getAvgStayedTime();
                    existingAverage.setAvgStayedTime(existingAvgStayedTime + newAvgStayedTime);
                    parkingAverageStayRepository.save(existingAverage);
                } else {
                    AverageParking newAverageParking = new AverageParking();
                    newAverageParking.setVehicleType(vehicleType);
                    newAverageParking.setAvgStayedTime(newAvgStayedTime);
                    parkingAverageStayRepository.save(newAverageParking);
                    vehicleTypeToAverageMap.put(vehicleType, newAverageParking);
                }
            }

            logger.info("########## promedios actualizados");
        }

    }

    public TransactionOutDTO getLongestStayedTime(){
        TransactionOutDTO transactionOutDTO = new TransactionOutDTO();
        List<LongestStayParking> longestStayParking =  (List<LongestStayParking>) parkingTheLongestStayRepository.findAll();
        transactionOutDTO.setPlate(longestStayParking.get(0).getPlate());
        return transactionOutDTO;
    }

    public List<TransactionOutDTO> getAverageStayedTime() {
        List<TransactionOutDTO> transactionOutDTOList = new ArrayList<>();
        List<AverageParking> averageParkingList = (List<AverageParking>) parkingAverageStayRepository.findAll();

        for (AverageParking avg : averageParkingList) {
            TransactionOutDTO transactionOutDTO = new TransactionOutDTO();
            transactionOutDTO.setAvgStayedTime(avg.getAvgStayedTime());
            transactionOutDTO.setVehicleType(avg.getVehicleType());
            transactionOutDTOList.add(transactionOutDTO);
        }

        return transactionOutDTOList;
    }


}
