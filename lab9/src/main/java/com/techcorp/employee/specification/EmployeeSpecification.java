////package com.techcorp.employee.specification;
////
////import com.techcorp.employee.model.Employee;
////import com.techcorp.employee.model.Position;
////import com.techcorp.employee.model.EmploymentStatus;
////import org.springframework.data.jpa.domain.Specification;
////import jakarta.persistence.criteria.Predicate;
////import java.util.ArrayList;
////import java.util.List;
////
////public class EmployeeSpecification {
////
////    public static Specification<Employee> withDynamicQuery(
////            String name,
////            String company,
////            Position position,
////            EmploymentStatus status,
////            Double minSalary,
////            Double maxSalary,
////            String departmentName) {
////
////        return (root, query, criteriaBuilder) -> {
////            List<Predicate> predicates = new ArrayList<>();
////
////            if (name != null && !name.trim().isEmpty()) {
////                predicates.add(criteriaBuilder.like(
////                        criteriaBuilder.lower(root.get("name")),
////                        "%" + name.toLowerCase() + "%"
////                ));
////            }
////
////            if (company != null && !company.trim().isEmpty()) {
////                predicates.add(criteriaBuilder.equal(
////                        criteriaBuilder.lower(root.get("company")),
////                        company.toLowerCase()
////                ));
////            }
////
////            if (position != null) {
////                predicates.add(criteriaBuilder.equal(root.get("position"), position));
////            }
////
////            if (status != null) {
////                predicates.add(criteriaBuilder.equal(root.get("status"), status));
////            }
////
////            if (minSalary != null) {
////                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("salary"), minSalary));
////            }
////
////            if (maxSalary != null) {
////                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("salary"), maxSalary));
////            }
////
////            if (departmentName != null && !departmentName.trim().isEmpty()) {
////                predicates.add(criteriaBuilder.equal(
////                        criteriaBuilder.lower(root.get("department").get("name")),
////                        departmentName.toLowerCase()
////                ));
////            }
////
////            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
////        };
////    }
////}
//
//
//
//
//
//package com.techcorp.employee.specification;
//
//import com.techcorp.employee.model.Employee;
//import com.techcorp.employee.model.Position;
//import com.techcorp.employee.model.EmploymentStatus;
//import org.springframework.data.jpa.domain.Specification;
//import jakarta.persistence.criteria.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class EmployeeSpecification {
//
//    // ✅ DODANE: Pełna implementacja z wszystkimi możliwymi kryteriami
//    public static Specification<Employee> withDynamicQuery(
//            String name,
//            String company,
//            Position position,
//            EmploymentStatus status,
//            Double minSalary,
//            Double maxSalary,
//            String departmentName,
//            String email,
//            String phone,
//            Boolean hasPhoto) {
//
//        return (root, query, criteriaBuilder) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            // ✅ Wyszukiwanie po imieniu/nazwisku (częściowa zgodność)
//            if (name != null && !name.trim().isEmpty()) {
//                String searchPattern = "%" + name.toLowerCase() + "%";
//                predicates.add(criteriaBuilder.like(
//                        criteriaBuilder.lower(root.get("name")),
//                        searchPattern
//                ));
//            }
//
//            // ✅ Firma (dokładna zgodność, case-insensitive)
//            if (company != null && !company.trim().isEmpty()) {
//                predicates.add(criteriaBuilder.equal(
//                        criteriaBuilder.lower(root.get("company")),
//                        company.toLowerCase()
//                ));
//            }
//
//            // ✅ Pozycja (enum)
//            if (position != null) {
//                predicates.add(criteriaBuilder.equal(root.get("position"), position));
//            }
//
//            // ✅ Status zatrudnienia (enum)
//            if (status != null) {
//                predicates.add(criteriaBuilder.equal(root.get("status"), status));
//            }
//
//            // ✅ Zakres wynagrodzenia
//            if (minSalary != null) {
//                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("salary"), minSalary));
//            }
//
//            if (maxSalary != null) {
//                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("salary"), maxSalary));
//            }
//
//            // ✅ Dział (join z Department)
//            if (departmentName != null && !departmentName.trim().isEmpty()) {
//                Join<Object, Object> departmentJoin = root.join("department", JoinType.LEFT);
//                predicates.add(criteriaBuilder.equal(
//                        criteriaBuilder.lower(departmentJoin.get("name")),
//                        departmentName.toLowerCase()
//                ));
//            }
//
//            // ✅ Email (częściowa zgodność)
//            if (email != null && !email.trim().isEmpty()) {
//                String emailPattern = "%" + email.toLowerCase() + "%";
//                predicates.add(criteriaBuilder.like(
//                        criteriaBuilder.lower(root.get("email")),
//                        emailPattern
//                ));
//            }
//
//            // ✅ Telefon
//            if (phone != null && !phone.trim().isEmpty()) {
//                String phonePattern = "%" + phone + "%";
//                predicates.add(criteriaBuilder.like(root.get("phone"), phonePattern));
//            }
//
//            // ✅ Czy ma zdjęcie
//            if (hasPhoto != null) {
//                if (hasPhoto) {
//                    predicates.add(criteriaBuilder.isNotNull(root.get("photoFileName")));
//                } else {
//                    predicates.add(criteriaBuilder.isNull(root.get("photoFileName")));
//                }
//            }
//
//            // ✅ Sortowanie po nazwisku (domyślnie)
//            query.orderBy(criteriaBuilder.asc(root.get("name")));
//
//            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
//        };
//    }
//
//    // ✅ DODANE: Specyficzne Specifications dla często używanych kryteriów
//    public static Specification<Employee> hasPosition(Position position) {
//        return (root, query, criteriaBuilder) ->
//                position == null ? null : criteriaBuilder.equal(root.get("position"), position);
//    }
//
//    public static Specification<Employee> hasStatus(EmploymentStatus status) {
//        return (root, query, criteriaBuilder) ->
//                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
//    }
//
//    public static Specification<Employee> companyEquals(String company) {
//        return (root, query, criteriaBuilder) -> {
//            if (company == null || company.trim().isEmpty()) {
//                return null;
//            }
//            return criteriaBuilder.equal(
//                    criteriaBuilder.lower(root.get("company")),
//                    company.toLowerCase()
//            );
//        };
//    }
//
//    public static Specification<Employee> salaryBetween(Double min, Double max) {
//        return (root, query, criteriaBuilder) -> {
//            List<Predicate> predicates = new ArrayList<>();
//            if (min != null) {
//                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("salary"), min));
//            }
//            if (max != null) {
//                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("salary"), max));
//            }
//            return predicates.isEmpty() ? null : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
//        };
//    }
//
//    public static Specification<Employee> nameContains(String name) {
//        return (root, query, criteriaBuilder) -> {
//            if (name == null || name.trim().isEmpty()) {
//                return null;
//            }
//            return criteriaBuilder.like(
//                    criteriaBuilder.lower(root.get("name")),
//                    "%" + name.toLowerCase() + "%"
//            );
//        };
//    }
//}





package com.techcorp.employee.specification;

import com.techcorp.employee.model.Employee;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.model.EmploymentStatus;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeSpecification {

//    public static Specification<Employee> withDynamicQuery(
//            String name,
//            String company,
//            Position position,
//            EmploymentStatus status,
//            Double minSalary,
//            Double maxSalary,
//            String departmentName) {
//
//        return (root, query, criteriaBuilder) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            // ✅ Name - częściowa zgodność (case-insensitive)
//            if (name != null && !name.trim().isEmpty()) {
//                predicates.add(criteriaBuilder.like(
//                        criteriaBuilder.lower(root.get("name")),
//                        "%" + name.toLowerCase() + "%"
//                ));
//            }
//
//            // ✅ Company - dokładna zgodność (case-insensitive)
//            if (company != null && !company.trim().isEmpty()) {
//                predicates.add(criteriaBuilder.equal(
//                        criteriaBuilder.lower(root.get("company")),
//                        company.toLowerCase()
//                ));
//            }
//
//            // ✅ Position - enum
//            if (position != null) {
//                predicates.add(criteriaBuilder.equal(root.get("position"), position));
//            }
//
//            // ✅ Status - enum
//            if (status != null) {
//                predicates.add(criteriaBuilder.equal(root.get("status"), status));
//            }
//
//            // ✅ Salary range
//            if (minSalary != null) {
//                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("salary"), minSalary));
//            }
//
//            if (maxSalary != null) {
//                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("salary"), maxSalary));
//            }
//
//            if (departmentName != null && !departmentName.trim().isEmpty()) {
//                if (departmentName.equalsIgnoreCase("Brak departamentu")) {
//                    predicates.add(criteriaBuilder.isNull(root.get("department")));
//                } else {
//                    // LEFT JOIN żeby nie tracić pracowników bez departamentu
//                    Join<Object, Object> departmentJoin = root.join("department", JoinType.LEFT);
//                    predicates.add(criteriaBuilder.equal(
//                            criteriaBuilder.lower(departmentJoin.get("name")),
//                            departmentName.toLowerCase()
//                    ));
//                }
//            }
//
//            return predicates.isEmpty() ? null : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
//        };
//    }





    public static Specification<Employee> withDynamicQuery(
            String name,
            String company,
            Position position,
            EmploymentStatus status,
            Double minSalary,
            Double maxSalary,
            String departmentName) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Name - częściowa zgodność
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"
                ));
            }

            // Company - dokładna zgodność
            if (company != null && !company.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("company")),
                        company.toLowerCase()
                ));
            }

            // Position
            if (position != null) {
                predicates.add(criteriaBuilder.equal(root.get("position"), position));
            }

            // Status
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Salary range
            if (minSalary != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("salary"), minSalary));
            }

            if (maxSalary != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("salary"), maxSalary));
            }

            // Department
            if (departmentName != null && !departmentName.trim().isEmpty()) {
                if (departmentName.equalsIgnoreCase("Brak departamentu")) {
                    predicates.add(criteriaBuilder.isNull(root.get("department")));
                } else {
                    Join<Object, Object> departmentJoin = root.join("department", JoinType.LEFT);
                    predicates.add(criteriaBuilder.equal(
                            criteriaBuilder.lower(departmentJoin.get("name")),
                            departmentName.toLowerCase()
                    ));
                }
            }

            // ✅ NIGDY nie zwracaj null - używaj conjunction() lub and()
            return predicates.isEmpty()
                    ? criteriaBuilder.conjunction()  // <-- ZMIANA TUTAJ
                    : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}