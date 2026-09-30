package com.solicita.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categoria da solicitação")
public enum Category {
    TI,
    RH,
    COMPRAS,
    FINANCEIRO,
    INFRAESTRUTURA
}
