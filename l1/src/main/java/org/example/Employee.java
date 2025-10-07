package org.example;


import java.util.Objects;

public class Employee {
    private final String name;
    private final String email;
    private final String company;
    private final Position position;
    private final double salary;

    public Employee(String name, String email, String company, Position position, double salary) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (company == null || company.trim().isEmpty()) {
            throw new IllegalArgumentException("Company cannot be null or empty");
        }
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null");
        }
        if (salary < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }

        this.name = name.trim();
        this.email = email.trim().toLowerCase();
        this.company = company.trim();
        this.position = position;
        this.salary = salary;
    }

    public Employee(String name, String email, String company, Position position) {
        this(name, email, company, position, position.getBaseSalary());
    }

    // Gettery
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCompany() {
        return company;
    }

    public Position getPosition() {
        return position;
    }

    public double getSalary() {
        return salary;
    }

    // equals i hashCode oparte na emailu
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
        return String.format("Employee{name='%s', email='%s', company='%s', position=%s, salary=%.2f}",
                name, email, company, position, salary);
    }
}