package com.solicita.dto.request;

import java.time.LocalDate;

import com.solicita.enums.Category;
import com.solicita.enums.RequestStatus;

public record RequestFilterDTO(
        String title,
        Category category,
        RequestStatus status,
        LocalDate startDate,
        LocalDate endDate) {
}