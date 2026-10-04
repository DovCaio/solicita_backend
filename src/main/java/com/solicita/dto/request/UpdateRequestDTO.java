package com.solicita.dto.request;

import com.solicita.enums.Category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateRequestDTO(
                @NotBlank(message = "O titulo não deve ser vazio.") @Size(max = 150) String title,

                @NotBlank(message = "A descrição não deve ser vazia.") String description,

                @NotNull(message = "A categoria não pode ser vazia.") Category category) {

}
