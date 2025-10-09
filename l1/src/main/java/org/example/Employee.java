package org.example;

import java.util.Objects;

public class Employee {
    private String name;
    private final String email;
    private String company;
    private Position position;
    private double salary;


    public Employee(String name, String email, String company, Position position, double salary) {
        validateInputs(name, email, company, position, salary);

        this.name = name.trim();
        this.email = email.trim().toLowerCase();
        this.company = company.trim();
        this.position = position;
        this.salary = Math.max(salary, position.getBaseSalary()); // Pensja nie może być niższa od bazowej
    }

    public Employee(String name, String email, String company, Position position) {
        this(name, email, company, position, position.getBaseSalary());
    }

    // Gettery
    public String getName() {
        return name;
    }

    public String getFirstName() {
        String[] names = name.split(" ");
        return names.length > 0 ? names[0] : name;
    }

    public String getLastName() {
        String[] names = name.split(" ");
        return names.length > 1 ? names[names.length - 1] : name;
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
        return String.format(java.util.Locale.US,
                "Employee{name='%s', email='%s', company='%s', position=%s, salary=%.2f}",
                name, email, company, position, salary);
    }


    private void validateInputs(String name, String email, String company, Position position, double salary) {
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
    }



    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name.trim();
    }

    public void setCompany(String company) {
        if (company == null || company.trim().isEmpty()) {
            throw new IllegalArgumentException("Company cannot be null or empty");
        }
        this.company = company.trim();
    }

    public void setPosition(Position position) {
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null");
        }
        this.position = position;
        this.salary = Math.max(this.salary, position.getBaseSalary());
    }

    public void setSalary(double salary) {
        if (salary < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
        this.salary = Math.max(salary, position.getBaseSalary());
    }

}