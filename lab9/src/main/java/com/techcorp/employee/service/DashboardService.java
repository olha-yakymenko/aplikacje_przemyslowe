package com.techcorp.employee.service;

import com.techcorp.employee.dto.DashboardStatisticsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.OptionalDouble;

@Service
public class DashboardService {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DepartmentService departmentService;

    public DashboardStatisticsDTO getDashboardStatistics() {
        long totalEmployees = employeeService.getEmployeeCount();
        long totalDepartments = departmentService.getDepartmentCount();

        double averageSalary = employeeService.calculateAverageSalary();
//                .orElse(0.0);

        return new DashboardStatisticsDTO(totalEmployees, averageSalary, totalDepartments);
    }
}