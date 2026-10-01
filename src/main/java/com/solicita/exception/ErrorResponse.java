package com.solicita.exception;

import java.time.Instant;

public record ErrorResponse(
        String message,
        Instant timestamp) {
}