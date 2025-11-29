package com.techcorp.employee.dao;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.CompanyStatistics;
import com.techcorp.employee.exception.DuplicateEmailException;
import com.techcorp.employee.exception.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcEmployeeDAO implements EmployeeDAO {

    private final JdbcTemplate jdbcTemplate;

    public JdbcEmployeeDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Employee> employeeRowMapper = (rs, rowNum) -> {
        Employee employee = new Employee();
        employee.setId(rs.getLong("id"));

        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String fullName = firstName + (lastName != null && !lastName.isEmpty() ? " " + lastName : "");
        employee.setName(fullName);

        employee.setEmail(rs.getString("email"));
        employee.setSalary(rs.getDouble("salary"));

        String positionStr = rs.getString("position");
        if (positionStr != null) {
            try {
                employee.setPosition(Position.valueOf(positionStr));
            } catch (IllegalArgumentException e) {
                employee.setPosition(Position.PROGRAMMER);
            }
        }

        employee.setCompany(rs.getString("company"));

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try {
                employee.setStatus(EmploymentStatus.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                employee.setStatus(EmploymentStatus.ACTIVE);
            }
        }

        long departmentId = rs.getLong("department_id");
        employee.setDepartmentId(rs.wasNull() ? null : departmentId);

        employee.setPhotoFileName(rs.getString("photo_file_name"));
        return employee;
    };

    @Override
    public List<Employee> findAll() {
        String sql = "SELECT * FROM employees";
        return jdbcTemplate.query(sql, employeeRowMapper);
    }

    @Override
    public Optional<Employee> findById(Long id) {
        String sql = "SELECT * FROM employees WHERE id = ?";
        List<Employee> employees = jdbcTemplate.query(sql, employeeRowMapper, id);
        return employees.isEmpty() ? Optional.empty() : Optional.of(employees.get(0));
    }

    @Override
    public Optional<Employee> findByEmail(String email) {
        String sql = "SELECT * FROM employees WHERE email = ?";
        List<Employee> employees = jdbcTemplate.query(sql, employeeRowMapper, email);
        return employees.isEmpty() ? Optional.empty() : Optional.of(employees.get(0));
    }

    @Override
    public void save(Employee employee) {
        try {
            if (employee.getId() == null) {
                insertEmployee(employee);
            } else {
                updateEmployee(employee);
            }
        } catch (DuplicateKeyException e) {
            String errorMsg = "Email already exists: " + employee.getEmail();
            throw new DuplicateEmailException(errorMsg, e);
        } catch (DataIntegrityViolationException e) {
            throw new DataAccessException("Database constraint violation", e);
        }
    }

    private void insertEmployee(Employee employee) {
        String sql = "INSERT INTO employees (first_name, last_name, email, salary, position, company, status, department_id, photo_file_name) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, employee.getFirstName());
            ps.setString(2, employee.getLastName());
            ps.setString(3, employee.getEmail());
            ps.setDouble(4, employee.getSalary());
            ps.setString(5, employee.getPosition().name());
            ps.setString(6, employee.getCompany());
            ps.setString(7, employee.getStatus().name());
            if (employee.getDepartmentId() != null) {
                ps.setLong(8, employee.getDepartmentId());
            } else {
                ps.setNull(8, java.sql.Types.BIGINT);
            }
            ps.setString(9, employee.getPhotoFileName());
            return ps;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId != null) {
            employee.setId(generatedId.longValue());
        }
    }

    private void updateEmployee(Employee employee) {
        String sql = "UPDATE employees SET first_name = ?, last_name = ?, email = ?, salary = ?, " +
                "position = ?, company = ?, status = ?, department_id = ?, photo_file_name = ? " +
                "WHERE id = ?";

        int affectedRows = jdbcTemplate.update(sql,
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getSalary(),
                employee.getPosition().name(),
                employee.getCompany(),
                employee.getStatus().name(),
                employee.getDepartmentId(),
                employee.getPhotoFileName(),
                employee.getId());

        if (affectedRows == 0) {
            throw new DataAccessException("No employee found with ID: " + employee.getId());
        }
    }

    @Override
    public void deleteByEmail(String email) {
        String sql = "DELETE FROM employees WHERE email = ?";
        jdbcTemplate.update(sql, email);
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM employees";
        jdbcTemplate.update(sql);
    }

    @Override
    public List<Employee> findByCompany(String company) {
        String sql = "SELECT * FROM employees WHERE company = ?";
        return jdbcTemplate.query(sql, employeeRowMapper, company);
    }

    @Override
    public List<Employee> findByDepartmentId(Long departmentId) {
        String sql = "SELECT * FROM employees WHERE department_id = ?";
        return jdbcTemplate.query(sql, employeeRowMapper, departmentId);
    }

    @Override
    public List<Employee> findEmployeesWithoutDepartment() {
        String sql = "SELECT * FROM employees WHERE department_id IS NULL";
        return jdbcTemplate.query(sql, employeeRowMapper);
    }

    @Override
    public List<CompanyStatistics> getCompanyStatistics() {
        String sql = "SELECT company, " +
                "COUNT(*) as employee_count, " +
                "AVG(salary) as average_salary, " +
                "MAX(salary) as max_salary " +
                "FROM employees " +
                "GROUP BY company " +
                "ORDER BY company";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String companyName = rs.getString("company");
            int employeeCount = rs.getInt("employee_count");
            double averageSalary = rs.getDouble("average_salary");
            double maxSalary = rs.getDouble("max_salary");

            String highestPaidEmployeeSql = "SELECT first_name, last_name FROM employees " +
                    "WHERE company = ? AND salary = ? LIMIT 1";
            List<String> highestPaid = jdbcTemplate.query(highestPaidEmployeeSql,
                    (rs2, rowNum2) -> rs2.getString("first_name") + " " + rs2.getString("last_name"),
                    companyName, maxSalary);

            String highestPaidEmployeeName = highestPaid.isEmpty() ? "" : highestPaid.get(0);

            CompanyStatistics stats = new CompanyStatistics(companyName, employeeCount, averageSalary, maxSalary);
            stats.setHighestPaidEmployee(highestPaidEmployeeName);
            return stats;
        });
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM employees WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }
}




//package com.techcorp.employee.dao;
//
//import com.techcorp.employee.model.Employee;
//import com.techcorp.employee.model.Position;
//import com.techcorp.employee.model.EmploymentStatus;
//import com.techcorp.employee.model.CompanyStatistics;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.core.RowMapper;
//import org.springframework.jdbc.support.GeneratedKeyHolder;
//import org.springframework.jdbc.support.KeyHolder;
//import org.springframework.stereotype.Repository;
//
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public class JdbcEmployeeDAO implements EmployeeDAO {
//
//    private final JdbcTemplate jdbcTemplate;
//
//    public JdbcEmployeeDAO(JdbcTemplate jdbcTemplate) {
//        this.jdbcTemplate = jdbcTemplate;
//    }
//
//    private final RowMapper<Employee> employeeRowMapper = (rs, rowNum) -> {
//        Employee employee = new Employee();
//        employee.setId(rs.getLong("id"));
//
//        // Transformacja: first_name + last_name → name
//        String firstName = rs.getString("first_name");
//        String lastName = rs.getString("last_name");
//        String fullName = firstName + (lastName != null && !lastName.isEmpty() ? " " + lastName : "");
//        employee.setName(fullName);
//
//        employee.setEmail(rs.getString("email"));
//        employee.setSalary(rs.getDouble("salary"));
//
//        String positionStr = rs.getString("position");
//        if (positionStr != null) {
//            employee.setPosition(Position.valueOf(positionStr));
//        }
//
//        employee.setCompany(rs.getString("company"));
//
//        String statusStr = rs.getString("status");
//        if (statusStr != null) {
//            employee.setStatus(EmploymentStatus.valueOf(statusStr));
//        }
//
//        long departmentId = rs.getLong("department_id");
//        employee.setDepartmentId(rs.wasNull() ? null : departmentId);
//
//        employee.setPhotoFileName(rs.getString("photo_file_name"));
//        return employee;
//    };
//
//    @Override
//    public List<Employee> findAll() {
//        String sql = "SELECT * FROM employees";
//        return jdbcTemplate.query(sql, employeeRowMapper);
//    }
//
//    @Override
//    public Optional<Employee> findById(Long id) {
//        String sql = "SELECT * FROM employees WHERE id = ?";
//        List<Employee> employees = jdbcTemplate.query(sql, employeeRowMapper, id);
//        return employees.stream().findFirst();
//    }
//
//    @Override
//    public Optional<Employee> findByEmail(String email) {
//        String sql = "SELECT * FROM employees WHERE email = ?";
//        List<Employee> employees = jdbcTemplate.query(sql, employeeRowMapper, email);
//        return employees.stream().findFirst();
//    }
//
//    @Override
//    public Employee save(Employee employee) {
//        if (employee.getId() == null) {
//            // INSERT - transformacja: name → first_name + last_name
//            String sql = "INSERT INTO employees (first_name, last_name, email, salary, position, company, status, department_id, photo_file_name) " +
//                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
//
//            KeyHolder keyHolder = new GeneratedKeyHolder();
//
//            jdbcTemplate.update(connection -> {
//                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
//                ps.setString(1, employee.getFirstName()); // first_name
//                ps.setString(2, employee.getLastName());  // last_name
//                ps.setString(3, employee.getEmail());
//                ps.setDouble(4, employee.getSalary());
//                ps.setString(5, employee.getPosition().name());
//                ps.setString(6, employee.getCompany());
//                ps.setString(7, employee.getStatus().name());
//                if (employee.getDepartmentId() != null) {
//                    ps.setLong(8, employee.getDepartmentId());
//                } else {
//                    ps.setNull(8, java.sql.Types.BIGINT);
//                }
//                ps.setString(9, employee.getPhotoFileName());
//                return ps;
//            }, keyHolder);
//
//            employee.setId(keyHolder.getKey().longValue());
//        } else {
//            // UPDATE - transformacja: name → first_name + last_name
//            String sql = "UPDATE employees SET first_name = ?, last_name = ?, email = ?, salary = ?, " +
//                    "position = ?, company = ?, status = ?, department_id = ?, photo_file_name = ? " +
//                    "WHERE id = ?";
//
//            jdbcTemplate.update(sql,
//                    employee.getFirstName(),
//                    employee.getLastName(),
//                    employee.getEmail(),
//                    employee.getSalary(),
//                    employee.getPosition().name(),
//                    employee.getCompany(),
//                    employee.getStatus().name(),
//                    employee.getDepartmentId(),
//                    employee.getPhotoFileName(),
//                    employee.getId());
//        }
//
//        return employee;
//    }
//
//    @Override
//    public void deleteByEmail(String email) {
//        String sql = "DELETE FROM employees WHERE email = ?";
//        jdbcTemplate.update(sql, email);
//    }
//
//    @Override
//    public void deleteAll() {
//        String sql = "DELETE FROM employees";
//        jdbcTemplate.update(sql);
//    }
//
//    @Override
//    public List<Employee> findByCompany(String company) {
//        String sql = "SELECT * FROM employees WHERE company = ?";
//        return jdbcTemplate.query(sql, employeeRowMapper, company);
//    }
//
//    @Override
//    public List<Employee> findByDepartmentId(Long departmentId) {
//        String sql = "SELECT * FROM employees WHERE department_id = ?";
//        return jdbcTemplate.query(sql, employeeRowMapper, departmentId);
//    }
//
//    @Override
//    public List<Employee> findEmployeesWithoutDepartment() {
//        String sql = "SELECT * FROM employees WHERE department_id IS NULL";
//        return jdbcTemplate.query(sql, employeeRowMapper);
//    }
//
//    @Override
//    public List<CompanyStatistics> getCompanyStatistics() {
//        String sql = "SELECT company, " +
//                "COUNT(*) as employee_count, " +
//                "AVG(salary) as average_salary, " +
//                "MAX(salary) as max_salary " +
//                "FROM employees " +
//                "GROUP BY company " +
//                "ORDER BY company";
//
//        return jdbcTemplate.query(sql, (rs, rowNum) -> {
//            String companyName = rs.getString("company");
//            int employeeCount = rs.getInt("employee_count");
//            double averageSalary = rs.getDouble("average_salary");
//            double maxSalary = rs.getDouble("max_salary");
//
//            // Znajdź pracownika z najwyższą pensją
//            String highestPaidEmployeeSql = "SELECT first_name, last_name FROM employees " +
//                    "WHERE company = ? AND salary = ? LIMIT 1";
//            List<String> highestPaid = jdbcTemplate.query(highestPaidEmployeeSql,
//                    (rs2, rowNum2) -> rs2.getString("first_name") + " " + rs2.getString("last_name"),
//                    companyName, maxSalary);
//
//            String highestPaidEmployeeName = highestPaid.isEmpty() ? "None" : highestPaid.get(0);
//
//            // UŻYJ NOWEGO KONSTRUKTORA z companyName
//            CompanyStatistics stats = new CompanyStatistics(companyName, employeeCount, averageSalary, maxSalary);
//            stats.setHighestPaidEmployee(highestPaidEmployeeName);
//            return stats;
//        });
//    }
//
//    @Override
//    public boolean existsByEmail(String email) {
//        String sql = "SELECT COUNT(*) FROM employees WHERE email = ?";
//        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
//        return count != null && count > 0;
//    }
//}





//package com.techcorp.employee.dao;
//
//import com.techcorp.employee.model.Employee;
//import com.techcorp.employee.model.Position;
//import com.techcorp.employee.model.EmploymentStatus;
//import com.techcorp.employee.model.CompanyStatistics;
//import com.techcorp.employee.exception.DuplicateEmailException;
//import com.techcorp.employee.exception.DataAccessException;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.core.RowMapper;
//import org.springframework.jdbc.support.GeneratedKeyHolder;
//import org.springframework.jdbc.support.KeyHolder;
//import org.springframework.stereotype.Repository;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.dao.DuplicateKeyException;
//import org.springframework.dao.EmptyResultDataAccessException;
//
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public class JdbcEmployeeDAO implements EmployeeDAO {
//
//    private final JdbcTemplate jdbcTemplate;
//
//    public JdbcEmployeeDAO(JdbcTemplate jdbcTemplate) {
//        this.jdbcTemplate = jdbcTemplate;
//    }
//
//    private final RowMapper<Employee> employeeRowMapper = (rs, rowNum) -> {
//        Employee employee = new Employee();
//        employee.setId(rs.getLong("id"));
//
//        // Transformacja: first_name + last_name → name
//        String firstName = rs.getString("first_name");
//        String lastName = rs.getString("last_name");
//        String fullName = firstName + (lastName != null && !lastName.isEmpty() ? " " + lastName : "");
//        employee.setName(fullName);
//
//        employee.setEmail(rs.getString("email"));
//        employee.setSalary(rs.getDouble("salary"));
//
//        String positionStr = rs.getString("position");
//        if (positionStr != null) {
//            try {
//                employee.setPosition(Position.valueOf(positionStr));
//            } catch (IllegalArgumentException e) {
//                employee.setPosition(Position.PROGRAMMER);
//            }
//        }
//
//        employee.setCompany(rs.getString("company"));
//
//        String statusStr = rs.getString("status");
//        if (statusStr != null) {
//            try {
//                employee.setStatus(EmploymentStatus.valueOf(statusStr));
//            } catch (IllegalArgumentException e) {
//                employee.setStatus(EmploymentStatus.ACTIVE);
//            }
//        }
//
//        long departmentId = rs.getLong("department_id");
//        employee.setDepartmentId(rs.wasNull() ? null : departmentId);
//
//        employee.setPhotoFileName(rs.getString("photo_file_name"));
//        return employee;
//    };
//
////
////    private final RowMapper<Employee> employeeRowMapper = new RowMapper<Employee>() {
////        @Override
////        public Employee mapRow(ResultSet rs, int rowNum) throws SQLException {
////            Employee employee = new Employee();
////            employee.setId(rs.getLong("id"));
////
////            // Transformacja: first_name + last_name → name
////            String firstName = rs.getString("first_name");
////            String lastName = rs.getString("last_name");
////            String fullName = firstName + (lastName != null && !lastName.isEmpty() ? " " + lastName : "");
////            employee.setName(fullName);
////
////            employee.setEmail(rs.getString("email"));
////            employee.setSalary(rs.getDouble("salary"));
////
////            String positionStr = rs.getString("position");
////            if (positionStr != null) {
////                try {
////                    employee.setPosition(Position.valueOf(positionStr));
////                } catch (IllegalArgumentException e) {
////                    employee.setPosition(Position.PROGRAMMER);
////                }
////            }
////
////            employee.setCompany(rs.getString("company"));
////
////            String statusStr = rs.getString("status");
////            if (statusStr != null) {
////                try {
////                    employee.setStatus(EmploymentStatus.valueOf(statusStr));
////                } catch (IllegalArgumentException e) {
////                    employee.setStatus(EmploymentStatus.ACTIVE);
////                }
////            }
////
////            return employee;
////        }
////    };
//
//
//    @Override
//    public List<Employee> findAll() {
//        try {
//            String sql = "SELECT * FROM employees";
//            return jdbcTemplate.query(sql, employeeRowMapper);
//        } catch (Exception e) {
//            throw new DataAccessException("Failed to retrieve employees from database", e);
//        }
//    }
//
//    @Override
//    public Optional<Employee> findById(Long id) {
//        try {
//            String sql = "SELECT * FROM employees WHERE id = ?";
//            List<Employee> employees = jdbcTemplate.query(sql, employeeRowMapper, id);
//            return employees.stream().findFirst();
//        } catch (Exception e) {
//            throw new DataAccessException("Failed to find employee by ID: " + id, e);
//        }
//    }
//
//    @Override
//    public Optional<Employee> findByEmail(String email) {
//        try {
//            String sql = "SELECT * FROM employees WHERE email = ?";
//            List<Employee> employees = jdbcTemplate.query(sql, employeeRowMapper, email);
//            return employees.stream().findFirst();
//        } catch (Exception e) {
//            throw new DataAccessException("Failed to find employee by email: " + email, e);
//        }
//    }
//
//    @Override
//    public Employee save(Employee employee) {
//        try {
//            if (employee.getId() == null) {
//                return insertEmployee(employee);
//            } else {
//                return updateEmployee(employee);
//            }
//        } catch (DuplicateKeyException e) {
//            String errorMsg = "Email already exists: " + employee.getEmail();
//            throw new DuplicateEmailException(errorMsg, e);
//        } catch (DataIntegrityViolationException e) {
//            throw new DataAccessException("Database constraint violation", e);
//        } catch (Exception e) {
//            String errorMsg = "Unexpected error while saving employee: " + employee.getEmail();
//            throw new DataAccessException(errorMsg, e);
//        }
//    }
//
//    private Employee insertEmployee(Employee employee) {
//
//        String sql = "INSERT INTO employees (first_name, last_name, email, salary, position, company, status, department_id, photo_file_name) " +
//                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
//
//        KeyHolder keyHolder = new GeneratedKeyHolder();
//
//        jdbcTemplate.update(connection -> {
//            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
//            ps.setString(1, employee.getFirstName());
//            ps.setString(2, employee.getLastName());
//            ps.setString(3, employee.getEmail());
//            ps.setDouble(4, employee.getSalary());
//            ps.setString(5, employee.getPosition().name());
//            ps.setString(6, employee.getCompany());
//            ps.setString(7, employee.getStatus().name());
//            if (employee.getDepartmentId() != null) {
//                ps.setLong(8, employee.getDepartmentId());
//            } else {
//                ps.setNull(8, java.sql.Types.BIGINT);
//            }
//            ps.setString(9, employee.getPhotoFileName());
//            return ps;
//        }, keyHolder);
//
//        Number generatedId = keyHolder.getKey();
//        if (generatedId != null) {
//            employee.setId(generatedId.longValue());
//        }
//
//        return employee;
//    }
//
//    private Employee updateEmployee(Employee employee) {
//
//        String sql = "UPDATE employees SET first_name = ?, last_name = ?, email = ?, salary = ?, " +
//                "position = ?, company = ?, status = ?, department_id = ?, photo_file_name = ? " +
//                "WHERE id = ?";
//
//        int affectedRows = jdbcTemplate.update(sql,
//                employee.getFirstName(),
//                employee.getLastName(),
//                employee.getEmail(),
//                employee.getSalary(),
//                employee.getPosition().name(),
//                employee.getCompany(),
//                employee.getStatus().name(),
//                employee.getDepartmentId(),
//                employee.getPhotoFileName(),
//                employee.getId());
//
//        if (affectedRows == 0) {
//            throw new DataAccessException("No employee found with ID: " + employee.getId());
//        }
//
//        return employee;
//    }
//
//    @Override
//    public void deleteByEmail(String email) {
//        try {
//            String sql = "DELETE FROM employees WHERE email = ?";
//            jdbcTemplate.update(sql, email);
//
//        } catch (Exception e) {
//            throw new DataAccessException("Failed to delete employee with email: " + email, e);
//        }
//    }
//
//
//    @Override
//    public void deleteAll() {
//        try {
//            String sql = "DELETE FROM employees";
//            jdbcTemplate.update(sql);
//        } catch (Exception e) {
//            throw new DataAccessException("Failed to delete all employees", e);
//        }
//    }
//
//    @Override
//    public List<Employee> findByCompany(String company) {
//        try {
//            String sql = "SELECT * FROM employees WHERE company = ?";
//            return jdbcTemplate.query(sql, employeeRowMapper, company);
//        } catch (Exception e) {
//            throw new DataAccessException("Failed to find employees for company: " + company, e);
//        }
//    }
//
//    @Override
//    public List<Employee> findByDepartmentId(Long departmentId) {
//        try {
//            String sql = "SELECT * FROM employees WHERE department_id = ?";
//            return jdbcTemplate.query(sql, employeeRowMapper, departmentId);
//        } catch (Exception e) {
//            throw new DataAccessException("Failed to find employees for department ID: " + departmentId, e);
//        }
//    }
//
//    @Override
//    public List<Employee> findEmployeesWithoutDepartment() {
//        try {
//            String sql = "SELECT * FROM employees WHERE department_id IS NULL";
//            return jdbcTemplate.query(sql, employeeRowMapper);
//        } catch (Exception e) {
//            throw new DataAccessException("Failed to find employees without department", e);
//        }
//    }
//
//    @Override
//    public List<CompanyStatistics> getCompanyStatistics() {
//        try {
//            String sql = "SELECT company, " +
//                    "COUNT(*) as employee_count, " +
//                    "AVG(salary) as average_salary, " +
//                    "MAX(salary) as max_salary " +
//                    "FROM employees " +
//                    "GROUP BY company " +
//                    "ORDER BY company";
//
//            return jdbcTemplate.query(sql, (rs, rowNum) -> {
//                String companyName = rs.getString("company");
//                int employeeCount = rs.getInt("employee_count");
//                double averageSalary = rs.getDouble("average_salary");
//                double maxSalary = rs.getDouble("max_salary");
//
//                try {
//                    String highestPaidEmployeeSql = "SELECT first_name, last_name FROM employees " +
//                            "WHERE company = ? AND salary = ? LIMIT 1";
//                    List<String> highestPaid = jdbcTemplate.query(highestPaidEmployeeSql,
//                            (rs2, rowNum2) -> rs2.getString("first_name") + " " + rs2.getString("last_name"),
//                            companyName, maxSalary);
//
//                    String highestPaidEmployeeName = highestPaid.isEmpty() ? "" : highestPaid.get(0);
//
//                    CompanyStatistics stats = new CompanyStatistics(companyName, employeeCount, averageSalary, maxSalary);
//                    stats.setHighestPaidEmployee(highestPaidEmployeeName);
//                    return stats;
//                } catch (Exception e) {
//                    CompanyStatistics stats = new CompanyStatistics(companyName, employeeCount, averageSalary, maxSalary);
//                    stats.setHighestPaidEmployee("Unknown");
//                    return stats;
//                }
//            });
//        } catch (Exception e) {
//            throw new DataAccessException("Failed to generate company statistics", e);
//        }
//    }
//
//    @Override
//    public boolean existsByEmail(String email) {
//        try {
//            String sql = "SELECT COUNT(*) FROM employees WHERE email = ?";
//            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
//            return count != null && count > 0;
//        } catch (EmptyResultDataAccessException e) {
//            return false;
//        } catch (Exception e) {
//            throw new DataAccessException("Failed to check if employee exists by email: " + email, e);
//        }
//    }
//
//}
//
