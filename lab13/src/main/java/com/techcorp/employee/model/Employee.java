package com.techcorp.employee.model;

import com.techcorp.employee.validation.TechCorpEmail;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Imię i nazwisko są wymagane")
    @Size(min = 2, max = 100, message = "Imię i nazwisko musi mieć od 2 do 100 znaków")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Nieprawidłowy format email")
    @Column(nullable = false, unique = true, length = 100)
    @TechCorpEmail
    private String email;

    @NotBlank(message = "Firma jest wymagana")
    @Column(nullable = false, length = 100)
    private String company;

    @NotNull(message = "Stanowisko jest wymagane")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Position position;

    @Min(value = 0, message = "Wynagrodzenie musi być liczbą dodatnią")
    @Column(nullable = false)
    private BigDecimal salary;

    @NotNull(message = "Status jest wymagany")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmploymentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "photo_file_name")
    private String photoFileName;

    // Konstruktory
    public Employee() {}

    public Employee(String name, String email, String company, Position position, BigDecimal salary, EmploymentStatus status) {
        this.name = name.trim();
        this.email = email.trim().toLowerCase();
        this.company = company.trim();
        this.position = position;
        this.salary = salary;
        this.status = status;
    }

    public Employee(String name, String email, String company, Position position, BigDecimal salary, EmploymentStatus status, Department department) {
        this.name = name.trim();
        this.email = email.trim().toLowerCase();
        this.company = company.trim();
        this.position = position;
        this.salary = salary;
        this.status = status;
        this.department = department;
    }

    public Employee(String name, String email, String company, Position position, BigDecimal salary) {
        this.name = name.trim();
        this.email = email.trim().toLowerCase();
        this.company = company.trim();
        this.position = position;
        this.salary = salary;
        this.status = EmploymentStatus.ACTIVE;
    }
//
//    public Employee(String name, String email, String company, Position position, double salary, EmploymentStatus employmentStatus) {
//    }


//    public Employee(String name, String email, String company, String position, double)

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

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) {
        if (salary.compareTo(BigDecimal.ZERO) < 0) {
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

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

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

        StringBuilder lastName = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            if (i > 1) lastName.append(" ");
            lastName.append(parts[i]);
        }
        return lastName.toString();
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