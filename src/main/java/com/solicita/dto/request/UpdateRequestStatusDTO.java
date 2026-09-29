package com.solicita.dto.request;

import com.solicita.enums.RequestStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateRequestStatusDTO(
        @NotNull RequestStatus status) {

}
