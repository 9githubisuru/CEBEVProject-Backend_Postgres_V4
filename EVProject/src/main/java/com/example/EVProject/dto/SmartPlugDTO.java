package com.example.EVProject.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class SmartPlugDTO {

    private String deviceId;
    private String cebSerialNo;
    private Double maximumOutput;
    private Integer stationId;

    // getters and setters

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getCebSerialNo() {
        return cebSerialNo;
    }

    public void setCebSerialNo(String cebSerialNo) {
        this.cebSerialNo = cebSerialNo;
    }

    public Double getMaximumOutput() {
        return maximumOutput;
    }

    public void setMaximumOutput(Double maximumOutput) {
        this.maximumOutput = maximumOutput;
    }

    public Integer getStationId() {
        return stationId;
    }

    public void setStationId(Integer stationId) {
        this.stationId = stationId;
    }
}
