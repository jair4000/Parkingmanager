package com.example.parkingmanager.dto;

import java.util.Date;

public class TransactionDTO {


    private String transactionId;
    private Date beginDate;
    private String plate;
    private Date finishDate;
    private Integer vehicleType;
    private Integer status;
    private Long stayedTime;

    public Long getStayedTime() {
        return stayedTime;
    }

    public void setStayedTime(Long stayedTime) {
        this.stayedTime = stayedTime;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public Integer getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(Integer vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
