package com.example.parkingmanager.dto;

public class TransactionOutDTO {

    private String plate;

    private String vehicleType;

    private Double avgStayedTime;

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public Double getAvgStayedTime() {
        return avgStayedTime;
    }

    public void setAvgStayedTime(Double avgStayedTime) {
        this.avgStayedTime = avgStayedTime;
    }
}
