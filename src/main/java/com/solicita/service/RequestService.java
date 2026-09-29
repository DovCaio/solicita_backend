package com.solicita.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.solicita.dto.request.CreateRequestDTO;
import com.solicita.dto.request.RequestResponseDTO;
import com.solicita.dto.request.UpdateRequestDTO;
import com.solicita.dto.request.UpdateRequestStatusDTO;
import com.solicita.entity.Request;
import com.solicita.entity.User;
import com.solicita.enums.RequestStatus;
import com.solicita.repository.RequestRepository;
import com.solicita.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    public RequestService(
            RequestRepository requestRepository,
            UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RequestResponseDTO create(
            CreateRequestDTO dto,
            String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Request request = new Request();

        request.setTitle(dto.title());
        request.setDescription(dto.description());
        request.setCategory(dto.category());

        request.setStatus(RequestStatus.ABERTO);
        request.setCreatedAt(Instant.now());
        request.setUser(user);

        Request savedRequest = requestRepository.save(request);

        return toResponse(savedRequest);
    }

    @Transactional(readOnly = true)
    public List<RequestResponseDTO> findAll() {

        return requestRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RequestResponseDTO findById(Long id) {

        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        return toResponse(request);
    }

    @Transactional
    public RequestResponseDTO update(
            Long id,
            UpdateRequestDTO dto) {

        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        if (request.getStatus() != RequestStatus.ABERTO) {
            throw new IllegalStateException(
                    "Somente solicitações abertas podem ser editadas");
        }

        request.setTitle(dto.title());
        request.setDescription(dto.description());
        request.setCategory(dto.category());
        request.setUpdatedAt(Instant.now());

        return toResponse(request);
    }

    @Transactional
    public RequestResponseDTO updateStatus(
            Long id,
            UpdateRequestStatusDTO dto) {

        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        request.setStatus(dto.status());
        request.setUpdatedAt(Instant.now());

        return toResponse(request);
    }

    @Transactional
    public void delete(Long id) {

        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        if (request.getStatus() != RequestStatus.ABERTO) {
            throw new IllegalStateException(
                    "Somente solicitações abertas podem ser excluídas");
        }

        requestRepository.delete(request);
    }

    private RequestResponseDTO toResponse(Request request) {

        return new RequestResponseDTO(
                request.getId(),
                request.getTitle(),
                request.getDescription(),
                request.getCategory(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                request.getUser().getId(),
                request.getUser().getUsername());
    }
}
