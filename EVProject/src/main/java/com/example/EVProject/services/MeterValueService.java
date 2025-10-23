package com.example.EVProject.services;

import com.example.EVProject.dto.MeterValueResponse;
import com.example.EVProject.model.*;
import com.example.EVProject.dto.MeterValueRequest;
import com.example.EVProject.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeterValueService {

    private final MeterValueRepository meterRepo;
    private final SampledValueRepository sampleRepo;
    private final ChargingSessionRepository sessionRepo;
    private SmartPlug station;

    @Transactional
    public void saveMeterValues(MeterValueRequest request) {
        ChargingSession session = sessionRepo.findById(Math.toIntExact(request.getTransactionId().longValue()))
                .orElseThrow(() -> new RuntimeException("Session not found: " + request.getTransactionId()));

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        for (MeterValueRequest.MeterReading reading : request.getMeterValue()) {
            var ref = new Object() {
                MeterValue meterValue = MeterValue.builder()
                        .connectorId(request.getConnectorId())
                        .timestamp(LocalDateTime.parse(reading.getTimestamp(), formatter))
                        .session(session)
                        .build();
            };

            ref.meterValue = meterRepo.save(ref.meterValue);

            var samples = reading.getSampledValue().stream().map(s -> SampledValue.builder()
                    .value(Double.parseDouble(s.getValue()))
                    .context(s.getContext())
                    .format(s.getFormat())
                    .measurand(s.getMeasurand())
                    .location(s.getLocation())
                    .unit(s.getUnit())
                    .meterValue(ref.meterValue)
                    .build()).collect(Collectors.toList());

            sampleRepo.saveAll(samples);
        }
    }

    @Transactional
    public List<MeterValueResponse> getMeterValuesBySession(Long sessionId) {
        List<MeterValue> meterValues = meterRepo.findAllBySession_SessionId(sessionId);

        return meterValues.stream().map(mv -> {
            MeterValueResponse dto = new MeterValueResponse();
            dto.setMeterId(mv.getMeterId());
            dto.setConnectorId(mv.getConnectorId());
            dto.setTimestamp(mv.getTimestamp());

            // Get deviceId and stationId from linked session → smartPlug → station
            if (mv.getSession() != null && mv.getSession().getSmartPlug() != null) {
                dto.setDeviceId(mv.getSession().getSmartPlug().getDeviceId());
                dto.setStationId(mv.getSession().getSmartPlug().getStation(station) != null
                        ? mv.getSession().getSmartPlug().getStation(station).getStationId()
                        : null);
            }

            // Map sampled values
            List<MeterValueResponse.SampleData> samples = mv.getSampledValues().stream().map(s -> {
                MeterValueResponse.SampleData sd = new MeterValueResponse.SampleData();
                sd.setValue(s.getValue());
                sd.setContext(s.getContext());
                sd.setFormat(s.getFormat());
                sd.setMeasurand(s.getMeasurand());
                sd.setLocation(s.getLocation());
                sd.setUnit(s.getUnit());
                return sd;
            }).toList();

            dto.setSampledValues(samples);
            return dto;
        }).toList();
    }

}
