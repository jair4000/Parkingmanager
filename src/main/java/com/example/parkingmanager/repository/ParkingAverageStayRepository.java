package com.example.parkingmanager.repository;

import com.example.parkingmanager.model.AverageParking;
import com.example.parkingmanager.model.LongestStayParking;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@EnableScan
@Repository
public interface ParkingAverageStayRepository extends CrudRepository<AverageParking, String> {
}
