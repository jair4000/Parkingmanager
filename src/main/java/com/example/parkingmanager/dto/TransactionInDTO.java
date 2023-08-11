package com.example.parkingmanager.dto;

import com.example.parkingmanager.enums.EnumTransactionStatus;
import com.example.parkingmanager.enums.EnumVehicleType;

public class TransactionInDTO {

    private String plate;

    private Integer vehicleType;

    public Integer getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(Integer vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }


}
