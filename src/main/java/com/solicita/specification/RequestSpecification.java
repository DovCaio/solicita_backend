package com.solicita.specification;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.solicita.dto.request.RequestFilterDTO;
import com.solicita.entity.Request;

import jakarta.persistence.criteria.Predicate;

public class RequestSpecification {

        private RequestSpecification() {
        }

        public static Specification<Request> withFilters(RequestFilterDTO filter) {
                return (root, query, criteriaBuilder) -> {

                        List<Predicate> predicates = new ArrayList<>();

                        if (filter.title() != null && !filter.title().isBlank()) {
                                predicates.add(
                                                criteriaBuilder.like(
                                                                criteriaBuilder.lower(root.get("title")),
                                                                "%" + filter.title().toLowerCase() + "%"));
                        }

                        if (filter.category() != null) {
                                predicates.add(
                                                criteriaBuilder.equal(
                                                                root.get("category"),
                                                                filter.category()));
                        }

                        if (filter.status() != null) {
                                predicates.add(
                                                criteriaBuilder.equal(
                                                                root.get("status"),
                                                                filter.status()));
                        }

                        if (filter.startDate() != null) {
                                Instant start = filter.startDate()
                                                .atStartOfDay()
                                                .toInstant(ZoneOffset.UTC);

                                predicates.add(
                                                criteriaBuilder.greaterThanOrEqualTo(
                                                                root.get("createdAt"),
                                                                start));
                        }

                        if (filter.endDate() != null) {
                                Instant end = filter.endDate()
                                                .plusDays(1)
                                                .atStartOfDay()
                                                .toInstant(ZoneOffset.UTC);

                                predicates.add(
                                                criteriaBuilder.lessThan(
                                                                root.get("createdAt"),
                                                                end));
                        }

                        return criteriaBuilder.and(
                                        predicates.toArray(new Predicate[0]));
                };
        }

}
