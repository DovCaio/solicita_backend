package com.solicita.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.solicita.entity.Request;

public interface RequestRepository extends JpaRepository<Request, Long> {

}
