package src.model;

import src.exception.InvalidDataException;

import java.util.Objects;

public class Employee {
    private String name;
    private final String email;
    private String company;
    private Position position;
    private double salary;

    // KONSTRUKTORY - InvalidDataException (checked)
    public Employee(String name, String email, String company, Position position, double salary) throws InvalidDataException {
        validateInputs(name, email, company, position, salary);
        this.name = name.trim();
        this.email = email.trim().toLowerCase();
        this.company = company.trim();
        this.position = position;
        this.salary = salary;
    }

    public Employee(String name, String email, String company, Position position) throws InvalidDataException {
        this(name, email, company, position, position.getBaseSalary());
    }

    // GETTERY
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getCompany() { return company; }
    public Position getPosition() { return position; }
    public double getSalary() { return salary; }

    public String getFirstName() {
        String[] names = name.split(" ");
        return names.length > 0 ? names[0] : name;
    }

    public String getLastName() {
        String[] names = name.split(" ");
        return names.length > 1 ? names[names.length - 1] : name;
    }

    // SETTERY
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

    // WALIDACJA - InvalidDataException (checked)
    private void validateInputs(String name, String email, String company, Position position, double salary) throws InvalidDataException {
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
                "Employee{name='%s', email='%s', company='%s', position=%s, salary=%.2f}",
                name, email, company, position, salary);
    }
}