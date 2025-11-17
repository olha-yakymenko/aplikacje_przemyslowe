package com.techcorp.employee.model;

public class Department {
    private Long id;
    private String name;
    private String description;
    private String managerEmail;
    private Double budget;

    // Pusty konstruktor
    public Department() {}

    // Gettery i settery dla WSZYSTKICH pól
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // GETTER I SETTER DLA DESCRIPTION - TO JEST BRAKUJĄCE!
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getManagerEmail() { return managerEmail; }
    public void setManagerEmail(String managerEmail) { this.managerEmail = managerEmail; }

    public Double getBudget() { return budget; }
    public void setBudget(Double budget) { this.budget = budget; }

    // Opcjonalnie: toString() dla debugowania
    @Override
    public String toString() {
        return String.format(
                "Department{id=%d, name='%s', description='%s', managerEmail='%s', budget=%s}",
                id, name, description, managerEmail, budget
        );
    }
}