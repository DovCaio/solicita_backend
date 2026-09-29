package com.solicita.service;

import java.util.List;

import com.solicita.dto.request.CreateRequestDTO;
import com.solicita.dto.request.RequestResponseDTO;
import com.solicita.dto.request.UpdateRequestDTO;
import com.solicita.dto.request.UpdateRequestStatusDTO;

public interface RequestService {

    RequestResponseDTO create(
            CreateRequestDTO dto,
            String username);

    List<RequestResponseDTO> findAll();

    RequestResponseDTO findById(Long id);

    RequestResponseDTO update(
            Long id,
            UpdateRequestDTO dto);

    RequestResponseDTO updateStatus(Long id,
            UpdateRequestStatusDTO dto);

    void delete(Long id);

}
