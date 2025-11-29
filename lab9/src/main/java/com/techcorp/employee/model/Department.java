//package com.techcorp.employee.model;
//
////public class Department {
////    private Long id;
////    private String name;
////    private String description;
////    private String managerEmail;
////    private Double budget;
//
//
//import jakarta.validation.constraints.*;
//
//public class Department {
//    private Long id;
//
//    @NotBlank(message = "Nazwa departamentu jest wymagana")
//    @Size(min = 2, max = 50, message = "Nazwa musi mieć od 2 do 50 znaków")
//    private String name;
//
//    @Size(max = 255, message = "Opis nie może przekraczać 255 znaków")
//    private String description;
//
//    @Email(message = "Nieprawidłowy format email")
//    private String managerEmail;
//
//    @NotNull(message = "Budżet jest wymagany")
//    @Min(value = 0, message = "Budżet musi być liczbą dodatnią")
//    private Double budget;
//
//    // Pusty konstruktor
//    public Department() {}
//
//    public Department(Long id, String name, String description, String managerEmail, Double budget) {
//        this.id = id;
//        this.name = name;
//        this.description = description;
//        this.managerEmail = managerEmail;
//        this.budget = budget;
//    }
//
//    public Department(String name, String description, String managerEmail, Double budget) {
//        this.name = name;
//        this.description = description;
//        this.managerEmail = managerEmail;
//        this.budget = budget;
//    }
//
//    // Gettery i settery dla WSZYSTKICH pól
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//
//    // GETTER I SETTER DLA DESCRIPTION - TO JEST BRAKUJĄCE!
//    public String getDescription() { return description; }
//    public void setDescription(String description) { this.description = description; }
//
//    public String getManagerEmail() { return managerEmail; }
//    public void setManagerEmail(String managerEmail) { this.managerEmail = managerEmail; }
//
//    public Double getBudget() { return budget; }
//    public void setBudget(Double budget) { this.budget = budget; }
//
//    // Opcjonalnie: toString() dla debugowania
//    @Override
//    public String toString() {
//        return String.format(
//                "Department{id=%d, name='%s', description='%s', managerEmail='%s', budget=%s}",
//                id, name, description, managerEmail, budget
//        );
//    }
//}





package com.techcorp.employee.model;

import jakarta.validation.constraints.*;

public class Department {
    private Long id;

    @NotBlank(message = "Nazwa departamentu jest wymagana")
    @Size(min = 2, max = 50, message = "Nazwa musi mieć od 2 do 50 znaków")
    private String name;

    @NotBlank(message = "Lokalizacja jest wymagana")
    @Size(max = 100, message = "Lokalizacja nie może przekraczać 100 znaków")
    private String location;

    @Size(max = 255, message = "Opis nie może przekraczać 255 znaków")
    private String description;

    @Email(message = "Nieprawidłowy format email")
    private String managerEmail;

    @NotNull(message = "Budżet jest wymagany")
    @DecimalMin(value = "0.0", inclusive = false, message = "Budżet musi być większy niż 0")
    private Double budget;

    // Konstruktory
    public Department() {}

    public Department(Long id, String name, String location, String description, String managerEmail, Double budget) {
        this.id = id;
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

    @Override
    public String toString() {
        return String.format(
                "Department{id=%d, name='%s', location='%s', description='%s', managerEmail='%s', budget=%s}",
                id, name, location, description, managerEmail, budget
        );
    }
}