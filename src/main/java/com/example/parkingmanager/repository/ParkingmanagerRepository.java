package com.example.parkingmanager.repository;

import com.example.parkingmanager.model.Transaction;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@EnableScan
@Repository
public interface ParkingmanagerRepository extends CrudRepository<Transaction, String> {
    Transaction findByPlateAndStatus(String plate, Integer status);

}
