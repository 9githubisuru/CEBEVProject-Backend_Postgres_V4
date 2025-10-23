package com.example.EVProject.controllers;

import com.example.EVProject.dto.ChargingStationDTO;
import com.example.EVProject.dto.ChargingStationStatusUpdate;
import com.example.EVProject.services.ChargingStationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/charging-stations")
public class ChargingStationController {

    @Autowired
    private ChargingStationService service;

    @GetMapping
    public List<ChargingStationDTO> getAllStations() {
        return service.getAllStations();
    }

    @GetMapping("/{id}")
    public ChargingStationDTO getStationById(@PathVariable Integer id) {
        return service.getStationById(id);
    }

    @PostMapping
    public ChargingStationDTO saveStation(@RequestBody ChargingStationDTO dto) {
        return service.saveStation(dto);
    }

    @DeleteMapping("/{id}")
    public void deleteStation(@PathVariable Integer id) {
        service.deleteStation(id);
    }

    @PostMapping("/status")
    public ResponseEntity<?> updateChargingStationStatus(
            @RequestBody ChargingStationStatusUpdate request,
            @RequestHeader("USER") String user,
            @RequestHeader("DIGEST") String digest) {

        try {
            // Basic header-based authentication
            if (!"test".equals(user) || !"test123".equals(digest)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "resultCode", 400,
                        "resultDesc", "AUTHENTICATION_FAILURE"
                ));
            }

            // Perform the update
            //service.updateChargingStationStatus(request.getStationId(), request.getStatus(), request.getErrorCode(),request.getTimestamp());
            Map<String, Object> response = service.updateChargingStationStatus(
                    request.getStationId(),
                    request.getStatus(),
                    request.getErrorCode(),
                    request.getTimestamp()
            );

            return ResponseEntity.ok(response);

//            return ResponseEntity.ok(Map.of(
//                    "resultCode", 200,
//                    "resultDesc", "SUCCESS"
//            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "resultCode", 400,
                    "resultDesc", e.getMessage()
            ));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "resultCode", 404,
                    "resultDesc", "CHARGING_STATION_NOT_FOUND"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "resultCode", 500,
                    "resultDesc", "INTERNAL_SERVER_ERROR"
            ));
        }
    }
}
