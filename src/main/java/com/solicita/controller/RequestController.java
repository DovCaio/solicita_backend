package com.solicita.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/requests")
@Tag(name = "Solicitações", description = "Operações de gerenciamento de solicitações")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    @Operation(summary = "Criar solicitação", description = "Cria uma nova solicitação associada ao usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Solicitação criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da solicitação inválidos"),
            @ApiResponse(responseCode = "403", description = "Usuário não autenticado")
    })
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
    @Operation(summary = "Listar solicitações", description = "Lista as solicitações aplicando filtros opcionais e paginação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitações encontradas"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de consulta inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public ResponseEntity<List<RequestResponseDTO>> findAll(
            @Parameter(description = "Texto para busca no título") @RequestParam(required = false) String title,

            @Parameter(description = "Categoria da solicitação") @RequestParam(required = false) Category category,

            @Parameter(description = "Status da solicitação") @RequestParam(required = false) RequestStatus status,

            @Parameter(description = "Data inicial do período") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Data final do período") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Quantidade de registros por página") @RequestParam(defaultValue = "10") int size) {

        RequestFilterDTO filter = new RequestFilterDTO(
                title,
                category,
                status,
                startDate,
                endDate);

        return ResponseEntity.ok(
                requestService.findAll(filter, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retorna uma solicitação", description = "Retorna uma solicitação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitação encontrada"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada"),
    })
    public ResponseEntity<RequestResponseDTO> findById(
            @Parameter(description = "ID da solicitação", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(requestService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Altera uma solicitação", description = "Altera a solicitação, caso ela esteja como aberta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitação alterada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada"),
            @ApiResponse(responseCode = "409", description = "Solicitação não está com o status de aberta"),
    })
    public ResponseEntity<RequestResponseDTO> update(
            @Parameter(description = "ID da solicitação", example = "1") @PathVariable Long id,
            @Valid @RequestBody UpdateRequestDTO dto) {
        return ResponseEntity.ok(requestService.update(id, dto));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Alterar status da solicitação", description = "Altera o status de uma solicitação existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status alterado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada"),
            @ApiResponse(responseCode = "400", description = "Status inválido"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public ResponseEntity<RequestResponseDTO> updateStatus(
            @Parameter(description = "ID da solicitação", example = "1") @PathVariable Long id,

            @Valid @RequestBody UpdateRequestStatusDTO dto) {
        return ResponseEntity.ok(requestService.updateStatus(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar solicitação", description = "Deleta a solicitação, caso ela esteja como aberta.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Solicitação deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada"),
            @ApiResponse(responseCode = "409", description = "Solicitação não está com o status de aberta"),
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da solicitação", example = "1") @PathVariable Long id) {

        requestService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
