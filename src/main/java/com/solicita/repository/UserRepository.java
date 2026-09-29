package com.solicita.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.solicita.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

}