package com.techcorp.employee.repository;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.dto.EmployeeListView;
import com.techcorp.employee.dto.CompanyStatisticsDTO;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import groovyjarjarantlr4.v4.runtime.misc.Nullable;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    void deleteByEmail(String email);

    List<Employee> findByCompany(String company);

    List<Employee> findByDepartmentId(Long departmentId);

    List<Employee> findByDepartmentIsNull();

    Page<Employee> findByStatus(EmploymentStatus status, Pageable pageable);

    Page<Employee> findByCompany(String company, Pageable pageable);

    List<Employee> findByStatus(EmploymentStatus status);

    @Query("SELECT e.name as name, " +
            "e.position as position, " +
            "e.company as company, " +
            "COALESCE(e.department.name, 'Brak departamentu') as departmentName " +
            "FROM Employee e LEFT JOIN e.department")
    Page<EmployeeListView> findAllEmployeesSummary(Pageable pageable);

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


//    @Query("SELECT NEW com.techcorp.employee.dto.CompanyStatisticsDTO(" +
//            "e.company, " +
//            "COUNT(e), " +
//            "AVG(e.salary), " +
//            "MAX(e.salary), " +
//            "(SELECT e2.name FROM Employee e2 " +
//            " WHERE e2.company = e.company " +
//            " ORDER BY e2.salary DESC LIMIT 1)" +
//            ") " +
//            "FROM Employee e GROUP BY e.company")
//    List<CompanyStatisticsDTO> getCompanyStatisticsDTO();

//    @Query("""
//    SELECT NEW com.techcorp.employee.dto.CompanyStatisticsDTO(
//        e.company,
//        COUNT(e),
//        AVG(e.salary),
//        MAX(e.salary),
//        (
//            SELECT e2.name
//            FROM Employee e2
//            WHERE e2.company = e.company
//              AND e2.salary = (
//                  SELECT MAX(e3.salary)
//                  FROM Employee e3
//                  WHERE e3.company = e.company
//              )
//        )
//    )
//    FROM Employee e
//    GROUP BY e.company
//""")
//    List<CompanyStatisticsDTO> getCompanyStatisticsDTO();
//

    @Query("""
    SELECT NEW com.techcorp.employee.dto.CompanyStatisticsDTO(
        e.company,
        COUNT(e),
        AVG(e.salary),
        MAX(e.salary),
        COALESCE(
            (SELECT e2.name FROM Employee e2 
             WHERE e2.company = e.company 
             ORDER BY e2.salary DESC LIMIT 1),
            'Brak'
        )
    )
    FROM Employee e
    GROUP BY e.company
""")
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



    @Query("SELECT e FROM Employee e WHERE e.salary = (SELECT MAX(e2.salary) FROM Employee e2)")
    List<Employee> findHighestPaidEmployees();

    @Query("SELECT e FROM Employee e WHERE e.salary = (SELECT MAX(e2.salary) FROM Employee e2 WHERE e2.company = :company)")
    List<Employee> findHighestPaidEmployeesByCompany(@Param("company") String company);

    @Query("SELECT e FROM Employee e WHERE e.salary < (SELECT AVG(e2.salary) FROM Employee e2)")
    List<Employee> findEmployeesBelowAverageSalary();

    @Query(value = "SELECT e FROM Employee e ORDER BY e.salary DESC")
    List<Employee> findTop10HighestPaidEmployees(Pageable pageable);


    @Query("SELECT e.name as name, " +
            "e.position as position, " +
            "e.company as company, " +
            "COALESCE(e.department.name, 'Brak departamentu') as departmentName " +
            "FROM Employee e LEFT JOIN e.department WHERE e.status = :status")
    Page<EmployeeListView> findByStatusProjection(
            @Param("status") EmploymentStatus status,
            Pageable pageable);

    @Query("SELECT e.name as name, " +
            "e.position as position, " +
            "e.company as company, " +
            "COALESCE(e.department.name, 'Brak departamentu') as departmentName " +
            "FROM Employee e LEFT JOIN e.department WHERE e.company = :company")
    Page<EmployeeListView> findByCompanyProjection(
            @Param("company") String company,
            Pageable pageable);


    // Dla getAllUniqueCompanies()
    @Query("SELECT DISTINCT e.company FROM Employee e ORDER BY e.company ASC")
    List<String> findDistinctCompanies();



    @Query("SELECT e.name as name, " +
            "e.email as email, " +
            "e.position as position, " +
            "e.company as company, " +
            "COALESCE(e.department.name, 'Brak departamentu') as departmentName " +
            "FROM Employee e LEFT JOIN e.department")
    Page<EmployeeListView> findAllProjection(Pageable pageable);






    @Query("""
        SELECT 
            e.name as name, 
            e.position as position, 
            e.company as company,
            COALESCE(e.department.name, 'Brak departamentu') as departmentName
        FROM Employee e 
        LEFT JOIN e.department d
        WHERE 
            (:name IS NULL OR :name = '' OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')))
            AND (:company IS NULL OR :company = '' OR LOWER(e.company) = LOWER(:company))
            AND (:position IS NULL OR e.position = :position)
            AND (:status IS NULL OR e.status = :status)
            AND (:minSalary IS NULL OR e.salary >= :minSalary)
            AND (:maxSalary IS NULL OR e.salary <= :maxSalary)
            AND (
                :departmentName IS NULL 
                OR :departmentName = ''
                OR (
                    LOWER(:departmentName) = 'brak departamentu' 
                    AND e.department IS NULL
                )
                OR (
                    LOWER(:departmentName) != 'brak departamentu' 
                    AND LOWER(e.department.name) = LOWER(:departmentName)
                )
            )
        ORDER BY e.name ASC
    """)
    Page<EmployeeListView> findWithFiltersProjection(
            @Param("name") String name,
            @Param("company") String company,
            @Param("position") Position position,
            @Param("status") EmploymentStatus status,
            @Param("minSalary") Double minSalary,
            @Param("maxSalary") Double maxSalary,
            @Param("departmentName") String departmentName,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Employee e WHERE e.id = :id")
    Optional<Employee> findByIdWithLock(@Param("id") Long id);
}










//package com.techcorp.employee.repository;
//
//import com.techcorp.employee.model.Employee;
//import com.techcorp.employee.dto.EmployeeListView;
//import com.techcorp.employee.dto.CompanyStatisticsDTO;
//import com.techcorp.employee.model.Position;
//import com.techcorp.employee.model.EmploymentStatus;
//import groovyjarjarantlr4.v4.runtime.misc.Nullable;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
//
//    Optional<Employee> findByEmail(String email);
//
//    boolean existsByEmail(String email);
//
//    void deleteByEmail(String email);
//
//    List<Employee> findByCompany(String company);
//
//    List<Employee> findByDepartmentId(Long departmentId);
//
//    List<Employee> findByDepartmentIsNull();
//
//    Page<Employee> findByStatus(EmploymentStatus status, Pageable pageable);
//
//    Page<Employee> findByCompany(String company, Pageable pageable);
//
//    List<Employee> findByStatus(EmploymentStatus status);
//
//    @Query("SELECT e.name as name, e.email as email, e.company as company, " +
//            "e.position as position, e.salary as salary, e.status as status, " +
//            "COALESCE(e.department.name, 'Brak departamentu') as departmentName " +
//            "FROM Employee e LEFT JOIN e.department")
//    Page<EmployeeListView> findAllEmployeesSummary(Pageable pageable);
//
//
//    @Query("SELECT AVG(e.salary) FROM Employee e")
//    Double findAverageSalary();
//
//    @Query("SELECT AVG(e.salary) FROM Employee e WHERE e.company = :company")
//    Double findAverageSalaryByCompany(@Param("company") String company);
//
//    @Query("SELECT MAX(e.salary) FROM Employee e")
//    Double findMaxSalary();
//
//    @Query("SELECT MAX(e.salary) FROM Employee e WHERE e.company = :company")
//    Double findMaxSalaryByCompany(@Param("company") String company);
//
//    @Query("SELECT MIN(e.salary) FROM Employee e")
//    Double findMinSalary();
//
//    @Query("SELECT SUM(e.salary) FROM Employee e")
//    Double findTotalSalaryCost();
//
//    @Query("SELECT SUM(e.salary) FROM Employee e WHERE e.company = :company")
//    Double findTotalSalaryCostByCompany(@Param("company") String company);
//
//    @Query("SELECT COUNT(e) FROM Employee e")
//    Long countAllEmployees();
//
//    @Query("SELECT COUNT(e) FROM Employee e WHERE e.company = :company")
//    Long countEmployeesByCompany(@Param("company") String company);
//
//    @Query("SELECT COUNT(e) FROM Employee e WHERE e.status = :status")
//    Long countEmployeesByStatus(@Param("status") EmploymentStatus status);
//
//    @Query("SELECT COUNT(e) FROM Employee e WHERE e.position = :position")
//    Long countEmployeesByPosition(@Param("position") Position position);
//
//    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId")
//    Long countEmployeesByDepartment(@Param("departmentId") Long departmentId);
//
//    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department IS NULL")
//    Long countEmployeesWithoutDepartment();
//
//    // STATYSTYKI GRUPUJĄCE
//    @Query("SELECT e.company, COUNT(e), AVG(e.salary), MAX(e.salary), MIN(e.salary) " +
//            "FROM Employee e GROUP BY e.company")
//    List<Object[]> getCompanyStatistics();
//
//    @Query("SELECT NEW com.techcorp.employee.dto.CompanyStatisticsDTO(" +
//            "e.company, " +
//            "COUNT(e), " +
//            "AVG(e.salary), " +
//            "MAX(e.salary), " +
//            "(SELECT e2.name FROM Employee e2 WHERE e2.company = e.company AND e2.salary = (SELECT MAX(e3.salary) FROM Employee e3 WHERE e3.company = e.company))" +
//            ") " +
//            "FROM Employee e GROUP BY e.company")
//    List<CompanyStatisticsDTO> getCompanyStatisticsDTO();
//
//    @Query("SELECT NEW com.techcorp.employee.dto.CompanyStatisticsDTO(" +
//            ":company, " +
//            "COUNT(e), " +
//            "AVG(e.salary), " +
//            "MAX(e.salary), " +
//            "(SELECT e2.name FROM Employee e2 WHERE e2.company = :company AND e2.salary = (SELECT MAX(e3.salary) FROM Employee e3 WHERE e3.company = :company))" +
//            ") " +
//            "FROM Employee e WHERE e.company = :company GROUP BY e.company")
//    Optional<CompanyStatisticsDTO> getCompanyStatisticsDTO(@Param("company") String company);
//
//
//    @Query("SELECT e.position, COUNT(e), AVG(e.salary), MAX(e.salary), MIN(e.salary) " +
//            "FROM Employee e GROUP BY e.position")
//    List<Object[]> getPositionStatistics();
//
//    @Query("SELECT e.status, COUNT(e), AVG(e.salary) " +
//            "FROM Employee e GROUP BY e.status")
//    List<Object[]> getStatusStatistics();
//
//
//
//    @Query("SELECT e FROM Employee e WHERE e.salary = (SELECT MAX(e2.salary) FROM Employee e2)")
//    List<Employee> findHighestPaidEmployees();
//
//    @Query("SELECT e FROM Employee e WHERE e.salary = (SELECT MAX(e2.salary) FROM Employee e2 WHERE e2.company = :company)")
//    List<Employee> findHighestPaidEmployeesByCompany(@Param("company") String company);
//
//    @Query("SELECT e FROM Employee e WHERE e.salary < (SELECT AVG(e2.salary) FROM Employee e2)")
//    List<Employee> findEmployeesBelowAverageSalary();
//
//    @Query(value = "SELECT e FROM Employee e ORDER BY e.salary DESC")
//    List<Employee> findTop10HighestPaidEmployees(Pageable pageable);
//
//
//    @Query("SELECT e.name as name, e.email as email, e.company as company, " +
//            "e.position as position, e.salary as salary, e.status as status, " +
//            "COALESCE(e.department.name, 'Brak departamentu') as departmentName " +
//            "FROM Employee e LEFT JOIN e.department WHERE e.status = :status")  // <-- LEFT JOIN!
//    Page<EmployeeListView> findByStatusProjection(
//            @Param("status") EmploymentStatus status,
//            Pageable pageable);
//
//    @Query("SELECT e.name as name, e.email as email, e.company as company, " +
//            "e.position as position, e.salary as salary, e.status as status, " +
//            "COALESCE(e.department.name, 'Brak departamentu') as departmentName " +
//            "FROM Employee e LEFT JOIN e.department WHERE e.company = :company")  // <-- LEFT JOIN!
//    Page<EmployeeListView> findByCompanyProjection(
//            @Param("company") String company,
//            Pageable pageable);
//
//
//    // Dla getAllUniqueCompanies()
//    @Query("SELECT DISTINCT e.company FROM Employee e ORDER BY e.company ASC")
//    List<String> findDistinctCompanies();
//
//
//
//    // ✅ DODAJ w EmployeeRepository.java
//    @Query("SELECT e.name as name, e.email as email, e.company as company, " +
//            "e.position as position, e.salary as salary, e.status as status, " +
//            "COALESCE(e.department.name, 'Brak departamentu') as departmentName " +
//            "FROM Employee e LEFT JOIN e.department")
//    Page<EmployeeListView> findAllProjection(Pageable pageable);
//
//
//    @Query("""
//        SELECT
//            e.name as name,
//            e.email as email,
//            e.company as company,
//            e.position as position,
//            e.salary as salary,
//            e.status as status,
//            CASE
//                WHEN e.department IS NULL THEN 'Brak departamentu'
//                ELSE e.department.name
//            END as departmentName
//        FROM Employee e
//        LEFT JOIN e.department d
//        WHERE
//            (:name IS NULL OR :name = '' OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')))
//            AND (:company IS NULL OR :company = '' OR LOWER(e.company) = LOWER(:company))
//            AND (:position IS NULL OR e.position = :position)
//            AND (:status IS NULL OR e.status = :status)
//            AND (:minSalary IS NULL OR e.salary >= :minSalary)
//            AND (:maxSalary IS NULL OR e.salary <= :maxSalary)
//            AND (
//                :departmentName IS NULL
//                OR :departmentName = ''
//                OR (
//                    LOWER(:departmentName) = 'brak departamentu'
//                    AND e.department IS NULL
//                )
//                OR (
//                    LOWER(:departmentName) != 'brak departamentu'
//                    AND LOWER(e.department.name) = LOWER(:departmentName)
//                )
//            )
//        ORDER BY e.name ASC
//    """)
//    Page<EmployeeListView> findWithFiltersProjection(
//            @Param("name") String name,
//            @Param("company") String company,
//            @Param("position") Position position,
//            @Param("status") EmploymentStatus status,
//            @Param("minSalary") Double minSalary,
//            @Param("maxSalary") Double maxSalary,
//            @Param("departmentName") String departmentName,
//            Pageable pageable);
//
//
//}