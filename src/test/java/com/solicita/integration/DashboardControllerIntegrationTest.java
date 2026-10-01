package com.solicita.integration;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
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

import org.springframework.http.MediaType;

import com.solicita.entity.Request;
import com.solicita.entity.User;
import com.solicita.enums.Category;
import com.solicita.enums.RequestStatus;
import com.solicita.repository.RequestRepository;
import com.solicita.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class DashboardControllerIntegrationTest {

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

    private MockHttpSession login() throws Exception {
        MvcResult result = mockMvc.perform(
                post("/api/auth/login")
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

    @Test
    void shouldReturnDashboard() throws Exception {
        MockHttpSession session = login();

        User user = userRepository.findByUsername("admin")
                .orElseThrow();

        Request open = new Request();
        open.setTitle("Solicitação aberta");
        open.setDescription("Descrição");
        open.setCategory(Category.TI);
        open.setStatus(RequestStatus.ABERTO);
        open.setCreatedAt(Instant.now());
        open.setUser(user);

        Request inService = new Request();
        inService.setTitle("Solicitação em atendimento");
        inService.setDescription("Descrição");
        inService.setCategory(Category.RH);
        inService.setStatus(RequestStatus.EM_ATENDIMENTO);
        inService.setCreatedAt(Instant.now());
        inService.setUser(user);

        Request completed = new Request();
        completed.setTitle("Solicitação concluída");
        completed.setDescription("Descrição");
        completed.setCategory(Category.COMPRAS);
        completed.setStatus(RequestStatus.CONCLUIDO);
        completed.setCreatedAt(Instant.now());
        completed.setUser(user);

        requestRepository.saveAll(List.of(
                open,
                inService,
                completed));

        mockMvc.perform(
                get("/api/dashboard")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.open").value(1))
                .andExpect(jsonPath("$.inService").value(1))
                .andExpect(jsonPath("$.completed").value(1));
    }
}
