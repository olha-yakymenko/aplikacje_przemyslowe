package com.techcorp.employee.dto;

import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.Position;

public class EmployeeDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String company;
    private Position position;
    private double salary;
    private EmploymentStatus status;

    // Konstruktory
    public EmployeeDTO() {}

    public EmployeeDTO(String firstName, String lastName, String email, String company,
                       Position position, double salary, EmploymentStatus status) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.company = company;
        this.position = position;
        this.salary = salary;
        this.status = status;
    }

    // Gettery i settery
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    public EmploymentStatus getStatus() { return status; }
    public void setStatus(EmploymentStatus status) { this.status = status; }
}