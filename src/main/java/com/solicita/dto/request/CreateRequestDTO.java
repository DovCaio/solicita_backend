package com.solicita.dto.request;

import com.solicita.enums.Category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRequestDTO(
        @NotBlank @Size(max = 150) String title,

        @NotBlank String description,

        @NotNull Category category) {

}
