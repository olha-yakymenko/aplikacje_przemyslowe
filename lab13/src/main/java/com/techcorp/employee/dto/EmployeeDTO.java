

package com.techcorp.employee.dto;

import com.techcorp.employee.model.Department;
import com.techcorp.employee.model.EmploymentStatus;
import com.techcorp.employee.model.Position;
import com.techcorp.employee.validation.TechCorpEmail;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class EmployeeDTO {
    @NotBlank(message = "Imię jest wymagane")
    @Size(min = 2, max = 50, message = "Imię musi mieć od 2 do 50 znaków")
    private String firstName;

    @NotBlank(message = "Nazwisko jest wymagane")
    @Size(min = 2, max = 50, message = "Nazwisko musi mieć od 2 do 50 znaków")
    private String lastName;

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Nieprawidłowy format email")
    @TechCorpEmail(message = "Email musi być w domenie @techcorp.com")
    private String email;

    @NotBlank(message = "Firma jest wymagana")
    @Size(min = 2, max = 100, message = "Nazwa firmy musi mieć od 2 do 100 znaków")
    private String company;

    @NotNull(message = "Stanowisko jest wymagane")
    private Position position;

    @NotNull(message = "Wynagrodzenie jest wymagane")
    @DecimalMin(value = "0.0", inclusive = false, message = "Wynagrodzenie musi być większe niż 0")
    @Max(value = 1000000, message = "Wynagrodzenie nie może przekraczać 1,000,000")
    private BigDecimal salary;

    @NotNull(message = "Status jest wymagany")
    private EmploymentStatus status;

    private Long departmentId;


    // Konstruktory
    public EmployeeDTO() {}

    public EmployeeDTO(String firstName, String lastName, String email, String company,
                       Position position, BigDecimal salary, EmploymentStatus status) {
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

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    public EmploymentStatus getStatus() { return status; }
    public void setStatus(EmploymentStatus status) { this.status = status; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    @Override
    public String toString() {
        return "EmployeeDTO{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", company='" + company + '\'' +
                ", position=" + position +
                ", salary=" + salary +
                ", status=" + status +
                ", departmentId=" + departmentId +
                '}';
    }

}