package com.techcorp.employee.controller;

import com.techcorp.employee.dto.CompanyStatisticsDTO;
import com.techcorp.employee.model.Employee;
import com.techcorp.employee.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/statistics")
public class StatisticsViewController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping
    public String showStatistics(Model model) {
        // Pobieranie wszystkich danych statystycznych z serwisu
        Map<String, Object> statistics = statisticsService.getAllStatistics();

        // Przekazanie danych do modelu
        model.addAllAttributes(statistics);
        model.addAttribute("pageTitle", "Statystyki");

        return "statistics/index";
    }

    @GetMapping("/company/{companyName}")
    public String showCompanyStatistics(@PathVariable String companyName, Model model) {
        // Użycie DTO zamiast bezpośrednio CompanyStatistics
        CompanyStatisticsDTO stats = statisticsService.getCompanyStatisticsDTO(companyName);

        model.addAttribute("stats", stats);
        model.addAttribute("companyName", companyName);
        model.addAttribute("employees", statisticsService.getEmployeesByCompany(companyName));
        model.addAttribute("pageTitle", "Statystyki - " + companyName);

        return "statistics/company-details";
    }
}