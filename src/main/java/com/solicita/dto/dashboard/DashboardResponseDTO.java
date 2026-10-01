package com.solicita.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

public record DashboardResponseDTO(
                @Schema(description = "Quantidade total de solicitações", example = "10") long total,

                @Schema(description = "Quantidade de solicitações com status ABERTO", example = "4") long open,

                @Schema(description = "Quantidade de solicitações com status EM_ATENDIMENTO", example = "3") long inService,

                @Schema(description = "Quantidade de solicitações com status CONCLUIDO", example = "3") long completed) {

}
