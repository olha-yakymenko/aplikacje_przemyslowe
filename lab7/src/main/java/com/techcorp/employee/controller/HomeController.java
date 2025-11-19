package com.techcorp.employee.controller;

import com.techcorp.employee.dto.DashboardStatisticsDTO;
import com.techcorp.employee.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/")
    public String home(Model model) {
        try {
            DashboardStatisticsDTO stats = dashboardService.getDashboardStatistics();

            model.addAttribute("totalEmployees", stats.getTotalEmployees());
            model.addAttribute("avgSalary", stats.getAverageSalary());
            model.addAttribute("totalDepartments", stats.getTotalDepartments());

        } catch (Exception e) {
            model.addAttribute("totalEmployees", 0);
            model.addAttribute("avgSalary", 0.0);
            model.addAttribute("totalDepartments", 0);
            model.addAttribute("error", "Nie udało się załadować danych statystycznych");
        }

        return "index";
    }
}