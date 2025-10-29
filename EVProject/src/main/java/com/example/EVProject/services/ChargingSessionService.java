package com.example.EVProject.services;

import com.example.EVProject.dto.ChargingSessionDTO;
import com.example.EVProject.dto.SolarOwnerConsumptionDTO;
import com.example.EVProject.model.ChargingSession;
import com.example.EVProject.repositories.ChargingSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChargingSessionService {

    @Autowired
    private ChargingSessionRepository repository;

    public List<ChargingSessionDTO> getAllSessions() {
        // line 14: make sure you call stream() properly
        return repository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ChargingSessionDTO getSessionById(Integer id) {
        // line 30: make sure orElse is called on Optional
        return repository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    public ChargingSessionDTO saveSession(ChargingSessionDTO dto) {
        ChargingSession session = convertToEntity(dto);
        ChargingSession saved = repository.save(session);
        return convertToDto(saved);
    }

    public void deleteSession(Integer id) {
        repository.deleteById(id);
    }

    public List<ChargingSessionDTO> getSessionsBySolarOwnerId(Integer ownerId) {
        return repository.findBySolarOwnerId(ownerId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    private ChargingSessionDTO convertToDto(ChargingSession session) {
        ChargingSessionDTO dto = new ChargingSessionDTO();
        dto.setSessionId(session.getSessionId());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setSoc(session.getSoc());
        dto.setChargingMode(session.getChargingMode());
        dto.setTotalConsumption(session.getTotalConsumption());
        dto.setAmount(session.getAmount());
        dto.setIdDevice(session.getIdDevice());
        return dto;
    }

    private ChargingSession convertToEntity(ChargingSessionDTO dto) {
        ChargingSession session = new ChargingSession();
        session.setSessionId(dto.getSessionId());
        session.setStartTime(dto.getStartTime());
        session.setEndTime(dto.getEndTime());
        session.setSoc(dto.getSoc());
        session.setChargingMode(dto.getChargingMode());
        session.setTotalConsumption(dto.getTotalConsumption());
        session.setAmount(dto.getAmount());
        session.setIdDevice(dto.getIdDevice());
        return session;
    }

    public Double getTotalConsumptionBySolarOwner(Integer ownerId) {
        return repository.getTotalConsumptionBySolarOwner(ownerId);
    }

    public SolarOwnerConsumptionDTO getMonthlyConsumptionByOwner(Integer ownerId) {
        return repository.getMonthlyConsumptionByOwner(ownerId);
    }


    public List<ChargingSessionDTO> getSessionsForCurrentMonth() {
        return repository.findSessionsForCurrentMonth()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


}
