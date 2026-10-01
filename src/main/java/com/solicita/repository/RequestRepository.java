package com.solicita.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.solicita.entity.Request;
import com.solicita.enums.RequestStatus;

public interface RequestRepository extends JpaRepository<Request, Long>,
        JpaSpecificationExecutor<Request> {

    long countByStatus(RequestStatus status);
}
