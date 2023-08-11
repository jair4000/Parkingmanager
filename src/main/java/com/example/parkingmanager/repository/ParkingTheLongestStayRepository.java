package com.example.parkingmanager.repository;

import com.example.parkingmanager.model.LongestStayParking;
import com.example.parkingmanager.model.Transaction;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@EnableScan
@Repository
public interface ParkingTheLongestStayRepository extends CrudRepository<LongestStayParking, String> {
}
