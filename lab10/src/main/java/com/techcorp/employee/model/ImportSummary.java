//
//
//
//
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

    public boolean hasErrors() {
        return !getErrors().isEmpty();
    }


}






//
//
//package com.techcorp.employee.model;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ImportSummary {
//    private int totalRecords;
//    private int importedRecords;
//    private int failedRecords;
//    private ImportStatus status;
//    private List<ImportError> errors = new ArrayList<>();
//
//    public ImportSummary() {
//        this.status = ImportStatus.PENDING;
//    }
//
//    // Metody do inkrementacji
//    public void incrementImported() {
//        importedRecords++;
//        totalRecords++;
//        updateStatus();
//    }
//
//    public void incrementFailed() {
//        failedRecords++;
//        totalRecords++;
//        updateStatus();
//    }
//
//    public void addTotalRecord() {
//        totalRecords++;
//        updateStatus();
//    }
//
//    // Metody dodawania błędów
//    public void addError(String errorType, String message, int lineNumber) {
//        errors.add(new ImportError(errorType, message, lineNumber));
//        incrementFailed();
//    }
//
//    public void addError(String message) {
//        errors.add(new ImportError("VALIDATION_ERROR", message, 0));
//        incrementFailed();
//    }
//
//    private void updateStatus() {
//        if (totalRecords == 0) {
//            status = ImportStatus.NO_DATA;
//        } else if (failedRecords == totalRecords) {
//            status = ImportStatus.FAILED;
//        } else if (failedRecords > 0) {
//            status = ImportStatus.PARTIAL_SUCCESS;
//        } else if (importedRecords > 0) {
//            status = ImportStatus.SUCCESS;
//        }
//    }
//
//    // Gettery i settery
//    public int getTotalRecords() { return totalRecords; }
//    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; updateStatus(); }
//
//    public int getImportedRecords() { return importedRecords; }
//    public void setImportedRecords(int importedRecords) { this.importedRecords = importedRecords; updateStatus(); }
//
//    public int getFailedRecords() { return failedRecords; }
//    public void setFailedRecords(int failedRecords) { this.failedRecords = failedRecords; updateStatus(); }
//
//    public ImportStatus getStatus() { return status; }
//    public void setStatus(ImportStatus status) { this.status = status; }
//
//    public List<ImportError> getErrors() { return errors; }
//    public void setErrors(List<ImportError> errors) { this.errors = errors; }
//
//    @Override
//    public String toString() {
//        return String.format("ImportSummary{status=%s, total=%d, imported=%d, failed=%d, errors=%d}",
//                status, totalRecords, importedRecords, failedRecords, errors.size());
//    }
//}