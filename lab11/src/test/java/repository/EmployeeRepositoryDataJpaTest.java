package com.techcorp.employee.repository;

import com.techcorp.employee.model.*;
import com.techcorp.employee.dto.EmployeeListView;
import com.techcorp.employee.dto.CompanyStatisticsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
@ActiveProfiles("test")
class EmployeeRepositoryDataJpaTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department itDepartment;
    private Department hrDepartment;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();

        itDepartment = new Department("IT", "Warsaw", "IT Department", "it.manager@company.com", 150000.0);
        hrDepartment = new Department("HR", "Krakow", "Human Resources", "hr.manager@company.com", 80000.0);

        departmentRepository.saveAll(List.of(itDepartment, hrDepartment));

        Employee emp1 = new Employee("Jan Kowalski", "jan.kowalski@techcorp.com", "TechCorp",
                Position.PROGRAMMER, new BigDecimal(8500.0), EmploymentStatus.ACTIVE, itDepartment);
        Employee emp2 = new Employee("Anna Nowak", "anna.nowak@techcorp.com", "TechCorp",
                Position.MANAGER, new BigDecimal(12000.0), EmploymentStatus.ACTIVE, hrDepartment);
        Employee emp3 = new Employee("Piotr Wiśniewski", "piotr.wisniewski@techcorp.com", "OtherCorp",
                Position.PROGRAMMER, new BigDecimal(7500.0), EmploymentStatus.TERMINATED, null);
        Employee emp4 = new Employee("Maria Lewandowska", "maria.lewandowska@techcorp.com", "TechCorp",
                Position.PRESIDENT, new BigDecimal(25000.0), EmploymentStatus.ACTIVE, null);
        Employee emp5 = new Employee("Tomasz Wójcik", "tomasz.wojcik@techcorp.com", "TechCorp",
                Position.PROGRAMMER, new BigDecimal(9000.0), EmploymentStatus.ON_LEAVE, itDepartment);
        Employee emp6 = new Employee("Katarzyna Zielińska", "k.zielinska@techcorp.com", "OtherCorp",
                Position.MANAGER, new BigDecimal(11000.0), EmploymentStatus.ACTIVE, null);

        employeeRepository.saveAll(List.of(emp1, emp2, emp3, emp4, emp5, emp6));

        employeeRepository.flush();
        departmentRepository.flush();
    }

    // ========== METODY DERIVED QUERY ==========

    @Test
    void testFindByEmail() {
        Optional<Employee> result = employeeRepository.findByEmail("jan.kowalski@techcorp.com");

        assertAll(
                () -> assertThat(result).isPresent(),
                () -> assertThat(result.get().getName()).isEqualTo("Jan Kowalski"),
                () -> assertThat(result.get().getCompany()).isEqualTo("TechCorp"),
                () -> assertThat(result.get().getPosition()).isEqualTo(Position.PROGRAMMER)
        );
    }

    @Test
    void testExistsByEmail() {
        boolean exists = employeeRepository.existsByEmail("anna.nowak@techcorp.com");
        assertThat(exists).isTrue();
    }

    @Test
    void testDeleteByEmail() {
        employeeRepository.deleteByEmail("jan.kowalski@techcorp.com");
        employeeRepository.flush();

        Optional<Employee> deleted = employeeRepository.findByEmail("jan.kowalski@techcorp.com");
        assertThat(deleted).isEmpty();
    }

    @Test
    void testFindByCompany() {
        List<Employee> result = employeeRepository.findByCompany("TechCorp");

        assertAll(
                () -> assertThat(result).hasSize(4),
                () -> assertThat(result).extracting(Employee::getCompany).containsOnly("TechCorp"),
                () -> assertThat(result).extracting(Employee::getName)
                        .contains("Jan Kowalski", "Anna Nowak", "Maria Lewandowska", "Tomasz Wójcik")
        );
    }

    @Test
    void testFindByDepartmentId() {
        List<Employee> result = employeeRepository.findByDepartmentId(itDepartment.getId());

        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(Employee::getName)
                        .containsExactlyInAnyOrder("Jan Kowalski", "Tomasz Wójcik")
        );
    }

    @Test
    void testFindByDepartmentIsNull() {
        List<Employee> result = employeeRepository.findByDepartmentIsNull();

        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result).extracting(Employee::getName)
                        .containsExactlyInAnyOrder("Piotr Wiśniewski", "Maria Lewandowska", "Katarzyna Zielińska")
        );
    }

    @Test
    void testFindByStatusWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> result = employeeRepository.findByStatus(EmploymentStatus.ACTIVE, pageable);

        assertAll(
                () -> assertThat(result.getTotalElements()).isEqualTo(4),
                () -> assertThat(result.getContent()).extracting(Employee::getStatus)
                        .containsOnly(EmploymentStatus.ACTIVE)
        );
    }

    @Test
    void testFindByCompanyWithPagination() {
        Pageable pageable = PageRequest.of(0, 3, Sort.by("name"));
        Page<Employee> result = employeeRepository.findByCompany("TechCorp", pageable);

        assertAll(
                () -> assertThat(result.getTotalElements()).isEqualTo(4),
                () -> assertThat(result.getTotalPages()).isEqualTo(2),
                () -> assertThat(result.getContent()).hasSize(3),
                () -> assertThat(result.getContent()).extracting(Employee::getCompany)
                        .containsOnly("TechCorp")
        );
    }

    @Test
    void testFindByStatus() {
        List<Employee> result = employeeRepository.findByStatus(EmploymentStatus.ACTIVE);

        assertAll(
                () -> assertThat(result).hasSize(4),
                () -> assertThat(result).extracting(Employee::getStatus)
                        .containsOnly(EmploymentStatus.ACTIVE)
        );
    }

    // ========== METODY Z @Query ==========

    @Test
    void testFindAllEmployeesSummary() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<EmployeeListView> result = employeeRepository.findAllEmployeesSummary(pageable);

        assertAll(
                () -> assertThat(result.getTotalElements()).isEqualTo(6),
                () -> assertThat(result.getContent()).isNotEmpty(),
                () -> result.getContent().forEach(employee -> {
                    assertThat(employee.getName()).isNotNull();
                    assertThat(employee.getPosition()).isNotNull();
                    assertThat(employee.getCompany()).isNotNull();
                    assertThat(employee.getDepartmentName()).isNotNull();
                })
        );
    }

//    @Test
//    void testFindAverageSalary() {
//        Double result = employeeRepository.findAverageSalary();
//
//        // Oblicz ręcznie średnią dla weryfikacji
//        double expectedAvg = (8500.0 + 12000.0 + 7500.0 + 25000.0 + 9000.0 + 11000.0) / 6;
//
//        assertThat(result).isEqualTo(expectedAvg);
//    }

    @Test
    void testFindAverageSalaryByCompany() {
        Double result = employeeRepository.findAverageSalaryByCompany("TechCorp");

        // Średnia dla TechCorp: (8500 + 12000 + 25000 + 9000) / 4 = 13625
        assertThat(result).isEqualTo(13625.0);
    }

    @Test
    void testFindMaxSalary() {
        Double result = employeeRepository.findMaxSalary();
        assertThat(result).isEqualTo(25000.0);
    }

    @Test
    void testFindMaxSalaryByCompany() {
        Double result = employeeRepository.findMaxSalaryByCompany("OtherCorp");
        assertThat(result).isEqualTo(11000.0);
    }

    @Test
    void testFindMinSalary() {
        Double result = employeeRepository.findMinSalary();
        assertThat(result).isEqualTo(7500.0);
    }

    @Test
    void testFindTotalSalaryCost() {
        Double result = employeeRepository.findTotalSalaryCost();
        double expectedTotal = 8500.0 + 12000.0 + 7500.0 + 25000.0 + 9000.0 + 11000.0;
        assertThat(result).isEqualTo(expectedTotal);
    }

    @Test
    void testFindTotalSalaryCostByCompany() {
        Double result = employeeRepository.findTotalSalaryCostByCompany("TechCorp");
        double expectedTotal = 8500.0 + 12000.0 + 25000.0 + 9000.0;
        assertThat(result).isEqualTo(expectedTotal);
    }

    @Test
    void testCountAllEmployees() {
        Long result = employeeRepository.countAllEmployees();
        assertThat(result).isEqualTo(6L);
    }

    @Test
    void testCountEmployeesByCompany() {
        Long result = employeeRepository.countEmployeesByCompany("OtherCorp");
        assertThat(result).isEqualTo(2L);
    }

    @Test
    void testCountEmployeesByStatus() {
        Long result = employeeRepository.countEmployeesByStatus(EmploymentStatus.ACTIVE);
        assertThat(result).isEqualTo(4L);
    }

    @Test
    void testCountEmployeesByPosition() {
        Long result = employeeRepository.countEmployeesByPosition(Position.PROGRAMMER);
        assertThat(result).isEqualTo(3L);
    }

    @Test
    void testCountEmployeesByDepartment() {
        Long result = employeeRepository.countEmployeesByDepartment(itDepartment.getId());
        assertThat(result).isEqualTo(2L);
    }

    @Test
    void testCountEmployeesWithoutDepartment() {
        Long result = employeeRepository.countEmployeesWithoutDepartment();
        assertThat(result).isEqualTo(3L);
    }

//    @Test
//    void testGetCompanyStatistics() {
//        List<Object[]> result = employeeRepository.getCompanyStatistics();
//
//        assertAll(
//                () -> assertThat(result).hasSize(2), // Dwie firmy
//                () -> {
//                    // Sprawdź statystyki dla TechCorp
//                    Object[] techCorpStats = result.stream()
//                            .filter(arr -> "TechCorp".equals(arr[0]))
//                            .findFirst()
//                            .orElse(null);
//
//                    assertThat(techCorpStats).isNotNull();
//                    assertThat(techCorpStats[0]).isEqualTo("TechCorp");
//                    assertThat(techCorpStats[1]).isEqualTo(4L); // count
//                    assertThat((Double)techCorpStats[2]).isEqualTo(13625.0); // avg
//                    assertThat((Double)techCorpStats[3]).isEqualTo(25000.0); // max
//                    assertThat((Double)techCorpStats[4]).isEqualTo(8500.0); // min
//                }
//        );
//    }

//    @Test
//    void testGetCompanyStatisticsDTO() {
//        List<CompanyStatisticsDTO> result = employeeRepository.getCompanyStatisticsDTO();
//
//        assertAll(
//                () -> assertThat(result).hasSize(2),
//                () -> {
//                    CompanyStatisticsDTO techCorpStats = result.stream()
//                            .filter(dto -> "TechCorp".equals(dto.getCompanyName()))
//                            .findFirst()
//                            .orElse(null);
//
//                    assertThat(techCorpStats).isNotNull();
//                    assertThat(techCorpStats.getEmployeeCount()).isEqualTo(4L);
//                    assertThat(techCorpStats.getAverageSalary()).isEqualTo(13625.0);
//
//                }
//        );
//    }
//
//    @Test
//    void testGetCompanyStatisticsDTOForSingleCompany() {
//        Optional<CompanyStatisticsDTO> result = employeeRepository.getCompanyStatisticsDTO("OtherCorp");
//
//        assertAll(
//                () -> assertThat(result).isPresent(),
//                () -> {
//                    CompanyStatisticsDTO dto = result.get();
//                    assertThat(dto.getCompanyName()).isEqualTo("OtherCorp");
//                    assertThat(dto.getEmployeeCount()).isEqualTo(2L);
//                    assertThat(dto.getAverageSalary()).isEqualTo(9250.0); // (7500 + 11000) / 2
//
//                }
//        );
//    }
//
  @Test
    void testGetPositionStatistics() {
        List<Object[]> result = employeeRepository.getPositionStatistics();

        assertAll(
                () -> assertThat(result).hasSize(3), // PROGRAMMER, MANAGER, PRESIDENT
                () -> {
                    Object[] programmerStats = result.stream()
                            .filter(arr -> Position.PROGRAMMER.equals(arr[0]))
                            .findFirst()
                            .orElse(null);

                    assertThat(programmerStats).isNotNull();
                    assertThat(programmerStats[0]).isEqualTo(Position.PROGRAMMER);
                    assertThat(programmerStats[1]).isEqualTo(3L); // count
                }
        );
    }

    @Test
    void testGetStatusStatistics() {
        List<Object[]> result = employeeRepository.getStatusStatistics();

        assertAll(
                () -> assertThat(result).hasSize(3), // ACTIVE, TERMINATED, ON_LEAVE
                () -> {
                    Object[] activeStats = result.stream()
                            .filter(arr -> EmploymentStatus.ACTIVE.equals(arr[0]))
                            .findFirst()
                            .orElse(null);

                    assertThat(activeStats).isNotNull();
                    assertThat(activeStats[0]).isEqualTo(EmploymentStatus.ACTIVE);
                    assertThat(activeStats[1]).isEqualTo(4L); // count
                }
        );
    }

//    @Test
//    void testFindHighestPaidEmployees() {
//        List<Employee> result = employeeRepository.findHighestPaidEmployees();
//
//        assertAll(
//                () -> assertThat(result).hasSize(1),
//                () -> assertThat(result.get(0).getName()).isEqualTo("Maria Lewandowska"),
//                () -> assertThat(result.get(0).getSalary()).isEqualTo(25000.0)
//        );
//    }

//    @Test
//    void testFindHighestPaidEmployeesByCompany() {
//        List<Employee> result = employeeRepository.findHighestPaidEmployeesByCompany("OtherCorp");
//
//        assertAll(
//                () -> assertThat(result).hasSize(1),
//                () -> assertThat(result.get(0).getName()).isEqualTo("Katarzyna Zielińska"),
//                () -> assertThat(result.get(0).getSalary()).isEqualTo(11000.0)
//        );
//    }

    @Test
    void testFindEmployeesBelowAverageSalary() {
        List<Employee> result = employeeRepository.findEmployeesBelowAverageSalary();

        assertThat(result).hasSize(5);
    }
//
//    @Test
//    void testFindTop10HighestPaidEmployees() {
//        Pageable pageable = PageRequest.of(0, 3);
//        List<Employee> result = employeeRepository.findTop10HighestPaidEmployees(pageable);
//
//        assertAll(
//                () -> assertThat(result).hasSize(3),
//                () -> assertThat(result.get(0).getSalary()).isEqualTo(25000.0),
//                () -> assertThat(result.get(1).getSalary()).isEqualTo(12000.0),
//                () -> assertThat(result.get(2).getSalary()).isEqualTo(11000.0)
//        );
//    }

    @Test
    void testFindByStatusProjection() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<EmployeeListView> result = employeeRepository.findByStatusProjection(EmploymentStatus.ACTIVE, pageable);

        assertAll(
                () -> assertThat(result.getTotalElements()).isEqualTo(4),
                () -> result.getContent().forEach(employee -> {
                    assertThat(employee.getName()).isNotNull();
                    assertThat(employee.getCompany()).isNotNull();
                })
        );
    }

    @Test
    void testFindByCompanyProjection() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<EmployeeListView> result = employeeRepository.findByCompanyProjection("TechCorp", pageable);

        assertAll(
                () -> assertThat(result.getTotalElements()).isEqualTo(4),
                () -> result.getContent().forEach(employee -> {
                    assertThat(employee.getCompany()).isEqualTo("TechCorp");
                })
        );
    }

    @Test
    void testFindDistinctCompanies() {
        List<String> result = employeeRepository.findDistinctCompanies();

        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).containsExactly("OtherCorp", "TechCorp") // posortowane ASC
        );
    }

    @Test
    void testFindAllProjection() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<EmployeeListView> result = employeeRepository.findAllProjection(pageable);

        assertAll(
                () -> assertThat(result.getTotalElements()).isEqualTo(6),
                () -> assertThat(result.getContent()).hasSize(6),
                () -> result.getContent().forEach(employee -> {
                    assertThat(employee.getName()).isNotNull();
                    assertThat(employee.getEmail()).isNotNull();
                    assertThat(employee.getPosition()).isNotNull();
                })
        );
    }

    @Test
    void testFindWithFiltersProjection() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<EmployeeListView> result = employeeRepository.findWithFiltersProjection(
                "Jan", // name filter
                "TechCorp", // company filter
                Position.PROGRAMMER, // position filter
                EmploymentStatus.ACTIVE, // status filter
                8000.0, // min salary
                10000.0, // max salary
                "IT", // department name
                pageable
        );

        assertAll(
                () -> assertThat(result.getTotalElements()).isEqualTo(1),
                () -> {
                    EmployeeListView employee = result.getContent().get(0);
                    assertThat(employee.getName()).isEqualTo("Jan Kowalski");
                    assertThat(employee.getCompany()).isEqualTo("TechCorp");
                    assertThat(employee.getPosition()).isEqualTo("PROGRAMMER");
                    assertThat(employee.getDepartmentName()).isEqualTo("IT");
                }
        );
    }

    @Test
    void testFindWithFiltersProjection_NoDepartment() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<EmployeeListView> result = employeeRepository.findWithFiltersProjection(
                null, // name filter
                "OtherCorp", // company filter
                null, // position filter
                null, // status filter
                null, // min salary
                null, // max salary
                "Brak departamentu", // department name
                pageable
        );

        assertAll(
                () -> assertThat(result.getTotalElements()).isEqualTo(2),
                () -> result.getContent().forEach(employee -> {
                    assertThat(employee.getCompany()).isEqualTo("OtherCorp");
                    assertThat(employee.getDepartmentName()).isEqualTo("Brak departamentu");
                })
        );
    }

    // ========== TESTY SPECIFICATION ==========

    @Test
    void testSpecificationWithMultipleFilters() {
        Specification<Employee> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("company"), "TechCorp"),
                        criteriaBuilder.equal(root.get("status"), EmploymentStatus.ACTIVE),
                        criteriaBuilder.greaterThanOrEqualTo(root.get("salary"), 10000.0)
                );

        List<Employee> result = employeeRepository.findAll(spec);

        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(Employee::getName)
                        .containsExactlyInAnyOrder("Anna Nowak", "Maria Lewandowska"),
                () -> result.forEach(employee -> {
                    assertThat(employee.getCompany()).isEqualTo("TechCorp");
                    assertThat(employee.getStatus()).isEqualTo(EmploymentStatus.ACTIVE);
                    assertThat(employee.getSalary()).isGreaterThanOrEqualTo(new BigDecimal(10000.0));
                })
        );
    }

    // ========== TESTY PAGINACJI ==========

    @Test
    void testPaginationWithSorting() {
        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "salary"));
        Page<Employee> result = employeeRepository.findAll(pageable);

        assertAll(
                () -> assertThat(result.getTotalElements()).isEqualTo(6),
                () -> assertThat(result.getTotalPages()).isEqualTo(2),
                () -> assertThat(result.getContent()).hasSize(3),
                () -> {
                    List<BigDecimal> salaries = result.getContent().stream()
                            .map(Employee::getSalary)
                            .toList();
                    assertThat(salaries).containsExactly(new BigDecimal(25000.0), new BigDecimal(12000.0), new BigDecimal(11000.0));
                }
        );
    }

    @Test
    void testPaginationSecondPage() {
        Pageable pageable = PageRequest.of(1, 4, Sort.by("name"));
        Page<Employee> result = employeeRepository.findAll(pageable);

        assertAll(
                () -> assertThat(result.getNumber()).isEqualTo(1),
                () -> assertThat(result.getSize()).isEqualTo(4),
                () -> assertThat(result.getTotalElements()).isEqualTo(6),
                () -> assertThat(result.getTotalPages()).isEqualTo(2),
                () -> assertThat(result.getContent()).hasSize(2) // Druga strona ma 2 elementy
        );
    }
}