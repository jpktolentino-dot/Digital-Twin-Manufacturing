package com.jermam.controller;

import com.jermam.dto.DashboardSummaryResponse;
import com.jermam.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService service;
    @GetMapping("/resumo") public DashboardSummaryResponse resumo() { return service.resumo(); }
}
