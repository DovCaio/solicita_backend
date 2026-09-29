package com.solicita.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solicita.service.RequestServiceImpl;

@RestController
@RequestMapping("/api/requests")
public class RequestController {

    private final RequestServiceImpl requestService;

    public RequestController(RequestServiceImpl requestService) {
        this.requestService = requestService;
    }

}
