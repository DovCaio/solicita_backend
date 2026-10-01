package com.solicita.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
                @Schema(description = "Nome de usuário", example = "admin") @NotBlank String username,

                @Schema(description = "Senha do usuário", example = "admin123") @NotBlank String password

) {

}
