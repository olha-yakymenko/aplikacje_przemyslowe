package com.techcorp.employee.service;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.repository.EmployeeViewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeViewServiceTest {

    @Mock
    private EmployeeViewRepository employeeViewRepository;

    @InjectMocks
    private EmployeeViewService employeeViewService;

    @Test
    void findAll_ShouldReturnAllEmployees() {
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        List<Employee> expectedEmployees = Arrays.asList(employee1, employee2);

        when(employeeViewRepository.findAll()).thenReturn(expectedEmployees);

        List<Employee> result = employeeViewService.findAll();

        assertAll(
                () -> assertThat(result).isEqualTo(expectedEmployees),
                () -> verify(employeeViewRepository, times(1)).findAll()
        );
    }

    @Test
    void findById_WhenEmployeeExists_ShouldReturnEmployee() {
        Long employeeId = 1L;
        Employee expectedEmployee = new Employee();
        expectedEmployee.setId(employeeId);

        when(employeeViewRepository.findById(employeeId)).thenReturn(Optional.of(expectedEmployee));

        Optional<Employee> result = employeeViewService.findById(employeeId);

        assertAll(
                () -> assertThat(result).isPresent(),
                () -> assertThat(result.get()).isEqualTo(expectedEmployee),
                () -> verify(employeeViewRepository, times(1)).findById(employeeId)
        );
    }

    @Test
    void findById_WhenEmployeeNotExists_ShouldReturnEmpty() {
        Long employeeId = 999L;

        when(employeeViewRepository.findById(employeeId)).thenReturn(Optional.empty());

        Optional<Employee> result = employeeViewService.findById(employeeId);

        assertAll(
                () -> assertThat(result).isEmpty(),
                () -> verify(employeeViewRepository, times(1)).findById(employeeId)
        );
    }

    @Test
    void findByCompany_ShouldReturnEmployeesByCompany() {
        String companyName = "TechCorp";
        Employee employee1 = new Employee();
        employee1.setCompany(companyName);
        Employee employee2 = new Employee();
        employee2.setCompany(companyName);
        List<Employee> expectedEmployees = Arrays.asList(employee1, employee2);

        when(employeeViewRepository.findByCompany(companyName)).thenReturn(expectedEmployees);

        List<Employee> result = employeeViewService.findByCompany(companyName);

        assertAll(
                () -> assertThat(result).isEqualTo(expectedEmployees),
                () -> verify(employeeViewRepository, times(1)).findByCompany(companyName)
        );
    }

    @Test
    void findByCompany_WhenNoEmployees_ShouldReturnEmptyList() {
        String companyName = "NonExistentCompany";

        when(employeeViewRepository.findByCompany(companyName)).thenReturn(Collections.emptyList());

        List<Employee> result = employeeViewService.findByCompany(companyName);

        assertAll(
                () -> assertThat(result).isEmpty(),
                () -> verify(employeeViewRepository, times(1)).findByCompany(companyName)
        );
    }

    @Test
    void findAllWithPageable_ShouldReturnFirstPage() {
        List<Employee> allEmployees = createEmployeesList(15);
        Pageable pageable = PageRequest.of(0, 10);

        when(employeeViewRepository.findAll()).thenReturn(allEmployees);

        Page<Employee> result = employeeViewService.findAll(pageable);

        assertAll(
                () -> assertThat(result.getContent()).hasSize(10),
                () -> assertThat(result.getTotalElements()).isEqualTo(15),
                () -> assertThat(result.getTotalPages()).isEqualTo(2),
                () -> assertThat(result.getNumber()).isEqualTo(0),
                () -> verify(employeeViewRepository, times(1)).findAll()
        );
    }

    @Test
    void findAllWithPageable_ShouldReturnSecondPage() {
        List<Employee> allEmployees = createEmployeesList(15);
        Pageable pageable = PageRequest.of(1, 10);

        when(employeeViewRepository.findAll()).thenReturn(allEmployees);

        Page<Employee> result = employeeViewService.findAll(pageable);

        assertAll(
                () -> assertThat(result.getContent()).hasSize(5),
                () -> assertThat(result.getTotalElements()).isEqualTo(15),
                () -> assertThat(result.getTotalPages()).isEqualTo(2),
                () -> assertThat(result.getNumber()).isEqualTo(1),
                () -> verify(employeeViewRepository, times(1)).findAll()
        );
    }

    @Test
    void findAllWithPageable_WhenEmptyList_ShouldReturnEmptyPage() {
        List<Employee> allEmployees = Collections.emptyList();
        Pageable pageable = PageRequest.of(0, 10);

        when(employeeViewRepository.findAll()).thenReturn(allEmployees);

        Page<Employee> result = employeeViewService.findAll(pageable);

        assertAll(
                () -> assertThat(result.getContent()).isEmpty(),
                () -> assertThat(result.getTotalElements()).isZero(),
                () -> assertThat(result.getTotalPages()).isZero(),
                () -> verify(employeeViewRepository, times(1)).findAll()
        );
    }

    @Test
    void findAllWithPageable_ShouldReturnFullPageWhenExactMatch() {
        List<Employee> allEmployees = createEmployeesList(10);
        Pageable pageable = PageRequest.of(0, 10);

        when(employeeViewRepository.findAll()).thenReturn(allEmployees);

        Page<Employee> result = employeeViewService.findAll(pageable);

        assertAll(
                () -> assertThat(result.getContent()).hasSize(10),
                () -> assertThat(result.getTotalElements()).isEqualTo(10),
                () -> assertThat(result.getTotalPages()).isEqualTo(1),
                () -> verify(employeeViewRepository, times(1)).findAll()
        );
    }

    @Test
    void count_ShouldReturnTotalNumberOfEmployees() {
        List<Employee> allEmployees = createEmployeesList(7);

        when(employeeViewRepository.findAll()).thenReturn(allEmployees);

        long result = employeeViewService.count();

        assertAll(
                () -> assertThat(result).isEqualTo(7),
                () -> verify(employeeViewRepository, times(1)).findAll()
        );
    }

    @Test
    void count_WhenEmpty_ShouldReturnZero() {
        when(employeeViewRepository.findAll()).thenReturn(Collections.emptyList());

        long result = employeeViewService.count();

        assertAll(
                () -> assertThat(result).isZero(),
                () -> verify(employeeViewRepository, times(1)).findAll()
        );
    }

    private List<Employee> createEmployeesList(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> {
                    Employee employee = new Employee();
                    employee.setId((long) i);
                    employee.setName("Employee " + i);
                    employee.setCompany("Company " + (i % 3));
                    return employee;
                })
                .collect(java.util.stream.Collectors.toList());
    }
}