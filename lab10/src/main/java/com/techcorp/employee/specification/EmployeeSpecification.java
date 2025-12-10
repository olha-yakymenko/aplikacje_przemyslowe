
package com.techcorp.employee.specification;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeSpecification {

    public static Specification<Employee> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Employee> fromCompany(String company) {
        return (root, query, criteriaBuilder) -> {
            if (company == null || company.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("company")),
                    company.toLowerCase()
            );
        };
    }

    public static Specification<Employee> withPosition(Position position) {
        return (root, query, criteriaBuilder) -> {
            if (position == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("position"), position);
        };
    }


    public static Specification<Employee> withStatus(EmploymentStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Employee> salaryGreaterThanOrEqual(Double minSalary) {
        return (root, query, criteriaBuilder) -> {
            if (minSalary == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("salary"), minSalary);
        };
    }

    public static Specification<Employee> salaryLessThanOrEqual(Double maxSalary) {
        return (root, query, criteriaBuilder) -> {
            if (maxSalary == null) {
                return null;
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("salary"), maxSalary);
        };
    }

    public static Specification<Employee> salaryBetween(Double minSalary, Double maxSalary) {
        return (root, query, criteriaBuilder) -> {
            if (minSalary == null && maxSalary == null) {
                return null;
            }

            List<Predicate> predicates = new ArrayList<>();
            if (minSalary != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("salary"), minSalary));
            }
            if (maxSalary != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("salary"), maxSalary));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Employee> inDepartment(String departmentName) {
        return (root, query, criteriaBuilder) -> {
            if (departmentName == null || departmentName.trim().isEmpty()) {
                return null;
            }

            if ("brak departamentu".equalsIgnoreCase(departmentName)) {
                return criteriaBuilder.isNull(root.get("department"));
            } else {
                Join<Employee, ?> departmentJoin = root.join("department", JoinType.LEFT);
                return criteriaBuilder.equal(
                        criteriaBuilder.lower(departmentJoin.get("name")),
                        departmentName.toLowerCase()
                );
            }
        };
    }

    public static Specification<Employee> hasEmail(String email) {
        return (root, query, criteriaBuilder) -> {
            if (email == null || email.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")),
                    "%" + email.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Employee> hasDepartment() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isNotNull(root.get("department"));
    }

    public static Specification<Employee> hasNoDepartment() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isNull(root.get("department"));
    }

    public static Specification<Employee> hasPhoto() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isNotNull(root.get("photoFileName"));
    }


    public static Specification<Employee> hasNoPhoto() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isNull(root.get("photoFileName"));
    }


    public static Specification<Employee> highEarnersInIT() {
        return Specification
                .where(fromCompany("IT"))
                .and(salaryGreaterThanOrEqual(5000.0));
    }


    public static Specification<Employee> activeManagers() {
        return Specification
                .where(withPosition(Position.MANAGER))
                .and(withStatus(EmploymentStatus.ACTIVE));
    }

    public static Specification<Employee> lowPaidWithoutDepartment() {
        return Specification
                .where(hasNoDepartment())
                .and(salaryLessThanOrEqual(3000.0));
    }
}