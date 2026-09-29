package com.solicita.config;

import com.solicita.entity.User;
import com.solicita.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeUsers(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {

            if (userRepository.findByUsername("admin").isEmpty()) {

                User user = new User();

                user.setUsername("admin");
                user.setPassword(
                        passwordEncoder.encode("admin123"));
                user.setCreatedAt(Instant.now());

                userRepository.save(user);
            }
        };
    }
}
