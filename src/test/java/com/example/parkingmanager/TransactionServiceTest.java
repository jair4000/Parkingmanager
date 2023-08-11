package com.example.parkingmanager;

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
import com.example.parkingmanager.service.TransactionService;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

public class TransactionServiceTest {

    @Mock
    private ParkingmanagerRepository parkingmanagerRepository;

    @Mock
    private SqsService mockSqsService;

    @Mock
    private Logger mockLogger;

    @InjectMocks()
    TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void testFindLongestStayedVehicle() throws Exception {

        List<Transaction> vehicles = new ArrayList<>();
        vehicles.add(new Transaction("ABC123", 120.0));
        vehicles.add(new Transaction("XAP512", 180.0));
        vehicles.add(new Transaction("LDSR123", 140.0));
        vehicles.add(new Transaction("AFQ32F", 90.0));


        when( (List<Transaction>)  parkingmanagerRepository.findAll()).thenReturn(vehicles);

        TransactionOutDTO longestStayed = transactionService.longestStayedTime();

        assertEquals("XAP512", longestStayed.getPlate());
    }

    @Test
    public void testFindLongestStayedVehicleEmptyList() throws Exception {
        List<Transaction> vehicles = new ArrayList<>();


        when( (List<Transaction>) parkingmanagerRepository.findAll()).thenReturn(vehicles);

        TransactionOutDTO longestStayed = transactionService.longestStayedTime();

        assertEquals(null, longestStayed);
    }


    @Test
    public void testCalculateAverageStayedTime() throws Exception {
        List<Transaction> vehicles = new ArrayList<>();
        vehicles.add(new Transaction("ABC123",1, 120.0));
        vehicles.add(new Transaction("XAP512",2, 180.0));
        vehicles.add(new Transaction("LDSR123", 2, 150.0));
        vehicles.add(new Transaction("AFQ32F",1, 90.0));

        when( (List<Transaction>) parkingmanagerRepository.findAll()).thenReturn(vehicles);

        List<TransactionOutDTO> average =  transactionService.averageStayedTime();

        assertEquals("automovil", average.get(0).getVehicleType());
        assertEquals("motocicleta", average.get(1).getVehicleType());
        assertEquals(105.0, average.get(0).getAvgStayedTime(), 0.001);   // 120 + 90 / 2 = 105
        assertEquals(165.0, average.get(1).getAvgStayedTime(), 0.001); // 180 + 150 / 2 = 165
    }

    @Test
    public void testCalculateAverageStayedTimeEmptyList() throws Exception {
        List<Transaction> vehicles = new ArrayList<>();

        List<TransactionOutDTO> expected = new ArrayList<>();

        when( (List<Transaction>) parkingmanagerRepository.findAll()).thenReturn(vehicles);

        List<TransactionOutDTO> average = transactionService.averageStayedTime();

        assertEquals(expected, average);
    }

    @Test
    public void testGetVehicleTypeName() {
        EnumVehicleType car = EnumVehicleType.AUTOMOVIL;
        EnumVehicleType truck = EnumVehicleType.MOTOCICLETA;
        EnumVehicleType motorcycle = EnumVehicleType.CAMION;

        assertEquals(EnumVehicleType.AUTOMOVIL.getName(), transactionService.getVehicleTypeName(car.getId()));
        assertEquals(EnumVehicleType.MOTOCICLETA.getName(), transactionService.getVehicleTypeName(truck.getId()));
        assertEquals(EnumVehicleType.CAMION.getName(), transactionService.getVehicleTypeName(motorcycle.getId()));
    }

    @Test
    public void testGetVehicleTypeNameUnknown() {
        int unknownId = 999; // An ID that doesn't match any vehicle type

        assertEquals("Unknown", transactionService.getVehicleTypeName(unknownId));
    }


    @Test
    public void testGetStayedTime() {

        Date beginDate = new Date(1631400000000L); // September 12, 2021, 12:00:00 (GMT)
        Date finishDate = new Date(1631407200000L); // September 12, 2021, 14:00:00 (GMT)

        Double stayedTime = transactionService.getNewStayedTime(beginDate, finishDate);

        assertEquals(120, stayedTime); // 120 minutes
    }

    @Test
    public void testGetStayedTimeSameDates() {

        Date beginDate = new Date(1631400000000L); // September 12, 2021, 12:00:00 (GMT)

        Double stayedTime = transactionService.getNewStayedTime(beginDate, beginDate);

        assertEquals(0, stayedTime); // 0 minutes
    }

    @Test
    public void testSendNewParkingTransactions() {
        MockitoAnnotations.initMocks(this);

        TransactionInDTO transactionInDTO = new TransactionInDTO(/* initialize with data */);

        transactionService.sendNewParkingTransactions(transactionInDTO);

        verify(mockSqsService, times(1))
                .sendMessage(anyString(), eq(Constants.NEW_TRANSACTION_PARKING_QUEUE));
    }

    @Test
    public void testSendFinishedParkingTransactions() {
        MockitoAnnotations.initMocks(this);

        TransactionInDTO transactionInDTO = new TransactionInDTO();

        transactionService.sendFinishedParkingTransactions(transactionInDTO);

        verify(mockSqsService, times(1))
                .sendMessage(anyString(), eq(Constants.FINISH_TRANSACTION_PARKING_QUEUE));
    }


    @Test
    public void testFinishParkingTransactions() {
        List<Message> messages = new ArrayList<>();

        // Create a valid JSON string representing a TransactionDTO
        String json = "{\"plate\": \"ABC123\"}";

        Message mockMessage = mock(Message.class);
        when(mockMessage.getBody()).thenReturn(json); // Use the valid JSON string
        messages.add(mockMessage);

        // Configure parkingmanagerRepository mock behavior
        when(parkingmanagerRepository.findByPlateAndStatus(anyString(), anyInt())).thenReturn(null);
        when(parkingmanagerRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        // Call the method being tested
        List<Message> messagesToDelete = transactionService.finishParkingTransactions(messages);

        // Verify that methods were called and correct behavior happened
        verify(parkingmanagerRepository, times(1)).save(any(Transaction.class));

        // Verify that the returned list is correct (if applicable)
        // assertEquals(expectedMessagesToDelete, messagesToDelete);
    }

    @Test
    public void testNewParkingTransactions() {
        List<Message> messages = new ArrayList<>();

        // Create a valid JSON string representing a TransactionDTO
        String json = "{\"plate\": \"ABC123\", \"vehicleType\": 1}";

        Message mockMessage = mock(Message.class);
        when(mockMessage.getBody()).thenReturn(json); // Use the valid JSON string
        messages.add(mockMessage);

        // Configure parkingmanagerRepository mock behavior
        when(parkingmanagerRepository.findByPlateAndStatus(anyString(), anyInt())).thenReturn(null);
        when(parkingmanagerRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        // Call the method being tested
        List<Message> messagesToDelete = transactionService.newParkingTransactions(messages);

        // Verify that methods were called and correct behavior happened
        verify(parkingmanagerRepository, times(1)).save(any(Transaction.class));

        // Verify that the returned list is correct (if applicable)
        // assertEquals(expectedMessagesToDelete, messagesToDelete);
    }



    @Test
    public void testNewParkingTransactionsExistingTransaction() {
        List<Message> messages = new ArrayList<>();
        // Add mock Message instances to the list

        TransactionDTO transactionDTO = new TransactionDTO(/* initialize with data */);
        Message mockMessage = mock(Message.class);
        when(mockMessage.getBody()).thenReturn("mock JSON string");
        when(parkingmanagerRepository.findByPlateAndStatus(anyString(), anyInt())).thenReturn(new Transaction());

        // Call the method being tested
        List<Message> messagesToDelete = transactionService.newParkingTransactions(messages);

        // Verify that methods were called and correct behavior happened
        verify(parkingmanagerRepository, never()).save(any(Transaction.class));


        // Verify that the returned list is correct (if applicable)
        // assertEquals(expectedMessagesToDelete, messagesToDelete);
    }

    @Test
    public void testMessageToTransactionDTO() {


        TransactionDTO transactionDTO = new TransactionDTO(/* initialize with data */);
        transactionDTO.setPlate("abc123");
        transactionDTO.setVehicleType(EnumVehicleType.AUTOMOVIL.getId());

        Transaction transaction = transactionService.messageToTransactionDTO(transactionDTO);

        assertNotNull(transaction);
        assertEquals("ABC123", transaction.getPlate()); // Assuming .toUpperCase() is applied
        assertEquals(EnumVehicleType.AUTOMOVIL.getId(), transaction.getVehicleType());
        assertEquals(EnumTransactionStatus.STARTED.getId(), transaction.getStatus());
        assertNotNull(transaction.getBeginDate());
        assertNotNull(transaction.getTransactionId());
        assertNotNull(transaction.getStayedTime());
    }

}
