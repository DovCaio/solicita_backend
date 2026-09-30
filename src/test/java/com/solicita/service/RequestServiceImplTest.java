package com.solicita.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.solicita.dto.request.CreateRequestDTO;
import com.solicita.dto.request.RequestFilterDTO;
import com.solicita.dto.request.RequestResponseDTO;
import com.solicita.dto.request.UpdateRequestDTO;
import com.solicita.dto.request.UpdateRequestStatusDTO;
import com.solicita.entity.Request;
import com.solicita.entity.User;
import com.solicita.enums.Category;
import com.solicita.enums.RequestStatus;
import com.solicita.exception.BusinessException;
import com.solicita.exception.ResourceNotFoundException;
import com.solicita.exception.ToManyResourceRequisitionException;
import com.solicita.repository.RequestRepository;
import com.solicita.repository.UserRepository;
import com.solicita.repository.specification.RequestSpecification;

@ExtendWith(MockitoExtension.class)
public class RequestServiceImplTest {

        @Mock
        private RequestRepository requestRepository;

        @Mock
        private UserRepository userRepository;

        @InjectMocks
        private RequestServiceImpl requestService;

        private final String username = "admin";

        @ParameterizedTest
        @EnumSource(Category.class)
        void shouldCreateRequestSuccessfullyWithCategory(Category category) {

                // Arrange

                User user = new User();

                user.setId(1L);
                user.setUsername(username);
                user.setPassword("password");
                user.setCreatedAt(Instant.now());

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

        @ParameterizedTest
        @ValueSource(ints = { 1, 10, 50, 99, 100 })
        void shouldFindRequestsWhenPageSizeIsValid(int size) {

                // Arrange
                RequestFilterDTO filter = new RequestFilterDTO(
                                null,
                                null,
                                null,
                                null,
                                null);

                Request request = new Request();
                request.setId(1L);
                request.setTitle("Computador não liga");
                request.setDescription("O computador não inicia.");
                request.setCategory(Category.TI);
                request.setStatus(RequestStatus.ABERTO);

                User user = new User();
                user.setId(1L);
                user.setUsername("admin");

                request.setUser(user);
                request.setCreatedAt(Instant.now());

                Page<Request> requestPage = new PageImpl<>(List.of(request));

                Specification<Request> specification = RequestSpecification.withFilters(filter);

                when(requestRepository.findAll(
                                any(Specification.class),
                                any(Pageable.class))).thenReturn(requestPage);

                // Act
                List<RequestResponseDTO> response = requestService.findAll(filter, 0, size);

                // Assert
                assertNotNull(response);
                assertEquals(1, response.size());

                RequestResponseDTO dto = response.get(0);

                assertEquals(request.getId(), dto.id());
                assertEquals(request.getTitle(), dto.title());
                assertEquals(request.getDescription(), dto.description());
                assertEquals(request.getCategory(), dto.category());
                assertEquals(request.getStatus(), dto.status());
                assertEquals(request.getUser().getUsername(), dto.username());

                verify(requestRepository).findAll(
                                any(Specification.class),
                                eq(PageRequest.of(0, size)));
        }

        @Test
        void shouldThrowExceptionWhenPageSizeIsGreaterThan100() {

                // Arrange
                RequestFilterDTO filter = new RequestFilterDTO(
                                null,
                                null,
                                null,
                                null,
                                null);

                // Act + Assert
                assertThrows(
                                ToManyResourceRequisitionException.class,
                                () -> requestService.findAll(filter, 0, 101));

                verifyNoInteractions(requestRepository);
        }

        @Test
        void shouldUseRequestedPageAndSize() {

                // Arrange
                RequestFilterDTO filter = new RequestFilterDTO(
                                null,
                                null,
                                null,
                                null,
                                null);

                when(requestRepository.findAll(
                                any(Specification.class),
                                any(Pageable.class))).thenReturn(Page.empty());

                // Act
                requestService.findAll(filter, 2, 20);

                // Assert
                verify(requestRepository).findAll(
                                any(Specification.class),
                                eq(PageRequest.of(2, 20)));
        }

        @Test
        void shouldUseProvidedFilters() {

                // Arrange
                RequestFilterDTO filter = new RequestFilterDTO(
                                "computador",
                                Category.TI,
                                RequestStatus.ABERTO,
                                LocalDate.of(2026, 9, 1),
                                LocalDate.of(2026, 9, 29));

                when(requestRepository.findAll(
                                any(Specification.class),
                                any(Pageable.class))).thenReturn(Page.empty());

                // Act
                requestService.findAll(filter, 0, 10);

                // Assert
                verify(requestRepository).findAll(
                                any(Specification.class),
                                eq(PageRequest.of(0, 10)));
        }

        @Test
        void shouldFindRequestByIdSuccessfully() {

                // Arrange
                Long id = 1L;

                User user = new User();
                user.setId(1L);
                user.setUsername("admin");

                Request request = new Request();
                request.setId(id);
                request.setTitle("Computador não liga");
                request.setDescription("O computador do setor não está iniciando.");
                request.setCategory(Category.TI);
                request.setStatus(RequestStatus.ABERTO);
                request.setCreatedAt(Instant.now());
                request.setUser(user);

                when(requestRepository.findById(id))
                                .thenReturn(Optional.of(request));

                // Act
                RequestResponseDTO response = requestService.findById(id);

                // Assert
                assertNotNull(response);
                assertEquals(id, response.id());
                assertEquals(request.getTitle(), response.title());
                assertEquals(request.getDescription(), response.description());
                assertEquals(request.getCategory(), response.category());
                assertEquals(request.getStatus(), response.status());
                assertEquals(request.getCreatedAt(), response.createdAt());
                assertEquals(user.getId(), response.userId());
                assertEquals(user.getUsername(), response.username());

                verify(requestRepository).findById(id);
        }

        @Test
        void shouldThrowExceptionWhenRequestDoesNotExist() {

                // Arrange
                Long id = 999L;

                when(requestRepository.findById(id))
                                .thenReturn(Optional.empty());

                // Act + Assert
                assertThrows(
                                ResourceNotFoundException.class,
                                () -> requestService.findById(id));

                verify(requestRepository).findById(id);
        }

        @Test
        void shouldUpdateRequestSuccessfully() {

                // Arrange
                Long id = 1L;

                User user = new User();
                user.setId(1L);
                user.setUsername("admin");

                Request request = new Request();
                request.setId(id);
                request.setTitle("Computador não liga");
                request.setDescription("Descrição antiga");
                request.setCategory(Category.TI);
                request.setStatus(RequestStatus.ABERTO);
                request.setCreatedAt(Instant.now());
                request.setUser(user);

                UpdateRequestDTO dto = new UpdateRequestDTO(
                                "Computador apresenta erro",
                                "Nova descrição da solicitação.",
                                Category.INFRAESTRUTURA);

                when(requestRepository.findById(id))
                                .thenReturn(Optional.of(request));

                // Act
                RequestResponseDTO response = requestService.update(id, dto);

                // Assert
                assertEquals(dto.title(), request.getTitle());
                assertEquals(dto.description(), request.getDescription());
                assertEquals(dto.category(), request.getCategory());
                assertNotNull(request.getUpdatedAt());

                assertEquals(id, response.id());
                assertEquals(dto.title(), response.title());
                assertEquals(dto.description(), response.description());
                assertEquals(dto.category(), response.category());
                assertEquals(RequestStatus.ABERTO, response.status());
                assertEquals(user.getUsername(), response.username());

                verify(requestRepository).findById(id);
        }

        @ParameterizedTest
        @EnumSource(value = RequestStatus.class, names = { "EM_ATENDIMENTO", "CONCLUIDO" })
        void shouldNotUpdateRequestWhenStatusIsNotOpen(RequestStatus status) {

                // Arrange
                Long id = 1L;

                Request request = new Request();
                request.setId(id);
                request.setTitle("Computador não liga");
                request.setDescription("Descrição");
                request.setCategory(Category.TI);
                request.setStatus(status);

                UpdateRequestDTO dto = new UpdateRequestDTO(
                                "Novo título",
                                "Nova descrição",
                                Category.RH);

                when(requestRepository.findById(id))
                                .thenReturn(Optional.of(request));

                // Act + Assert
                assertThrows(
                                BusinessException.class,
                                () -> requestService.update(id, dto));

                assertEquals("Computador não liga", request.getTitle());
                assertEquals("Descrição", request.getDescription());
                assertEquals(Category.TI, request.getCategory());
                assertEquals(status, request.getStatus());

                verify(requestRepository).findById(id);
        }

        @Test
        void shouldThrowExceptionWhenRequestDoesNotExistForUpdateFeature() {

                // Arrange
                Long id = 999L;

                UpdateRequestDTO dto = new UpdateRequestDTO(
                                "Novo título",
                                "Nova descrição",
                                Category.TI);

                when(requestRepository.findById(id))
                                .thenReturn(Optional.empty());

                // Act + Assert
                assertThrows(
                                ResourceNotFoundException.class,
                                () -> requestService.update(id, dto));

                verify(requestRepository).findById(id);
        }

        @ParameterizedTest
        @EnumSource(RequestStatus.class)
        void shouldUpdateRequestStatusSuccessfully(RequestStatus status) {

                // Arrange
                Long id = 1L;

                User user = new User();
                user.setId(1L);
                user.setUsername("admin");

                Request request = new Request();
                request.setId(id);
                request.setTitle("Computador não liga");
                request.setDescription("Descrição");
                request.setCategory(Category.TI);
                request.setStatus(RequestStatus.ABERTO);
                request.setCreatedAt(Instant.now());
                request.setUser(user);

                UpdateRequestStatusDTO dto = new UpdateRequestStatusDTO(status);

                when(requestRepository.findById(id))
                                .thenReturn(Optional.of(request));

                // Act
                RequestResponseDTO response = requestService.updateStatus(id, dto);

                // Assert
                assertEquals(status, request.getStatus());
                assertNotNull(request.getUpdatedAt());

                assertEquals(id, response.id());
                assertEquals(status, response.status());
                assertEquals(user.getUsername(), response.username());

                verify(requestRepository).findById(id);
        }

        @Test
        void shouldThrowExceptionWhenRequestDoesNotExistOnUpdateStatus() {

                // Arrange
                Long id = 999L;

                UpdateRequestStatusDTO dto = new UpdateRequestStatusDTO(RequestStatus.CONCLUIDO);

                when(requestRepository.findById(id))
                                .thenReturn(Optional.empty());

                // Act + Assert
                assertThrows(
                                ResourceNotFoundException.class,
                                () -> requestService.updateStatus(id, dto));

                verify(requestRepository).findById(id);
        }

}
