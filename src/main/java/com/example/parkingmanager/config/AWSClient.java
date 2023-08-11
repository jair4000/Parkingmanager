package com.example.parkingmanager.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.example.parkingmanager.constants.Constants;
import org.springframework.stereotype.Component;

@Component
public class AWSClient {
    public static final String REGION = Regions.US_EAST_1.getName();
    public static  String NEW_TRANSACTION_PARKING_QUEUE;
    public static String FINISH_TRANSACTION_PARKING_QUEUE;
    private final AwsProperties awsProperties;
    private AWSStaticCredentialsProvider awsStaticCredentialsProvider;

    public AWSClient(ApplicationProperties applicationProperties, AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
        NEW_TRANSACTION_PARKING_QUEUE = String.format("%s%s", "", Constants.NEW_TRANSACTION_PARKING_QUEUE);
        FINISH_TRANSACTION_PARKING_QUEUE = String.format("%s%s", "", Constants.FINISH_TRANSACTION_PARKING_QUEUE);

    }

    public AWSStaticCredentialsProvider getAWSCredentials() {
        if(awsStaticCredentialsProvider == null) {
            BasicAWSCredentials awsCreeds = new BasicAWSCredentials(awsProperties.getAccessKey(), awsProperties.getSecretKey());
            awsStaticCredentialsProvider = new AWSStaticCredentialsProvider(awsCreeds);
        }
        return awsStaticCredentialsProvider;
    }
}
