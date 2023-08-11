package com.example.parkingmanager;

import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan ({"com.example.parkingmanager.*"})
//@EnableDynamoDBRepositories({"com.example.parkingmanager.repository"})
//@ComponentScan({ "com.example.*" })
public class ParkingmanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParkingmanagerApplication.class, args);
	}

}
