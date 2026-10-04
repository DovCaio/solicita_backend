package com.solicita.dto.request;

import com.solicita.enums.RequestStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateRequestStatusDTO(
                @NotNull(message = "O status não deve ser vazio") RequestStatus status) {

}
