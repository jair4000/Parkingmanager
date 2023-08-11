package com.example.parkingmanager.enums;

public enum EnumTransactionStatus {

    STARTED(1 , "iniciada"),

    FINISHED (2, "finalizada"),

    CANCELED(3, "cancelado");


    private final Integer id;

    private final String name;

    EnumTransactionStatus(Integer id, String name) {
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
