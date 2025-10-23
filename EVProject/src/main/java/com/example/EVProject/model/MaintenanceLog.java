package com.example.EVProject.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_logs")
public class MaintenanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maintenance_id")
    private Integer maintenanceId;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "charger_health")
    private String chargerHealth;

    @Column(name = "anomaly_detected")
    private String anomalyDetected;

    @Column(name = "anomaly_description", columnDefinition = "text")
    private String anomalyDescription;

    @Column(name = "device_id")
    private String deviceId;

    @ManyToOne
    @JoinColumn(name = "device_id", referencedColumnName = "device_id", insertable = false, updatable = false)
    private SmartPlug smartPlug;

    // getters and setters

    public Integer getMaintenanceId() {
        return maintenanceId;
    }

    public void setMaintenanceId(Integer maintenanceId) {
        this.maintenanceId = maintenanceId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getChargerHealth() {
        return chargerHealth;
    }

    public void setChargerHealth(String chargerHealth) {
        this.chargerHealth = chargerHealth;
    }

    public String getAnomalyDetected() {
        return anomalyDetected;
    }

    public void setAnomalyDetected(String anomalyDetected) {
        this.anomalyDetected = anomalyDetected;
    }

    public String getAnomalyDescription() {
        return anomalyDescription;
    }

    public void setAnomalyDescription(String anomalyDescription) {
        this.anomalyDescription = anomalyDescription;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public SmartPlug getSmartPlug() {
        return smartPlug;
    }

    public void setSmartPlug(SmartPlug smartPlug) {
        this.smartPlug = smartPlug;
    }
}

