Uruchomienie aplikacji:
mvn spring-boot:run

Pełny build z weryfikacją:
mvn clean verify

Kompilacja i uruchomienie testów:
mvn clean test   

Raport pokrycia w target/site/jacoco/index.html: 
mvn jacoco:report          

Pełny cykl życia z weryfikacją: 
mvn clean verify           



Endpointy:

# 1. Wszyscy pracownicy
curl -X GET http://localhost:8080/api/employees

# 2. Pracownicy po firmie
curl -X GET "http://localhost:8080/api/employees?company=TechCorp"

# 3. Pracownik po emailu
curl -X GET http://localhost:8080/api/employees/jan.kowalski@example.com

# 4. Pracownicy po statusie
curl -X GET http://localhost:8080/api/employees/status/ACTIVE

# 5. Nowy pracownik (ACTIVE)
curl -X POST http://localhost:8080/api/employees \
-H "Content-Type: application/json" \
-d '{
"firstName": "Jan",
"lastName": "Kowalski",
"email": "jan.kowalski@example.com",
"company": "TechCorp",
"position": "PROGRAMMER",
"salary": 8500.00,
"status": "ACTIVE"
}'

# 6. Nowy pracownik (MANAGER)
curl -X POST http://localhost:8080/api/employees \
-H "Content-Type: application/json" \
-d '{
"firstName": "Anna",
"lastName": "Nowak",
"email": "anna.nowak@example.com",
"company": "TechCorp",
"position": "MANAGER",
"salary": 15000.00,
"status": "ACTIVE"
}'

# 7. Aktualizacja pracownika
curl -X PUT http://localhost:8080/api/employees/jan.kowalski@example.com \
-H "Content-Type: application/json" \
-d '{
"firstName": "Jan",
"lastName": "Kowalski",
"email": "jan.kowalski@example.com",
"company": "TechCorp",
"position": "MANAGER",
"salary": 13000.00,
"status": "ACTIVE"
}'

# 8. Zmiana statusu na ON_LEAVE
curl -X PATCH http://localhost:8080/api/employees/jan.kowalski@example.com/status \
-H "Content-Type: application/json" \
-d '{"status": "ON_LEAVE"}'

# 9. Zmiana statusu na TERMINATED
curl -X PATCH http://localhost:8080/api/employees/anna.nowak@example.com/status \
-H "Content-Type: application/json" \
-d '{"status": "TERMINATED"}'

# 10. Usuwanie pracownika
curl -X DELETE http://localhost:8080/api/employees/jan.kowalski@example.com

# 11. Średnie wynagrodzenie (wszyscy)
curl -X GET "http://localhost:8080/api/statistics/salary/average"

# 12. Średnie wynagrodzenie w firmie
curl -X GET "http://localhost:8080/api/statistics/salary/average?company=TechCorp"

# 13. Statystyki firmy
curl -X GET "http://localhost:8080/api/statistics/company/TechCorp"

# 14. Liczba pracowników na stanowiskach
curl -X GET "http://localhost:8080/api/statistics/positions"

# 15. Rozkład statusów zatrudnienia
curl -X GET "http://localhost:8080/api/statistics/status"

# 16. Duplikat emaila - powinien zwrócić 409 Conflict
curl -X POST http://localhost:8080/api/employees \
-H "Content-Type: application/json" \
-d '{
"firstName": "Tomasz",
"lastName": "Testowy",
"email": "jan.kowalski@example.com", # TEN SAM EMAIL
"company": "TechCorp",
"position": "PROGRAMMER",
"salary": 8000.00,
"status": "ACTIVE"
}'

# 17. Pobieranie nieistniejącego pracownika - 404 Not Found
curl -X GET http://localhost:8080/api/employees/nieistniejacy@example.com

# 18. Usuwanie nieistniejącego pracownika - 404 Not Found
curl -X DELETE http://localhost:8080/api/employees/nieistniejacy@example.com

# 19. Nieprawidłowy email - 400 Bad Request
curl -X POST http://localhost:8080/api/employees \
-H "Content-Type: application/json" \
-d '{
"firstName": "Test",
"lastName": "User",
"email": "nieprawidlowy-email",
"company": "TechCorp",
"position": "PROGRAMMER",
"salary": 8000.00,
"status": "ACTIVE"
}'

# 20. Ujemne wynagrodzenie - 400 Bad Request
curl -X POST http://localhost:8080/api/employees \
-H "Content-Type: application/json" \
-d '{
"firstName": "Test",
"lastName": "User",
"email": "test@example.com",
"company": "TechCorp",
"position": "PROGRAMMER",
"salary": -1000.00, # UJEMNE WYNAGRODZENIE
"status": "ACTIVE"
}'


--------
curl -X POST http://localhost:8080/api/files/import/csv \
-F "file=@employees.csv"


curl -X POST http://localhost:8080/api/files/import/xml \
-F "file=@employees.xml" 

curl -X POST http://localhost:8080/api/files/documents/john.doe@company.com \
-F "file=@contract.pdf" \
-F "type=CONTRACT"

curl -X POST http://localhost:8080/api/files/documents/john.doe@company.com \                 
-F "file=@contract.pdf" \
-F "type=CONTRACT"

curl http://localhost:8080/api/files/documents/john.doe@company.com

curl -X POST http://localhost:8080/api/files/photos/john.doe@company.com \
-F "file=@photo.png"

curl http://localhost:8080/api/files/photos/john.doe@company.com \
--output photo.jpg


curl -X GET "http://localhost:8080/api/files/reports/statistics/TechCorp" \
--output techcorp_statistics.pdf

curl -X GET "http://localhost:8080/api/files/export/csv" --output employees_all.csv

curl -X GET "http://localhost:8080/api/files/export/csv?company=TechCorp" --output google_employees.csv

bledy:
# Pusty plik
curl -X POST http://localhost:8080/api/files/import/csv \
-F "file=@empty_file.csv"

# Plik bez nazwy
echo "test,data" | curl -X POST http://localhost:8080/api/files/import/csv \
-F "file=@-"

# Nieprawidłowy email pracownika (dla dokumentów/zdjęć)
curl -X POST "http://localhost:8080/api/files/documents/invalid-email" \
-F "file=@test.pdf" \
-F "type=CONTRACT"

# Nieistniejący pracownik (dokumenty)
curl -X GET "http://localhost:8080/api/files/documents/nonexistent@example.com"

# Nieistniejący dokument
curl -X GET "http://localhost:8080/api/files/documents/john.smith@techcorp.com/99999"

# Nieistniejące zdjęcie
curl -X GET "http://localhost:8080/api/files/photos/nonexistent@example.com"