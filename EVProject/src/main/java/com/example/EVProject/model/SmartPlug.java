package com.example.EVProject.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "smart_plug")
public class SmartPlug {

    @Id
    @Column(name = "id_device")
    private String idDevice;

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

    public SmartPlug getStation(SmartPlug station) {
        return station;
    }
}

