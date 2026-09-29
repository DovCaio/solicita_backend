package com.solicita.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.solicita.dto.request.CreateRequestDTO;
import com.solicita.dto.request.RequestFilterDTO;
import com.solicita.dto.request.RequestResponseDTO;
import com.solicita.dto.request.UpdateRequestDTO;
import com.solicita.dto.request.UpdateRequestStatusDTO;
import com.solicita.enums.Category;
import com.solicita.enums.RequestStatus;
import com.solicita.service.RequestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/requests")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ResponseEntity<RequestResponseDTO> create(
            @Valid @RequestBody CreateRequestDTO dto,
            Authentication authentication) {
        RequestResponseDTO response = requestService.create(
                dto,
                authentication.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<RequestResponseDTO>> findAll(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        RequestFilterDTO filter = new RequestFilterDTO(
                title,
                category,
                status,
                startDate,
                endDate);

        return ResponseEntity.ok(
                requestService.findAll(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequestResponseDTO> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(requestService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RequestResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequestDTO dto) {
        return ResponseEntity.ok(requestService.update(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RequestResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequestStatusDTO dto) {
        return ResponseEntity.ok(requestService.updateStatus(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        requestService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
