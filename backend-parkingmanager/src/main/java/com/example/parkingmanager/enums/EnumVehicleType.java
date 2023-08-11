package com.example.parkingmanager.enums;

public enum EnumVehicleType {

    AUTOMOVIL(1 , "automovil"),

    MOTOCICLETA (2, "motocicleta"),

    CAMION(3, "camion"),

    OTRO(4, "otro");

    private final Integer id;

    private final String name;

    EnumVehicleType(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
