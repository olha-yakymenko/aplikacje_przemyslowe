package com.techcorp.employee.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nazwa departamentu jest wymagana")
    @Size(min = 2, max = 50, message = "Nazwa musi mieć od 2 do 50 znaków")
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @NotBlank(message = "Lokalizacja jest wymagana")
    @Size(max = 100, message = "Lokalizacja nie może przekraczać 100 znaków")
    @Column(nullable = false, length = 100)
    private String location;

    @Size(max = 255, message = "Opis nie może przekraczać 255 znaków")
    private String description;

    @Email(message = "Nieprawidłowy format email")
    @Column(name = "manager_email")
    private String managerEmail;

    @NotNull(message = "Budżet jest wymagany")
    @DecimalMin(value = "0.0", inclusive = false, message = "Budżet musi być większy niż 0")
    @Column(nullable = false)
    private Double budget;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Employee> employees = new ArrayList<>();

    // Konstruktory
    public Department() {}

    public Department(String name, String location, String description, String managerEmail, Double budget) {
        this.name = name;
        this.location = location;
        this.description = description;
        this.managerEmail = managerEmail;
        this.budget = budget;
    }

    // Gettery i settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getManagerEmail() { return managerEmail; }
    public void setManagerEmail(String managerEmail) { this.managerEmail = managerEmail; }

    public Double getBudget() { return budget; }
    public void setBudget(Double budget) { this.budget = budget; }

    public List<Employee> getEmployees() { return employees; }
    public void setEmployees(List<Employee> employees) { this.employees = employees; }

    // Metody pomocnicze
    public void addEmployee(Employee employee) {
        employees.add(employee);
        employee.setDepartment(this);
    }

    public void removeEmployee(Employee employee) {
        employees.remove(employee);
        employee.setDepartment(null);
    }

    @Override
    public String toString() {
        return String.format(
                "Department{id=%d, name='%s', location='%s', description='%s', managerEmail='%s', budget=%s}",
                id, name, location, description, managerEmail, budget
        );
    }
}