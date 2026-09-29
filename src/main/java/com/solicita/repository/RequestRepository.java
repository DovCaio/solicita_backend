package com.solicita.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.solicita.dto.request.RequestFilterDTO;
import com.solicita.dto.request.RequestResponseDTO;
import com.solicita.entity.Request;

public interface RequestRepository extends JpaRepository<Request, Long>,
        JpaSpecificationExecutor<Request> {
    List<RequestResponseDTO> findAll(RequestFilterDTO filter);
}
