package com.solicita.repository.specification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.solicita.dto.request.RequestFilterDTO;
import com.solicita.entity.Request;
import com.solicita.entity.User;
import com.solicita.enums.Category;
import com.solicita.enums.RequestStatus;
import com.solicita.repository.RequestRepository;
import com.solicita.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
public class RequestSpecificationTest {

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
        private RequestRepository requestRepository;

        @Autowired
        private UserRepository userRepository;

        private User user;

        @BeforeEach
        void setUp() {
                user = new User();
                user.setUsername("admin");
                user.setPassword("password");
                user.setCreatedAt(Instant.now());

                user = userRepository.save(user);

                requestRepository.save(
                                createRequest(
                                                "Computador não liga",
                                                Category.TI,
                                                RequestStatus.ABERTO,
                                                Instant.parse("2026-09-10T10:00:00Z")));

                requestRepository.save(
                                createRequest(
                                                "Solicitação de acesso",
                                                Category.RH,
                                                RequestStatus.EM_ATENDIMENTO,
                                                Instant.parse("2026-09-15T10:00:00Z")));

                requestRepository.save(
                                createRequest(
                                                "Compra de equipamento",
                                                Category.COMPRAS,
                                                RequestStatus.CONCLUIDO,
                                                Instant.parse("2026-09-20T10:00:00Z")));
        }

        private Request createRequest(
                        String title,
                        Category category,
                        RequestStatus status,
                        Instant createdAt) {

                Request request = new Request();

                request.setTitle(title);
                request.setDescription("Descrição");
                request.setCategory(category);
                request.setStatus(status);
                request.setCreatedAt(createdAt);
                request.setUser(user);

                return request;
        }

        @Test
        void shouldFilterByTitleIgnoringCase() {

                RequestFilterDTO filter = new RequestFilterDTO(
                                "COMPUTADOR",
                                null,
                                null,
                                null,
                                null);

                Specification<Request> specification = RequestSpecification.withFilters(filter);

                List<Request> result = requestRepository.findAll(specification);

                assertEquals(1, result.size());
                assertEquals("Computador não liga", result.get(0).getTitle());
        }

        @Test
        void shouldFilterByCategory() {
                RequestFilterDTO filter = new RequestFilterDTO(
                                null,
                                Category.TI,
                                null,
                                null,
                                null);

                List<Request> result = requestRepository.findAll(
                                RequestSpecification.withFilters(filter));

                assertThat(result).hasSize(1);
                assertThat(result)
                                .extracting(Request::getCategory)
                                .containsOnly(Category.TI);

        }

        @Test
        void shouldFilterByStatus() {
                RequestFilterDTO filter = new RequestFilterDTO(
                                null,
                                null,
                                RequestStatus.EM_ATENDIMENTO,
                                null,
                                null);

                List<Request> result = requestRepository.findAll(
                                RequestSpecification.withFilters(filter));

                assertThat(result).hasSize(1);
                assertThat(result)
                                .extracting(Request::getStatus)
                                .containsOnly(RequestStatus.EM_ATENDIMENTO);
        }

        @Test
        void shouldFilterFromStartDate() {
                LocalDate startDate = LocalDate.of(2026, 9, 15);

                RequestFilterDTO filter = new RequestFilterDTO(
                                null,
                                null,
                                null,
                                startDate,
                                null);

                List<Request> result = requestRepository.findAll(
                                RequestSpecification.withFilters(filter));

                assertThat(result).hasSize(2);
                assertThat(result)
                                .allMatch(request -> !request.getCreatedAt()
                                                .isBefore(startDate.atStartOfDay().toInstant(ZoneOffset.UTC)));
        }

        @Test
        void shouldFilterUntilEndDate() {
                LocalDate endDate = LocalDate.of(2026, 9, 15);

                RequestFilterDTO filter = new RequestFilterDTO(
                                null,
                                null,
                                null,
                                null,
                                endDate);

                List<Request> result = requestRepository.findAll(
                                RequestSpecification.withFilters(filter));

                assertThat(result).hasSize(2);

                assertThat(result)
                                .allMatch(request -> request.getCreatedAt()
                                                .isBefore(
                                                                endDate.plusDays(1)
                                                                                .atStartOfDay()
                                                                                .toInstant(ZoneOffset.UTC)));
        }

        @Test
        void shouldFilterByMultipleCriteria() {
                RequestFilterDTO filter = new RequestFilterDTO(
                                "computador",
                                Category.TI,
                                RequestStatus.ABERTO,
                                LocalDate.of(2026, 9, 1),
                                LocalDate.of(2026, 9, 30));

                List<Request> result = requestRepository.findAll(
                                RequestSpecification.withFilters(filter));

                assertThat(result).hasSize(1);

                Request request = result.getFirst();

                assertThat(request.getTitle())
                                .containsIgnoringCase("computador");
                assertThat(request.getCategory())
                                .isEqualTo(Category.TI);
                assertThat(request.getStatus())
                                .isEqualTo(RequestStatus.ABERTO);
        }

        @Test
        void shouldReturnAllRequestsWhenNoFilterIsProvided() {
                RequestFilterDTO filter = new RequestFilterDTO(null, null, null, null, null);

                List<Request> result = requestRepository.findAll(
                                RequestSpecification.withFilters(filter));

                assertThat(result).hasSize(3);
        }
}
