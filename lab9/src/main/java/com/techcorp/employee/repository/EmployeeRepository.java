package com.techcorp.employee.repository;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.dto.EmployeeListView;
import com.techcorp.employee.dto.CompanyStatisticsDTO;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    // ✅ DODANE: Usuwanie po email
    void deleteByEmail(String email);

    List<Employee> findByCompany(String company);

    List<Employee> findByDepartmentId(Long departmentId);

    List<Employee> findByDepartmentIsNull();

    // ✅ DODANE: Metody dla paginacji
    Page<Employee> findByStatus(EmploymentStatus status, Pageable pageable);

    Page<Employee> findByCompany(String company, Pageable pageable);

    // ✅ DODANE: Metoda bez paginacji dla kompatybilności
    List<Employee> findByStatus(EmploymentStatus status);

    // Optymalizacja: Projekcja dla listy pracowników
//    // W EmployeeRepository ZMIEŃ:
//    @Query("SELECT e.name as name, e.email as email, e.company as company, " +
//            "e.position as position, e.salary as salary, e.status as status, " +
//            "COALESCE(e.department.name, 'Brak departamentu') as departmentName " +
//            "FROM Employee e LEFT JOIN e.department")
//    // <-- ZMIANA: LEFT JOIN!
//    Page<EmployeeListView> findAllEmployeesSummary(Pageable pageable);

    @Query("SELECT e.name as name, e.email as email, e.company as company, " +
            "e.position as position, e.salary as salary, e.status as status, " +
            "COALESCE(e.department.name, 'Brak departamentu') as departmentName " +
            "FROM Employee e LEFT JOIN e.department")
    Page<EmployeeListView> findAllEmployeesSummary(Pageable pageable);

    // ===== OPERACJE MATEMATYCZNE W SQL =====

    @Query("SELECT AVG(e.salary) FROM Employee e")
    Double findAverageSalary();

    @Query("SELECT AVG(e.salary) FROM Employee e WHERE e.company = :company")
    Double findAverageSalaryByCompany(@Param("company") String company);

    @Query("SELECT MAX(e.salary) FROM Employee e")
    Double findMaxSalary();

    @Query("SELECT MAX(e.salary) FROM Employee e WHERE e.company = :company")
    Double findMaxSalaryByCompany(@Param("company") String company);

    @Query("SELECT MIN(e.salary) FROM Employee e")
    Double findMinSalary();

    @Query("SELECT SUM(e.salary) FROM Employee e")
    Double findTotalSalaryCost();

    @Query("SELECT SUM(e.salary) FROM Employee e WHERE e.company = :company")
    Double findTotalSalaryCostByCompany(@Param("company") String company);

    @Query("SELECT COUNT(e) FROM Employee e")
    Long countAllEmployees();

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.company = :company")
    Long countEmployeesByCompany(@Param("company") String company);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.status = :status")
    Long countEmployeesByStatus(@Param("status") EmploymentStatus status);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.position = :position")
    Long countEmployeesByPosition(@Param("position") Position position);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId")
    Long countEmployeesByDepartment(@Param("departmentId") Long departmentId);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department IS NULL")
    Long countEmployeesWithoutDepartment();

    // STATYSTYKI GRUPUJĄCE
    @Query("SELECT e.company, COUNT(e), AVG(e.salary), MAX(e.salary), MIN(e.salary) " +
            "FROM Employee e GROUP BY e.company")
    List<Object[]> getCompanyStatistics();

    // ✅ DODANE: Statystyki z CompanyStatisticsDTO
    @Query("SELECT NEW com.techcorp.employee.dto.CompanyStatisticsDTO(" +
            "e.company, " +
            "COUNT(e), " +
            "AVG(e.salary), " +
            "MAX(e.salary), " +
            "(SELECT e2.name FROM Employee e2 WHERE e2.company = e.company AND e2.salary = (SELECT MAX(e3.salary) FROM Employee e3 WHERE e3.company = e.company))" +
            ") " +
            "FROM Employee e GROUP BY e.company")
    List<CompanyStatisticsDTO> getCompanyStatisticsDTO();

    @Query("SELECT NEW com.techcorp.employee.dto.CompanyStatisticsDTO(" +
            ":company, " +
            "COUNT(e), " +
            "AVG(e.salary), " +
            "MAX(e.salary), " +
            "(SELECT e2.name FROM Employee e2 WHERE e2.company = :company AND e2.salary = (SELECT MAX(e3.salary) FROM Employee e3 WHERE e3.company = :company))" +
            ") " +
            "FROM Employee e WHERE e.company = :company GROUP BY e.company")
    Optional<CompanyStatisticsDTO> getCompanyStatisticsDTO(@Param("company") String company);


    @Query("SELECT e.position, COUNT(e), AVG(e.salary), MAX(e.salary), MIN(e.salary) " +
            "FROM Employee e GROUP BY e.position")
    List<Object[]> getPositionStatistics();

    @Query("SELECT e.status, COUNT(e), AVG(e.salary) " +
            "FROM Employee e GROUP BY e.status")
    List<Object[]> getStatusStatistics();

    // ZAAWANSOWANE WYSZUKIWANIE
    @Query("SELECT e FROM Employee e WHERE " +
            "(:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:company IS NULL OR LOWER(e.company) = LOWER(:company)) AND " +
            "(:position IS NULL OR e.position = :position) AND " +
            "(:minSalary IS NULL OR e.salary >= :minSalary) AND " +
            "(:maxSalary IS NULL OR e.salary <= :maxSalary)")
    Page<Employee> findEmployeesWithFilters(
            @Param("name") String name,
            @Param("company") String company,
            @Param("position") Position position,
            @Param("minSalary") Double minSalary,
            @Param("maxSalary") Double maxSalary,
            Pageable pageable);

    // ✅ DODANE: Optymalizowane wyszukiwanie z projekcją
//    @Query("SELECT e.name as name, e.email as email, e.position as position, " +
//            "COALESCE(e.department.name, 'Brak departamentu') as departmentName " +
//            "FROM Employee e WHERE " +
//            "(:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
//            "(:company IS NULL OR LOWER(e.company) = LOWER(:company)) AND " +
//            "(:position IS NULL OR e.position = :position) AND " +
//            "(:minSalary IS NULL OR e.salary >= :minSalary) AND " +
//            "(:maxSalary IS NULL OR e.salary <= :maxSalary)")
//    Page<EmployeeListView> findEmployeesWithFiltersOptimized(
//            @Param("name") String name,
//            @Param("company") String company,
//            @Param("position") Position position,
//            @Param("minSalary") Double minSalary,
//            @Param("maxSalary") Double maxSalary,
//            Pageable pageable);

    @Query("SELECT e.name as name, e.email as email, e.company as company, " +
            "e.position as position, e.salary as salary, e.status as status, " +
            "COALESCE(e.department.name, 'Brak departamentu') as departmentName " +
            "FROM Employee e LEFT JOIN e.department WHERE " +  // <-- DODANO LEFT JOIN i pola!
            "(:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:company IS NULL OR LOWER(e.company) = LOWER(:company)) AND " +
            "(:position IS NULL OR e.position = :position) AND " +
            "(:minSalary IS NULL OR e.salary >= :minSalary) AND " +
            "(:maxSalary IS NULL OR e.salary <= :maxSalary)")
    Page<EmployeeListView> findEmployeesWithFiltersOptimized(
            @Param("name") String name,
            @Param("company") String company,
            @Param("position") Position position,
            @Param("minSalary") Double minSalary,
            @Param("maxSalary") Double maxSalary,
            Pageable pageable);

    // Znajdź pracownika z najwyższą pensją
    @Query("SELECT e FROM Employee e WHERE e.salary = (SELECT MAX(e2.salary) FROM Employee e2)")
    List<Employee> findHighestPaidEmployees();

    @Query("SELECT e FROM Employee e WHERE e.salary = (SELECT MAX(e2.salary) FROM Employee e2 WHERE e2.company = :company)")
    List<Employee> findHighestPaidEmployeesByCompany(@Param("company") String company);

    // Znajdź pracowników z pensją poniżej średniej
    @Query("SELECT e FROM Employee e WHERE e.salary < (SELECT AVG(e2.salary) FROM Employee e2)")
    List<Employee> findEmployeesBelowAverageSalary();

    // TOP 10 najlepiej zarabiających
    @Query(value = "SELECT e FROM Employee e ORDER BY e.salary DESC")
    List<Employee> findTop10HighestPaidEmployees(Pageable pageable);

    // ✅ DODANE: Metody pomocnicze
    @Query("SELECT e FROM Employee e WHERE e.department.name = :departmentName")
    List<Employee> findByDepartmentName(@Param("departmentName") String departmentName);

    @Query("SELECT e FROM Employee e WHERE e.salary BETWEEN :minSalary AND :maxSalary")
    List<Employee> findBySalaryBetween(@Param("minSalary") Double minSalary, @Param("maxSalary") Double maxSalary);

    long countByCompany(String company);

    // ✅ DODANE: Metody dla dostępnych menedżerów
    @Query("SELECT e FROM Employee e WHERE e.position IN (com.techcorp.employee.model.Position.MANAGER, " +
            "com.techcorp.employee.model.Position.VICE_PRESIDENT, com.techcorp.employee.model.Position.PRESIDENT)")
    List<Employee> findAvailableManagers();


    // ✅ DODANE: Zaawansowane wyszukiwanie z PROJEKCJĄ (pełna wersja)
    @Query("SELECT e.name as name, e.email as email, e.company as company, " +
            "e.position as position, e.salary as salary, e.status as status, " +
            "COALESCE(e.department.name, 'Brak departamentu') as departmentName " +
            "FROM Employee e LEFT JOIN e.department WHERE " +  // <-- LEFT JOIN!
            "(:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:company IS NULL OR LOWER(e.company) = LOWER(:company)) AND " +
            "(:position IS NULL OR e.position = :position) AND " +
            "(:status IS NULL OR e.status = :status) AND " +
            "(:minSalary IS NULL OR e.salary >= :minSalary) AND " +
            "(:maxSalary IS NULL OR e.salary <= :maxSalary) AND " +
            "(:departmentName IS NULL OR " +
            "(:departmentName = 'Brak departamentu' AND e.department IS NULL) OR " +
            "(e.department IS NOT NULL AND LOWER(e.department.name) = LOWER(:departmentName)))")  // <-- POPRAWIONE!
    Page<EmployeeListView> findEmployeesWithFiltersProjection(
            @Param("name") String name,
            @Param("company") String company,
            @Param("position") Position position,
            @Param("status") EmploymentStatus status,
            @Param("minSalary") Double minSalary,
            @Param("maxSalary") Double maxSalary,
            @Param("departmentName") String departmentName,
            Pageable pageable);

//    // ✅ DODANE: Pracownicy według statusu z PROJEKCJĄ
//    @Query("SELECT e.name as name, e.email as email, e.company as company, " +
//            "e.position as position, e.salary as salary, e.status as status, " +
//            "COALESCE(e.department.name, 'Brak departamentu') as departmentName " +
//            "FROM Employee e WHERE e.status = :status")
//    Page<EmployeeListView> findByStatusProjection(
//            @Param("status") EmploymentStatus status,
//            Pageable pageable);
//
//    // ✅ POPRAWIONE: Użyj LEFT JOIN zamiast JOIN
//    @Query("SELECT e.name as name, e.email as email, e.company as company, " +
//            "e.position as position, e.salary as salary, e.status as status, " +
//            "COALESCE(e.department.name, 'Brak departamentu') as departmentName " +
//            "FROM Employee e LEFT JOIN e.department WHERE e.company = :company")
//    // <-- LEFT JOIN!
//    Page<EmployeeListView> findByCompanyProjection(
//            @Param("company") String company,
//            Pageable pageable);


    @Query("SELECT e.name as name, e.email as email, e.company as company, " +
            "e.position as position, e.salary as salary, e.status as status, " +
            "COALESCE(e.department.name, 'Brak departamentu') as departmentName " +
            "FROM Employee e LEFT JOIN e.department WHERE e.status = :status")  // <-- LEFT JOIN!
    Page<EmployeeListView> findByStatusProjection(
            @Param("status") EmploymentStatus status,
            Pageable pageable);

    // ✅ POPRAWIONE: Pracownicy według firmy z PROJEKCJĄ
    @Query("SELECT e.name as name, e.email as email, e.company as company, " +
            "e.position as position, e.salary as salary, e.status as status, " +
            "COALESCE(e.department.name, 'Brak departamentu') as departmentName " +
            "FROM Employee e LEFT JOIN e.department WHERE e.company = :company")  // <-- LEFT JOIN!
    Page<EmployeeListView> findByCompanyProjection(
            @Param("company") String company,
            Pageable pageable);



}