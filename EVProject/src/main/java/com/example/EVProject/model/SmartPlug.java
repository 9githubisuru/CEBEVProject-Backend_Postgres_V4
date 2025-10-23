package com.example.EVProject.model;

import jakarta.persistence.*;

@Entity
@Table(name = "smart_plug")
public class SmartPlug {

    @Id
    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "ceb_serial_no")
    private String cebSerialNo;

    @Column(name = "maximum_output")
    private Double maximumOutput;

    @Column(name = "station_id")
    private Integer stationId;

    @ManyToOne
    @JoinColumn(name = "station_id", referencedColumnName = "station_id", insertable = false, updatable = false)
    private ChargingStation chargingStation;

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

    public ChargingStation getChargingStation() {
        return chargingStation;
    }

    public void setChargingStation(ChargingStation chargingStation) {
        this.chargingStation = chargingStation;
    }

    public SmartPlug getStation(SmartPlug station) {
        return station;
    }
}

