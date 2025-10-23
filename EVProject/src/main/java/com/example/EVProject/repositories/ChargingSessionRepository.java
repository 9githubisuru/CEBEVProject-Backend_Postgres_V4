package com.example.EVProject.repositories;

import com.example.EVProject.dto.SolarOwnerConsumptionDTO;
import com.example.EVProject.model.ChargingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChargingSessionRepository extends JpaRepository<ChargingSession, Integer> {

    @Query("select s from ChargingSession s " +
            "join s.smartPlug sp " +
            "join sp.chargingStation cs " +
            "where cs.solarOwnerId = :ownerId")
    List<ChargingSession> findBySolarOwnerId(@Param("ownerId") Integer ownerId);

    @Query("select sum(s.totalConsumption) from ChargingSession s " +
            "join s.smartPlug sp " +
            "join sp.chargingStation cs " +
            "where cs.solarOwnerId = :ownerId")
    Double getTotalConsumptionBySolarOwner(@Param("ownerId") Integer ownerId);

    @Query("select new com.example.EVProject.dto.SolarOwnerConsumptionDTO(o.username, sum(s.totalConsumption)) " +
            "from ChargingSession s " +
            "join s.smartPlug sp " +
            "join sp.chargingStation cs " +
            "join cs.solarOwner o " +
            "where o.solarOwnerId = :ownerId " +
            "and extract(month from s.startTime) = extract(month from current_date) " +
            "and extract(year from s.startTime) = extract(year from current_date) " +
            "group by o.username")
    SolarOwnerConsumptionDTO getMonthlyConsumptionByOwner(@Param("ownerId") Integer ownerId);

    @Query("select s from ChargingSession s " +
            "where extract(month from s.startTime) = extract(month from current_date) " +
            "and extract(year from s.startTime) = extract(year from current_date)")
    List<ChargingSession> findSessionsForCurrentMonth();

}
