package com.solicita.dto.request;

import com.solicita.enums.Category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRequestDTO(
        @Schema(description = "Título da solicitação", example = "Computador não liga") @NotBlank(message = "O titulo não deve ser vazio.") @Size(max = 150, message = "Titulo não pode passar de 150 caracteres") String title,

        @Schema(description = "Descrição detalhada da solicitação", example = "O computador não apresenta nenhum sinal ao pressionar o botão de ligar.") @NotBlank(message = "A descrição não deve ser vazia.") String description,

        @Schema(description = "Categoria da solicitação", example = "TI") @NotNull(message = "A categoria não deve ser vazia") Category category) {

}
