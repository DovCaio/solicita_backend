package com.solicita.dto.request;

import java.time.Instant;

import com.solicita.enums.Category;
import com.solicita.enums.RequestStatus;

public record RequestResponseDTO(
        Long id,
        String title,
        String description,
        Category category,
        RequestStatus status,
        Instant createdAt,
        Instant updatedAt,
        Long userId,
        String username) {

}
