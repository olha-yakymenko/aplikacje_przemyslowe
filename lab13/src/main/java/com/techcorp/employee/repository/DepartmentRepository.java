//package com.techcorp.employee.repository;
//
//import com.techcorp.employee.model.Department;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface DepartmentRepository extends JpaRepository<Department, Long> {
//
//    Optional<Department> findByName(String name);
//    boolean existsByName(String name);
//
//    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.employees WHERE d.id = :id")
//    Optional<Department> findByIdWithEmployees(Long id);
//
//    List<Department> findByLocation(String location);
//
//    @Query("SELECT DISTINCT d.name FROM Department d ORDER BY d.name")
//    List<String> findAllDepartmentNames();
//}




package com.techcorp.employee.repository;

import com.techcorp.employee.model.Department;
import com.techcorp.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByName(String name);
    boolean existsByName(String name);

    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.employees WHERE d.id = :id")
    Optional<Department> findByIdWithEmployees(Long id);

    List<Department> findByLocation(String location);

    @Query("SELECT DISTINCT d.name FROM Department d ORDER BY d.name")
    List<String> findAllDepartmentNames();

    // Nowe metody:

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department WHERE e.department.id = :departmentId")
    List<Employee> findEmployeesByDepartmentId(@Param("departmentId") Long departmentId);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department WHERE e.email = :email")
    Optional<Employee> findEmployeeWithDepartmentByEmail(@Param("email") String email);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department")
    List<Employee> findAllEmployeesWithDepartment();

    @Query("SELECT d.name FROM Department d WHERE d.id = :departmentId")
    Optional<String> findDepartmentNameById(@Param("departmentId") Long departmentId);

    @Query("SELECT e.email, d.name FROM Employee e LEFT JOIN e.department d")
    List<Object[]> findAllEmployeeEmailsWithDepartmentNames();

    @Query("SELECT e.id, e.name, e.email, e.position, e.salary, d.id as departmentId, d.name as departmentName " +
            "FROM Employee e LEFT JOIN e.department d WHERE e.department.id = :departmentId")
    List<Object[]> findEmployeesWithDepartmentDetailsByDepartmentId(@Param("departmentId") Long departmentId);

    @Query("SELECT e.id, e.name, e.email, e.position, e.salary, d.id as departmentId, d.name as departmentName " +
            "FROM Employee e LEFT JOIN e.department d")
    List<Object[]> findAllEmployeesWithDepartmentDetails();
}