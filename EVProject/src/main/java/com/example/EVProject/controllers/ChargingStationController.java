package com.example.EVProject.controllers;

import com.example.EVProject.dto.ChargingStationDTO;
import com.example.EVProject.dto.ChargingStationStatusUpdate;
import com.example.EVProject.model.IdTagInfo;
import com.example.EVProject.model.OcppMessageLog;
import com.example.EVProject.model.SmartPlug;
import com.example.EVProject.repositories.IdTagInfoRepository;
import com.example.EVProject.repositories.OcppMessageLogRepository;
import com.example.EVProject.repositories.SmartPlugRepository;
import com.example.EVProject.services.ChargingStationService;
import com.example.EVProject.utils.IdDeviceValidator;
import com.example.EVProject.utils.OcppMessageParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/charging-stations")
public class ChargingStationController {

    @Autowired
    private ChargingStationService service;

    @Autowired
    private IdDeviceValidator idDeviceValidator;

    @Autowired
    private OcppMessageLogRepository messageLogRepo;

    @Autowired
    private SmartPlugRepository smartPlugRepository;

    @Autowired
    private IdTagInfoRepository idTagInfoRepository;

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

//    @PostMapping("/status")
//    public ResponseEntity<?> updateChargingStationStatus(
//            @RequestBody ChargingStationStatusUpdate request,
//            @RequestHeader("USER") String user,
//            @RequestHeader("DIGEST") String digest) {
//
//        try {
//            // Basic header-based authentication
//            if (!"test".equals(user) || !"test123".equals(digest)) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
//                        "resultCode", 400,
//                        "resultDesc", "AUTHENTICATION_FAILURE"
//                ));
//            }
//
//            // Perform the update
//            //service.updateChargingStationStatus(request.getStationId(), request.getStatus(), request.getErrorCode(),request.getTimestamp());
//            Map<String, Object> response = service.updateChargingStationStatus(
//                    request.getStationId(),
//                    request.getStatus(),
//                    request.getErrorCode(),
//                    request.getTimestamp()
//            );
//
//            return ResponseEntity.ok(response);
//
////            return ResponseEntity.ok(Map.of(
////                    "resultCode", 200,
////                    "resultDesc", "SUCCESS"
////            ));
//
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
//                    "resultCode", 400,
//                    "resultDesc", e.getMessage()
//            ));
//        } catch (EntityNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
//                    "resultCode", 404,
//                    "resultDesc", "CHARGING_STATION_NOT_FOUND"
//            ));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                    "resultCode", 500,
//                    "resultDesc", "INTERNAL_SERVER_ERROR"
//            ));
//        }
//    }

    @PostMapping("/status")
    public ResponseEntity<?> updateChargingStationStatus(
            @RequestBody String rawBody,
            @RequestHeader("USER") String user,
            @RequestHeader("DIGEST") String digest,
            @RequestHeader("IdDevice") String idDevice) {

        LocalDateTime receivedTime = LocalDateTime.now();
        OcppMessageLog logEntry = new OcppMessageLog();

        try {
            // 1️⃣ Header authentication (existing)
            if (!"test".equals(user) || !"test123".equals(digest)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("error", "AUTHENTICATION_FAILURE"));
            }

            // 2️⃣ Validate IdDevice against DB
            idDeviceValidator.validate(idDevice);

            // 3️⃣ Parse OCPP message structure
            var parsed = OcppMessageParser.parse(rawBody);
            if (!"StatusNotification".equalsIgnoreCase(parsed.action())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        Map.of("error", "Invalid action type"));
            }

            // 4️⃣ Extract payload
            var payload = parsed.payload();
            Integer connectorId = payload.get("connectorId").asInt();
            String errorCode = payload.get("errorCode").asText();
            String status = payload.get("status").asText();
            String timestampStr = payload.get("timestamp").asText();
            LocalDateTime timestamp = LocalDateTime.parse(timestampStr.replace("Z", ""));

            // Save log (incoming)
            logEntry.setIdDevice(idDevice);
            logEntry.setMessageId(parsed.messageId());
            logEntry.setAction(parsed.action());
            logEntry.setMessageTypeId(parsed.messageTypeId());
            logEntry.setPayload(payload.toString());
            logEntry.setReceivedAt(receivedTime);

            // 5️⃣ Update via existing service
            Integer statusCode = mapStatusStringToCode(status);
            service.updateChargingStationStatus(connectorId, statusCode, errorCode, timestamp);

            // 6️⃣ Echo messageId in response (OCPP .conf)
            Object[] ocppResponse = new Object[]{
                    3,
                    parsed.messageId(),
                    new HashMap<>() // empty payload
            };

            // Save response log
            logEntry.setResponse(new ObjectMapper().writeValueAsString(ocppResponse));
            logEntry.setRespondedAt(LocalDateTime.now());
            messageLogRepo.save(logEntry);

            return ResponseEntity.ok(ocppResponse);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            logEntry.setResponse("{\"error\": \"" + e.getMessage() + "\"}");
            logEntry.setRespondedAt(LocalDateTime.now());
            messageLogRepo.save(logEntry);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "INTERNAL_SERVER_ERROR"));
        }
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<?> handleHeartbeat(
            @RequestBody String rawBody,
            @RequestHeader("IdDevice") String idDevice) {

        LocalDateTime receivedTime = LocalDateTime.now();

        try {
            // 1️⃣ Validate headers
//            if (!"test".equals(user) || !"test123".equals(digest)) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(Map.of("error", "AUTHENTICATION_FAILURE"));
//            }

            // 2️⃣ Validate IdDevice in smart_plug table
            idDeviceValidator.validate(idDevice);

            // 3️⃣ Parse OCPP structure
            var parsed = OcppMessageParser.parse(rawBody);

            // Validate message type and action
            if (parsed.messageTypeId() != 2 || !"Heartbeat".equalsIgnoreCase(parsed.action())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid message type or action. Expected Heartbeat CALL"));
            }

            // 4️⃣ Build Heartbeat.conf response according to doc
            Map<String, Object> payload = Map.of(
                    "currentTime", java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).toString()
            );

            Object[] ocppResponse = new Object[]{
                    3,
                    parsed.messageId(),
                    payload
            };

            // 5️⃣ Optional logging (if using ocpp_message_log)
            if (messageLogRepo != null) {
                OcppMessageLog log = new OcppMessageLog();
                log.setIdDevice(idDevice);
                log.setMessageId(parsed.messageId());
                log.setAction(parsed.action());
                log.setMessageTypeId(parsed.messageTypeId());
                log.setPayload(parsed.payload().toString());
                log.setResponse(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(ocppResponse));
                log.setReceivedAt(receivedTime);
                log.setRespondedAt(LocalDateTime.now());
                messageLogRepo.save(log);
            }

            // 6️⃣ Return response
            return ResponseEntity.ok(ocppResponse);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_SERVER_ERROR"));
        }
    }

    @PostMapping("/authorize")
    public ResponseEntity<?> handleAuthorize(
            @RequestBody String rawBody,
            @RequestHeader("IdDevice") String headerIdDevice) {

        LocalDateTime receivedTime = LocalDateTime.now();

        try {

            // 2️⃣ Validate IdDevice using your util class
            idDeviceValidator.validate(headerIdDevice);

            // 3️⃣ Parse OCPP-like message
            var parsed = OcppMessageParser.parse(rawBody);
            if (!"Authorize".equalsIgnoreCase(parsed.action())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid action type. Expected 'Authorize'"));
            }

            // 4️⃣ Extract IdDevice from request payload
            String bodyIdDevice = parsed.payload().has("IdDevice")
                    ? parsed.payload().get("IdDevice").asText()
                    : null;

            if (bodyIdDevice == null || bodyIdDevice.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Missing IdDevice in payload"));
            }

            // 5️⃣ Compare header and body IdDevice (must match)
            if (!headerIdDevice.equals(bodyIdDevice)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Header and payload IdDevice mismatch"));
            }

            // 6️⃣ Validate the IdDevice in the payload (redundant but explicit)
            idDeviceValidator.validate(bodyIdDevice);

            // 4️⃣ Check if IdTag already exists for this IdDevice
            Optional<IdTagInfo> existingTagOpt = idTagInfoRepository.findByIdDevice(bodyIdDevice);
            IdTagInfo tagRecord;
            String idTag;
            LocalDateTime expiryDate;

            if (existingTagOpt.isPresent() && existingTagOpt.get().getExpiryDate().isAfter(LocalDateTime.now())) {
                // ✅ Reuse existing IdTag if still valid
                tagRecord = existingTagOpt.get();
                idTag = tagRecord.getIdTag();
                expiryDate = tagRecord.getExpiryDate();
            } else {

                // Fetch SmartPlug details (for cebSerialNo or username)
                SmartPlug plug = smartPlugRepository.findById(bodyIdDevice)
                        .orElseThrow(() -> new IllegalArgumentException("IdDevice not found: " + bodyIdDevice));

                idDeviceValidator.validate(plug.getIdDevice());

                String accountReference = (plug.getCebSerialNo() != null)
                        ? plug.getCebSerialNo()
                        : plug.getIdDevice();

                idTag = generateIdTag(accountReference);
                expiryDate = LocalDateTime.of(2025, 12, 31, 23, 59, 59);

                tagRecord = new IdTagInfo();
                tagRecord.setIdDevice(bodyIdDevice);
                tagRecord.setIdTag(idTag);
                tagRecord.setStatus("Accepted");
                tagRecord.setExpiryDate(expiryDate);
                idTagInfoRepository.save(tagRecord);
            }

            // 5️⃣ Build OCPP response according to documentation
            Map<String, Object> idTagInfo = Map.of(
                    "status", "Accepted",
                    "expiryDate", expiryDate.toString() + "Z",
                    "IdTag", idTag
            );

            Map<String, Object> payload = Map.of("idTagInfo", idTagInfo);
            Object[] ocppResponse = new Object[]{
                    3,
                    parsed.messageId(),
                    payload
            };

            // 6️⃣ Optional logging (for debugging/traceability)
            if (messageLogRepo != null) {
                OcppMessageLog log = new OcppMessageLog();
                log.setIdDevice(bodyIdDevice);
                log.setMessageId(parsed.messageId());
                log.setAction(parsed.action());
                log.setMessageTypeId(parsed.messageTypeId());
                log.setPayload(parsed.payload().toString());
                log.setResponse(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(ocppResponse));
                log.setReceivedAt(receivedTime);
                log.setRespondedAt(LocalDateTime.now());
                messageLogRepo.save(log);
            }

            return ResponseEntity.ok(ocppResponse);

        } catch (IllegalArgumentException e) {
            Map<String, Object> idTagInfo = Map.of("status", "Invalid");
            Map<String, Object> payload = Map.of("idTagInfo", idTagInfo);
            Object[] ocppResponse = new Object[]{3, "AUTH-REQ-FAILED", payload};
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ocppResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_SERVER_ERROR"));
        }
    }

    private String generateIdTag(String baseValue) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((baseValue + System.currentTimeMillis()).getBytes());
            String hex = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            return "IDT-" + hex.substring(0, 8).toUpperCase(); // e.g. IDT-8BC9F2C1
        } catch (Exception e) {
            throw new RuntimeException("Error generating IdTag", e);
        }
    }

    private Integer mapStatusStringToCode(String status) {
        return switch (status) {
            case "Available" -> 1;
            case "Preparing" -> 2;
            case "Charging" -> 3;
            case "Finishing" -> 4;
            case "Unavailable" -> 5;
            default -> throw new IllegalArgumentException("Unknown status: " + status);
        };
    }

}
