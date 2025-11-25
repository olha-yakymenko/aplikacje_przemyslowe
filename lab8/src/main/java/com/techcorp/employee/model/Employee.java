//package com.techcorp.employee.model;
//
//import com.techcorp.employee.exception.InvalidDataException;
//import jakarta.validation.constraints.*;
//
//import java.util.Objects;
//
////public class Employee {
////    private String name;
////    private final String email;
////    private String company;
////    private Position position;
////    private double salary;
////    private EmploymentStatus status;
////    private String photoFileName;
//
//public class Employee {
//    @NotBlank(message = "Imię i nazwisko są wymagane")
//    @Size(min = 2, max = 100, message = "Imię i nazwisko musi mieć od 2 do 100 znaków")
//    private String name;
//
//    @NotBlank(message = "Email jest wymagany")
//    @Email(message = "Nieprawidłowy format email")
//    private final String email;
//
//    @NotBlank(message = "Firma jest wymagana")
//    private String company;
//
//    @NotNull(message = "Stanowisko jest wymagane")
//    private Position position;
//
//    @Min(value = 0, message = "Wynagrodzenie musi być liczbą dodatnią")
//    private double salary;
//
//    @NotNull(message = "Status jest wymagany")
//    private EmploymentStatus status;
//
//    private Long departmentId;
//    private String photoFileName;
//
//
//    // Konstruktory
//    public Employee(String name, String email, String company, Position position, double salary) throws InvalidDataException {
//        this(name, email, company, position, salary, EmploymentStatus.ACTIVE); // Domyślny status
//    }
//
//    public Employee(String name, String email, String company, Position position) throws InvalidDataException {
//        this(name, email, company, position, position.getBaseSalary(), EmploymentStatus.ACTIVE);
//    }
//
//    public Employee(String name, String email, String company, Position position, double salary, EmploymentStatus status) throws InvalidDataException {
//        validateInputs(name, email, company, position, salary, status);
//        this.name = name.trim();
//        this.email = email.trim().toLowerCase();
//        this.company = company.trim();
//        this.position = position;
//        this.salary = salary;
//        this.status = status;
//    }
//
//
//    public Employee(String email) {
//        this.email = email;
//    }
//
//    // Gettery i settery
//    public EmploymentStatus getStatus() { return status; }
//
//    public void setStatus(EmploymentStatus status) {
//        if (status == null) {
//            throw new IllegalArgumentException("Status cannot be null");
//        }
//        this.status = status;
//    }
//
//    // Aktualizowana walidacja
//    private void validateInputs(String name, String email, String company, Position position, double salary, EmploymentStatus status) throws InvalidDataException {
//        if (name == null || name.trim().isEmpty()) {
//            throw new InvalidDataException("Name cannot be null or empty");
//        }
//        if (email == null || email.trim().isEmpty()) {
//            throw new InvalidDataException("Email cannot be null or empty");
//        }
//        if (company == null || company.trim().isEmpty()) {
//            throw new InvalidDataException("Company cannot be null or empty");
//        }
//        if (position == null) {
//            throw new InvalidDataException("Position cannot be null");
//        }
//        if (salary < 0) {
//            throw new InvalidDataException("Salary cannot be negative");
//        }
//        if (status == null) {
//            throw new InvalidDataException("Employment status cannot be null");
//        }
//    }
//
//    // Pozostałe metody bez zmian...
//    public String getName() { return name; }
//    public String getEmail() { return email; }
//    public String getCompany() { return company; }
//    public Position getPosition() { return position; }
//    public double getSalary() { return salary; }
//
//    public String getFirstName() {
//        String[] names = name.split(" ");
//        return names.length > 0 ? names[0] : name;
//    }
//
//    public String getLastName() {
//        String[] names = name.split(" ");
//        return names.length > 1 ? names[names.length - 1] : name;
//    }
//
//    public void setName(String name) {
//        if (name == null || name.trim().isEmpty()) {
//            throw new IllegalArgumentException("Name cannot be null or empty");
//        }
//        this.name = name.trim();
//    }
//
//    public void setCompany(String company) {
//        if (company == null || company.trim().isEmpty()) {
//            throw new IllegalArgumentException("Company cannot be null or empty");
//        }
//        this.company = company.trim();
//    }
//
//    public void setPosition(Position position) {
//        if (position == null) {
//            throw new IllegalArgumentException("Position cannot be null");
//        }
//        this.position = position;
//        this.salary = Math.max(this.salary, position.getBaseSalary());
//    }
//
//    public void setSalary(double salary) {
//        if (salary < 0) {
//            throw new IllegalArgumentException("Salary cannot be negative");
//        }
//        this.salary = Math.max(salary, position.getBaseSalary());
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Employee employee = (Employee) o;
//        return Objects.equals(email, employee.email);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(email);
//    }
//
//    @Override
//    public String toString() {
//        return String.format(java.util.Locale.US,
//                "Employee{name='%s', email='%s', company='%s', position=%s, salary=%.2f, status=%s}",
//                name, email, company, position, salary, status);
//    }
//
//    public String getPhotoFileName() { return photoFileName; }
//    public void setPhotoFileName(String photoFileName) { this.photoFileName = photoFileName; }
//
//    public Long getDepartmentId() {
//        return departmentId;
//    }
//
//    public void setDepartmentId(Long departmentId) {
//        this.departmentId = departmentId;
//    }
//}














package com.techcorp.employee.model;

import com.techcorp.employee.exception.InvalidDataException;
import jakarta.validation.constraints.*;

import java.util.Objects;

public class Employee {
    private Long id;

    @NotBlank(message = "Imię i nazwisko są wymagane")
    @Size(min = 2, max = 100, message = "Imię i nazwisko musi mieć od 2 do 100 znaków")
    private String name;

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Nieprawidłowy format email")
    private String email;

    @NotBlank(message = "Firma jest wymagana")
    private String company;

    @NotNull(message = "Stanowisko jest wymagane")
    private Position position;

    @Min(value = 0, message = "Wynagrodzenie musi być liczbą dodatnią")
    private double salary;

    @NotNull(message = "Status jest wymagany")
    private EmploymentStatus status;

    private Long departmentId;
    private String photoFileName;

    // Konstruktory
    public Employee() {
        // Wymagany przez JDBC/Spring
    }

    public Employee(String name, String email, String company, Position position, double salary, EmploymentStatus status) throws InvalidDataException {
        validateInputs(name, email, company, position, salary, status);
        this.name = name.trim();
        this.email = email.trim().toLowerCase();
        this.company = company.trim();
        this.position = position;
        this.salary = salary;
        this.status = status;
    }

    public Employee(String name, String email, String company, Position position, double salary) throws InvalidDataException {
        this(name, email, company, position, salary, EmploymentStatus.ACTIVE);
    }

    public Employee(String name, String email, String company, Position position) throws InvalidDataException {
        this(name, email, company, position, position.getBaseSalary(), EmploymentStatus.ACTIVE);
    }

    public Employee(String email) {
        this.email = email;
    }

    // Gettery i settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name.trim();
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        this.email = email.trim().toLowerCase();
    }

    public String getCompany() { return company; }
    public void setCompany(String company) {
        if (company == null || company.trim().isEmpty()) {
            throw new IllegalArgumentException("Company cannot be null or empty");
        }
        this.company = company.trim();
    }

    public Position getPosition() { return position; }
    public void setPosition(Position position) {
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null");
        }
        this.position = position;
    }

    public double getSalary() { return salary; }
    public void setSalary(double salary) {
        if (salary < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
        this.salary = salary;
    }

    public EmploymentStatus getStatus() { return status; }
    public void setStatus(EmploymentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = status;
    }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getPhotoFileName() { return photoFileName; }
    public void setPhotoFileName(String photoFileName) { this.photoFileName = photoFileName; }

    // Metody pomocnicze do podziału name
    public String getFirstName() {
        if (name == null || name.trim().isEmpty()) return "";
        String[] parts = name.trim().split("\\s+");
        return parts.length > 0 ? parts[0] : name;
    }

    public String getLastName() {
        if (name == null || name.trim().isEmpty()) return "";
        String[] parts = name.trim().split("\\s+");
        if (parts.length <= 1) return "";

        // Łączymy wszystkie części oprócz pierwszej (imienia)
        StringBuilder lastName = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            if (i > 1) lastName.append(" ");
            lastName.append(parts[i]);
        }
        return lastName.toString();
    }

    private void validateInputs(String name, String email, String company, Position position, double salary, EmploymentStatus status) throws InvalidDataException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidDataException("Name cannot be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidDataException("Email cannot be null or empty");
        }
        if (company == null || company.trim().isEmpty()) {
            throw new InvalidDataException("Company cannot be null or empty");
        }
        if (position == null) {
            throw new InvalidDataException("Position cannot be null");
        }
        if (salary < 0) {
            throw new InvalidDataException("Salary cannot be negative");
        }
        if (status == null) {
            throw new InvalidDataException("Employment status cannot be null");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(email, employee.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return String.format(java.util.Locale.US,
                "Employee{id=%d, name='%s', email='%s', company='%s', position=%s, salary=%.2f, status=%s}",
                id, name, email, company, position, salary, status);
    }
}