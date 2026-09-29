package com.solicita.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solicita.dto.dashboard.DashboardResponseDTO;
import com.solicita.enums.RequestStatus;
import com.solicita.repository.RequestRepository;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final RequestRepository requestRepository;

    public DashboardServiceImpl(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponseDTO getDashboard() {

        long total = requestRepository.count();

        long open = requestRepository.countByStatus(
                RequestStatus.ABERTO);

        long inService = requestRepository.countByStatus(
                RequestStatus.EM_ATENDIMENTO);

        long completed = requestRepository.countByStatus(
                RequestStatus.CONCLUIDO);

        return new DashboardResponseDTO(
                total,
                open,
                inService,
                completed);
    }

}
