package com.example.EVProject.controllers;

import com.example.EVProject.dto.MeterValueRequest;
import com.example.EVProject.dto.MeterValueResponse;
import com.example.EVProject.services.MeterValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meter-values")
@RequiredArgsConstructor
public class MeterValueController {

    private final MeterValueService meterService;

    @PostMapping
    public String saveMeterValues(@RequestBody MeterValueRequest request) {
        meterService.saveMeterValues(request);
        return "Meter values saved successfully!";
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<List<MeterValueResponse>> getMeterValuesBySession(@PathVariable Long sessionId) {
        List<MeterValueResponse> response = meterService.getMeterValuesBySession(sessionId);
        return ResponseEntity.ok(response);
    }


}
