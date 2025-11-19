////package com.techcorp.employee.controller;
////
////import com.techcorp.employee.dto.CompanyStatisticsDTO;
////import com.techcorp.employee.model.CompanyStatistics;
////import com.techcorp.employee.model.Employee;
////import com.techcorp.employee.model.EmploymentStatus;
////import com.techcorp.employee.model.Position;
////import com.techcorp.employee.service.EmployeeService;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.http.ResponseEntity;
////import org.springframework.web.bind.annotation.*;
////
////import java.util.HashMap;
////import java.util.Map;
////import java.util.Optional;
////import java.util.stream.Collectors;
////
////@RestController
////@RequestMapping("/api/statistics")
////public class StatisticsController {
////
////    @Autowired
////    private EmployeeService employeeService;
////
////    // GET - średnie wynagrodzenie (ogólne lub dla firmy)
////    @GetMapping("/salary/average")
////    public ResponseEntity<Map<String, Double>> getAverageSalary(
////            @RequestParam(required = false) String company) {
////
////        Map<String, Double> response = new HashMap<>();
////
////        if (company != null && !company.trim().isEmpty()) {
////            double avgSalary = employeeService.calculateAverageSalaryByCompany(company)
////                    .orElse(0.0);
////            response.put("averageSalary", avgSalary);
////        } else {
////            double avgSalary = employeeService.calculateAverageSalary()
////                    .orElse(0.0);
////            response.put("averageSalary", avgSalary);
////        }
////
////        return ResponseEntity.ok(response);
////    }
////
////    // GET - statystyki firmy
////    @GetMapping("/company/{companyName}")
////    public ResponseEntity<CompanyStatisticsDTO> getCompanyStatistics(@PathVariable String companyName) {
////        CompanyStatistics stats = employeeService.getCompanyStatistics(companyName);
////
////        double highestSalary = employeeService.findHighestSalaryByCompany(companyName);
////
////        CompanyStatisticsDTO dto = new CompanyStatisticsDTO(
////                companyName,
////                stats.getEmployeeCount(),
////                stats.getAverageSalary(),
////                highestSalary,
////                stats.getHighestPaidEmployee()
////        );
////
////        return ResponseEntity.ok(dto);
////    }
////
////    // GET - liczba pracowników na stanowiskach
////    @GetMapping("/positions")
////    public ResponseEntity<Map<String, Integer>> getPositionStatistics() {
////        Map<Position, Long> positionCounts = employeeService.countEmployeesByPosition();
////
////        Map<String, Integer> response = positionCounts.entrySet().stream()
////                .collect(Collectors.toMap(
////                        entry -> entry.getKey().name(),
////                        entry -> entry.getValue().intValue()
////                ));
////
////        return ResponseEntity.ok(response);
////    }
////
////    // GET - rozkład statusów zatrudnienia
////    @GetMapping("/status")
////    public ResponseEntity<Map<String, Integer>> getEmploymentStatusStatistics() {
////        Map<EmploymentStatus, Long> statusDistribution = employeeService.getEmploymentStatusDistribution();
////
////        Map<String, Integer> response = statusDistribution.entrySet().stream()
////                .collect(Collectors.toMap(
////                        entry -> entry.getKey().name(),
////                        entry -> entry.getValue().intValue()
////                ));
////
////        return ResponseEntity.ok(response);
////    }
////}
//
//
//
//
//
//
//package com.techcorp.employee.controller;
//
//import com.techcorp.employee.dto.CompanyStatisticsDTO;
//import com.techcorp.employee.service.StatisticsService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/statistics")
//public class StatisticsController {
//
//    @Autowired
//    private StatisticsService statisticsService;
//
//    // GET - średnie wynagrodzenie (ogólne lub dla firmy)
//    @GetMapping("/salary/average")
//    public ResponseEntity<Map<String, Double>> getAverageSalary(
//            @RequestParam(required = false) String company) {
//
//        Map<String, Double> response = statisticsService.getAverageSalary(company);
//        return ResponseEntity.ok(response);
//    }
//
//    // GET - statystyki firmy
//    @GetMapping("/company/{companyName}")
//    public ResponseEntity<CompanyStatisticsDTO> getCompanyStatistics(@PathVariable String companyName) {
//        CompanyStatisticsDTO dto = statisticsService.getCompanyStatistics(companyName);
//        return ResponseEntity.ok(dto);
//    }
//
//    // GET - liczba pracowników na stanowiskach
//    @GetMapping("/positions")
//    public ResponseEntity<Map<String, Integer>> getPositionStatistics() {
//        Map<String, Integer> response = statisticsService.getPositionStatistics();
//        return ResponseEntity.ok(response);
//    }
//
//    // GET - rozkład statusów zatrudnienia
//    @GetMapping("/status")
//    public ResponseEntity<Map<String, Integer>> getEmploymentStatusStatistics() {
//        Map<String, Integer> response = statisticsService.getEmploymentStatusStatistics();
//        return ResponseEntity.ok(response);
//    }
//}







package com.techcorp.employee.controller;

import com.techcorp.employee.dto.CompanyStatisticsDTO;
import com.techcorp.employee.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
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
    public ResponseEntity<CompanyStatisticsDTO> getCompanyStatistics(@PathVariable String companyName) {
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