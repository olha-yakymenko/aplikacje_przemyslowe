



package com.techcorp.employee.model;

import java.util.ArrayList;
import java.util.List;

public class ImportSummary {
    private int importedCount;
    private List<String> errors = new ArrayList<>();

    public ImportSummary() {}

    public void incrementImported() { importedCount++; }
    public void addError(String error) { errors.add(error); }

    public int getImportedCount() { return importedCount; }
    public List<String> getErrors() { return errors; }

    @Override
    public String toString() {
        return "Imported: " + importedCount + ", Errors: " + errors;
    }
}