package com.solicita.service;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.solicita.dto.dashboard.DashboardResponseDTO;
import com.solicita.enums.RequestStatus;
import com.solicita.repository.RequestRepository;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceImplTest {

    @Mock
    private RequestRepository requestRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void shouldReturnDashboardWithRequestCounts() {
        when(requestRepository.count()).thenReturn(10L);
        when(requestRepository.countByStatus(RequestStatus.ABERTO))
                .thenReturn(4L);
        when(requestRepository.countByStatus(RequestStatus.EM_ATENDIMENTO))
                .thenReturn(3L);
        when(requestRepository.countByStatus(RequestStatus.CONCLUIDO))
                .thenReturn(3L);

        DashboardResponseDTO result = dashboardService.getDashboard();

        assertThat(result.total()).isEqualTo(10L);
        assertThat(result.open()).isEqualTo(4L);
        assertThat(result.inService()).isEqualTo(3L);
        assertThat(result.completed()).isEqualTo(3L);
    }

}
