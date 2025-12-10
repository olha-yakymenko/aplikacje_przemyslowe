




package com.techcorp.employee.controller;

import com.techcorp.employee.dto.CompanyStatisticsDTO;
import com.techcorp.employee.service.StatisticsService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@Validated  // Dodaj adnotację @Validated
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    // GET - średnie wynagrodzenie (ogólne lub dla firmy)
    @GetMapping("/salary/average")
    public ResponseEntity<Map<String, Double>> getAverageSalary(
            @RequestParam(required = false) String company) {

        Map<String, Double> response = statisticsService.getAverageSalary(company);
        return ResponseEntity.ok(response);
    }

    // GET - statystyki firmy
    @GetMapping("/company/{companyName}")
    public ResponseEntity<CompanyStatisticsDTO> getCompanyStatistics(
            @PathVariable @NotBlank(message = "Company name cannot be blank") String companyName) {

        CompanyStatisticsDTO dto = statisticsService.getCompanyStatisticsDTO(companyName);
        return ResponseEntity.ok(dto);
    }

    // GET - liczba pracowników na stanowiskach
    @GetMapping("/positions")
    public ResponseEntity<Map<String, Integer>> getPositionStatistics() {
        Map<String, Integer> response = statisticsService.getPositionStatistics();
        return ResponseEntity.ok(response);
    }

    // GET - rozkład statusów zatrudnienia
    @GetMapping("/status")
    public ResponseEntity<Map<String, Integer>> getEmploymentStatusStatistics() {
        Map<String, Integer> response = statisticsService.getEmploymentStatusStatistics();
        return ResponseEntity.ok(response);
    }
}