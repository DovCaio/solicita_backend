package com.solicita.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.solicita.entity.Request;
import com.solicita.entity.User;
import com.solicita.enums.Category;
import com.solicita.enums.RequestStatus;
import com.solicita.repository.RequestRepository;
import com.solicita.repository.UserRepository;

import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.time.Instant;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class RequestControllerIntegrationTest {

        @Container
        static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
                        .withDatabaseName("solicita")
                        .withUsername("solicita")
                        .withPassword("solicita");

        @DynamicPropertySource
        static void configureProperties(DynamicPropertyRegistry registry) {
                registry.add(
                                "spring.datasource.url",
                                postgres::getJdbcUrl);

                registry.add(
                                "spring.datasource.username",
                                postgres::getUsername);

                registry.add(
                                "spring.datasource.password",
                                postgres::getPassword);
        }

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private RequestRepository requestRepository;

        @Autowired
        private UserRepository userRepository;

        @BeforeEach
        void setUp() {
                requestRepository.deleteAll();
        }

        private MockHttpSession login() throws Exception {
                MvcResult result = mockMvc.perform(
                                post("/api/auth/login")
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                    "username": "admin",
                                                                    "password": "admin123"
                                                                }
                                                                """))
                                .andExpect(status().isOk())
                                .andReturn();

                return (MockHttpSession) result.getRequest().getSession();
        }

        @ParameterizedTest
        @EnumSource(Category.class)
        void shouldCreateRequestWithDifferentCategories(Category category) throws Exception {
                MockHttpSession session = login();

                mockMvc.perform(
                                post("/api/requests")
                                                .session(session)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                    "title": "Nova solicitação",
                                                                    "description": "Descrição da solicitação",
                                                                    "category": "%s"
                                                                }
                                                                """.formatted(category)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.title").value("Nova solicitação"))
                                .andExpect(jsonPath("$.description").value("Descrição da solicitação"))
                                .andExpect(jsonPath("$.category").value(category.name()))
                                .andExpect(jsonPath("$.status").value("ABERTO"))
                                .andExpect(jsonPath("$.username").value("admin"))
                                .andExpect(jsonPath("$.createdAt").exists());
        }

        @Test
        void shouldRejectRequestWithoutTitle() throws Exception {
                MockHttpSession session = login();

                mockMvc.perform(
                                post("/api/requests")
                                                .session(session)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                    "title": "",
                                                                    "description": "Descrição",
                                                                    "category": "TI"
                                                                }
                                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").exists())
                                .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        void shouldFindRequestById() throws Exception {
                MockHttpSession session = login();

                User user = userRepository.findByUsername("admin")
                                .orElseThrow();

                Request request = new Request();
                request.setTitle("Computador não liga");
                request.setDescription("Descrição");
                request.setCategory(Category.TI);
                request.setStatus(RequestStatus.ABERTO);
                request.setCreatedAt(Instant.now());
                request.setUser(user);

                request = requestRepository.save(request);

                mockMvc.perform(
                                get("/api/requests/{id}", request.getId())
                                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(request.getId()))
                                .andExpect(jsonPath("$.title").value("Computador não liga"))
                                .andExpect(jsonPath("$.category").value("TI"))
                                .andExpect(jsonPath("$.status").value("ABERTO"))
                                .andExpect(jsonPath("$.username").value("admin"));
        }

        @Test
        void shouldListRequests() throws Exception {
                MockHttpSession session = login();

                User user = userRepository.findByUsername("admin")
                                .orElseThrow();

                Request first = new Request();
                first.setTitle("Computador não liga");
                first.setDescription("Descrição 1");
                first.setCategory(Category.TI);
                first.setStatus(RequestStatus.ABERTO);
                first.setCreatedAt(Instant.now());
                first.setUser(user);

                Request second = new Request();
                second.setTitle("Compra de equipamento");
                second.setDescription("Descrição 2");
                second.setCategory(Category.COMPRAS);
                second.setStatus(RequestStatus.EM_ATENDIMENTO);
                second.setCreatedAt(Instant.now());
                second.setUser(user);

                requestRepository.save(first);
                requestRepository.save(second);

                mockMvc.perform(
                                get("/api/requests")
                                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        void shouldFilterRequestsByCategory() throws Exception {
                MockHttpSession session = login();

                User user = userRepository.findByUsername("admin")
                                .orElseThrow();

                Request tiRequest = new Request();
                tiRequest.setTitle("Computador");
                tiRequest.setDescription("TI");
                tiRequest.setCategory(Category.TI);
                tiRequest.setStatus(RequestStatus.ABERTO);
                tiRequest.setCreatedAt(Instant.now());
                tiRequest.setUser(user);

                Request hrRequest = new Request();
                hrRequest.setTitle("Acesso");
                hrRequest.setDescription("RH");
                hrRequest.setCategory(Category.RH);
                hrRequest.setStatus(RequestStatus.ABERTO);
                hrRequest.setCreatedAt(Instant.now());
                hrRequest.setUser(user);

                requestRepository.save(tiRequest);
                requestRepository.save(hrRequest);

                mockMvc.perform(
                                get("/api/requests")
                                                .param("category", "TI")
                                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1))
                                .andExpect(jsonPath("$[0].category").value("TI"));
        }

        @ParameterizedTest
        @EnumSource(Category.class)
        void shouldUpdateRequestWithDifferentCategories(Category category) throws Exception {
                MockHttpSession session = login();

                User user = userRepository.findByUsername("admin")
                                .orElseThrow();

                Request request = createRequest(
                                "Solicitação antiga",
                                Category.TI,
                                RequestStatus.ABERTO,
                                Instant.now(),
                                user);

                request.setUser(user);
                request = requestRepository.save(request);

                mockMvc.perform(
                                put("/api/requests/{id}", request.getId())
                                                .session(session)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                    "title": "Solicitação atualizada",
                                                                    "description": "Descrição atualizada",
                                                                    "category": "%s"
                                                                }
                                                                """.formatted(category)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(request.getId()))
                                .andExpect(jsonPath("$.title").value("Solicitação atualizada"))
                                .andExpect(jsonPath("$.description").value("Descrição atualizada"))
                                .andExpect(jsonPath("$.category").value(category.name()))
                                .andExpect(jsonPath("$.status").value("ABERTO"))
                                .andExpect(jsonPath("$.updatedAt").exists());
        }

        @Test
        void shouldUpdateRequestStatus() throws Exception {
                MockHttpSession session = login();

                User user = userRepository.findByUsername("admin")
                                .orElseThrow();

                Request request = new Request();
                request.setTitle("Computador não liga");
                request.setDescription("Descrição");
                request.setCategory(Category.TI);
                request.setStatus(RequestStatus.ABERTO);
                request.setCreatedAt(Instant.now());
                request.setUser(user);

                request = requestRepository.save(request);

                mockMvc.perform(
                                patch("/api/requests/{id}/status", request.getId())
                                                .session(session)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                    "status": "EM_ATENDIMENTO"
                                                                }
                                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status")
                                                .value("EM_ATENDIMENTO"))
                                .andExpect(jsonPath("$.updatedAt").exists());
        }

        @Test
        void shouldDeleteRequest() throws Exception {
                MockHttpSession session = login();

                User user = userRepository.findByUsername("admin")
                                .orElseThrow();

                Request request = new Request();
                request.setTitle("Computador não liga");
                request.setDescription("Descrição");
                request.setCategory(Category.TI);
                request.setStatus(RequestStatus.ABERTO);
                request.setCreatedAt(Instant.now());
                request.setUser(user);

                request = requestRepository.save(request);

                Long requestId = request.getId();

                mockMvc.perform(
                                delete("/api/requests/{id}", requestId)
                                                .session(session))
                                .andExpect(status().isNoContent());

                assertThat(requestRepository.findById(requestId))
                                .isEmpty();
        }

        @Test
        void shouldNotUpdateRequestWhenStatusIsInService() throws Exception {
                MockHttpSession session = login();

                User user = userRepository.findByUsername("admin")
                                .orElseThrow();

                Request request = new Request();
                request.setTitle("Computador não liga");
                request.setDescription("Descrição");
                request.setCategory(Category.TI);
                request.setStatus(RequestStatus.EM_ATENDIMENTO);
                request.setCreatedAt(Instant.now());
                request.setUser(user);

                Long reqId = requestRepository.save(request).getId();

                mockMvc.perform(put("/api/requests/{id}", reqId)
                                .session(session)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "title": "Novo título",
                                                    "description": "Nova descrição",
                                                    "category": "RH"
                                                }
                                                """))
                                .andExpect(status().isConflict());

                Request reqForVerification = requestRepository.findById(reqId)
                                .orElseThrow();

                assertThat(reqForVerification.getTitle())
                                .isEqualTo("Computador não liga");

                assertThat(reqForVerification.getDescription())
                                .isEqualTo("Descrição");

                assertThat(reqForVerification.getCategory())
                                .isEqualTo(Category.TI);

                assertThat(reqForVerification.getStatus())
                                .isEqualTo(RequestStatus.EM_ATENDIMENTO);
        }

        @Test
        void shouldNotUpdateRequestWhenStatusIsCompleted() throws Exception {
                MockHttpSession session = login();

                User user = userRepository.findByUsername("admin")
                                .orElseThrow();

                Request request = new Request();
                request.setTitle("Computador não liga");
                request.setDescription("Descrição");
                request.setCategory(Category.TI);
                request.setStatus(RequestStatus.CONCLUIDO);
                request.setCreatedAt(Instant.now());
                request.setUser(user);

                Long reqId = requestRepository.save(request).getId();

                mockMvc.perform(put("/api/requests/{id}", reqId)
                                .session(session)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "title": "Novo título",
                                                    "description": "Nova descrição",
                                                    "category": "RH"
                                                }
                                                """))
                                .andExpect(status().isConflict());

                Request reqForVerification = requestRepository.findById(reqId)
                                .orElseThrow();

                assertThat(reqForVerification.getTitle())
                                .isEqualTo("Computador não liga");

                assertThat(reqForVerification.getDescription())
                                .isEqualTo("Descrição");

                assertThat(reqForVerification.getCategory())
                                .isEqualTo(Category.TI);

                assertThat(reqForVerification.getStatus())
                                .isEqualTo(RequestStatus.CONCLUIDO);
        }

        @Test
        void shouldNotDeleteRequestWhenStatusIsInService() throws Exception {
                MockHttpSession session = login();

                User user = userRepository.findByUsername("admin")
                                .orElseThrow();

                Request request = new Request();
                request.setTitle("Computador não liga");
                request.setDescription("Descrição");
                request.setCategory(Category.TI);
                request.setStatus(RequestStatus.EM_ATENDIMENTO);
                request.setCreatedAt(Instant.now());
                request.setUser(user);

                Long reqId = requestRepository.save(request).getId();

                mockMvc.perform(delete("/api/requests/{id}", request.getId())
                                .session(session)
                                .with(csrf()))
                                .andExpect(status().isConflict());

                Boolean exists = requestRepository.existsById(reqId);

                assertThat(exists).isTrue();
                // verifica que os dados originais continuam iguais
        }

        @Test
        void shouldNotDeleteRequestWhenStatusIsCompleted() throws Exception {
                MockHttpSession session = login();

                User user = userRepository.findByUsername("admin")
                                .orElseThrow();

                Request request = new Request();
                request.setTitle("Computador não liga");
                request.setDescription("Descrição");
                request.setCategory(Category.TI);
                request.setStatus(RequestStatus.EM_ATENDIMENTO);
                request.setCreatedAt(Instant.now());
                request.setUser(user);

                Long reqId = requestRepository.save(request).getId();

                mockMvc.perform(delete("/api/requests/{id}", request.getId())
                                .session(session)
                                .with(csrf()))
                                .andExpect(status().isConflict());
                Boolean exists = requestRepository.existsById(reqId);

                assertThat(exists).isTrue();

        }

        private Request createRequest(
                        String title,
                        Category category,
                        RequestStatus status,
                        Instant createdAt, User user) {

                Request request = new Request();

                request.setTitle(title);
                request.setDescription("Descrição");
                request.setCategory(category);
                request.setStatus(status);
                request.setCreatedAt(createdAt);
                request.setUser(user);

                return request;
        }
}
