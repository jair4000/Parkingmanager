package com.example.parkingmanager.dto;

import com.example.parkingmanager.enums.EnumTransactionStatus;
import com.example.parkingmanager.enums.EnumVehicleType;
import jakarta.validation.constraints.NotNull;

public class TransactionInDTO {

    @NotNull
    private String plate;
    @NotNull
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
