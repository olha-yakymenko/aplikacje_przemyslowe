package com.techcorp.employee.dto;

import com.techcorp.employee.model.EmploymentStatus;

public interface EmployeeListView {
    String getName();
    String getEmail();
    String getPosition();
    String getCompany();
    Double getSalary(); // DODAJ
    EmploymentStatus getStatus();
    String getDepartmentName();

    default String getFirstName() {
        String[] parts = getName().split("\\s+");
        return parts.length > 0 ? parts[0] : getName();
    }

    default String getLastName() {
        String[] parts = getName().split("\\s+");
        if (parts.length <= 1) return "";

        StringBuilder lastName = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            if (i > 1) lastName.append(" ");
            lastName.append(parts[i]);
        }
        return lastName.toString();
    }
}