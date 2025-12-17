package com.techcorp.employee.model;


import java.math.BigDecimal;
import java.util.Comparator;

public enum Position {
    PRESIDENT(new BigDecimal(25000), 1),
    VICE_PRESIDENT(new BigDecimal(18000), 2),
    MANAGER(new BigDecimal(12000), 3),
    PROGRAMMER(new BigDecimal(8000), 4),
    INTERN(new BigDecimal(3000), 5);

    private final BigDecimal baseSalary;
    private final int hierarchyLevel;

    Position(BigDecimal baseSalary, int hierarchyLevel) {
        this.baseSalary = baseSalary;
        this.hierarchyLevel = hierarchyLevel;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public int getHierarchyLevel() {
        return hierarchyLevel;
    }

    public static Comparator<Position> getHierarchyComparator() {
        return Comparator.comparingInt(Position::getHierarchyLevel);
    }
}