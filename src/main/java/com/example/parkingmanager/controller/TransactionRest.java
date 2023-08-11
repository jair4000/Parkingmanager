package com.example.parkingmanager.controller;

import com.example.parkingmanager.constants.Constants;
import com.example.parkingmanager.dto.Response;
import com.example.parkingmanager.dto.TransactionInDTO;
import com.example.parkingmanager.dto.TransactionOutDTO;
import com.example.parkingmanager.service.TransactionService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TransactionRest {

    @Autowired
    TransactionService transactionService;

    @PostMapping("/newParking")
    public Response newParking(@RequestBody @Valid TransactionInDTO transactionInDTO)  {
        try {
            transactionService.sendNewParkingTransactions(transactionInDTO);
            return new Response(200, Constants.REQUEST_SUCCESS_MESSAGE);
        } catch (Exception e) {
            return new Response(500,"Ocurrio un error",e.getMessage());
        }
    }

    @PostMapping("/finishParking")
    public Response finishParking(@RequestBody @Valid TransactionInDTO transactionInDTO) {
        try {
            transactionService.sendFinishedParkingTransactions(transactionInDTO);
            return new Response(200, Constants.REQUEST_SUCCESS_MESSAGE);
        } catch (Exception e) {
            return new Response(500,"Ocurrio un error",e.getMessage());
        }
    }

    @PostMapping("/getAverageStayedTime")
    public Response<List<TransactionOutDTO>> getAverageStayedTime() {
        try {
            List<TransactionOutDTO> transactionOutDTO = transactionService.averageStayedTime();
            return new Response(transactionOutDTO);
        } catch (Exception e) {
            return new Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Ocurrio un error",e.getMessage());
        }
    }

    @PostMapping("/getLongestStayedTime")
    public Response<TransactionOutDTO> getLongestStayedTime() {
        try {
            TransactionOutDTO transactionOutDTO =  transactionService.longestStayedTime();
            return new Response(transactionOutDTO);
        } catch (Exception e) {
            return new Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Ocurrio un error",e.getMessage());
        }
    }



}
