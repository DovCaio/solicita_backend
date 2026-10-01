package com.solicita.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status atual da solicitação")
public enum RequestStatus {
    ABERTO,
    EM_ATENDIMENTO,
    CONCLUIDO
}
