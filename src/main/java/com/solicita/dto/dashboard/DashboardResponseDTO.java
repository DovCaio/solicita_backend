package com.solicita.dto.dashboard;

public record DashboardResponseDTO(
        long total,
        long open,
        long inService,
        long completed) {

}
