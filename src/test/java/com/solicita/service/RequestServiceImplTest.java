package com.solicita.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.solicita.dto.request.CreateRequestDTO;
import com.solicita.dto.request.RequestResponseDTO;
import com.solicita.entity.Request;
import com.solicita.entity.User;
import com.solicita.enums.Category;
import com.solicita.enums.RequestStatus;
import com.solicita.exception.ResourceNotFoundException;
import com.solicita.repository.RequestRepository;
import com.solicita.repository.UserRepository;

import com.solicita.service.RequestServiceImpl;

@ExtendWith(MockitoExtension.class)
public class RequestServiceImplTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RequestServiceImpl requestService;

    @ParameterizedTest
    @EnumSource(Category.class)
    void shouldCreateRequestSuccessfullyWithCategory(Category category) {

        // Arrange
        String username = "admin";

        User user = new User();
        user.setId(1L);
        user.setUsername(username);

        CreateRequestDTO dto = new CreateRequestDTO(
                "Computador não liga",
                "O computador do setor não está iniciando.",
                category);

        Request savedRequest = new Request();
        savedRequest.setId(1L);
        savedRequest.setTitle(dto.title());
        savedRequest.setDescription(dto.description());
        savedRequest.setCategory(dto.category());
        savedRequest.setStatus(RequestStatus.ABERTO);
        savedRequest.setCreatedAt(Instant.now());
        savedRequest.setUser(user);

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(user));

        when(requestRepository.save(any(Request.class)))
                .thenReturn(savedRequest);

        // Act
        RequestResponseDTO response = requestService.create(dto, username);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(dto.title(), response.title());
        assertEquals(dto.description(), response.description());
        assertEquals(category, response.category());
        assertEquals(RequestStatus.ABERTO, response.status());
        assertEquals(username, response.username());

        verify(userRepository)
                .findByUsername(username);

        verify(requestRepository)
                .save(any(Request.class));
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {

        // Arrange
        String username = "unknown";

        CreateRequestDTO dto = new CreateRequestDTO(
                "Computador não liga",
                "Descrição",
                Category.TI);

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> requestService.create(dto, username));

        verify(userRepository)
                .findByUsername(username);

        verifyNoInteractions(requestRepository);
    }
}
