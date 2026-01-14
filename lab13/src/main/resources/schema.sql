CREATE TABLE IF NOT EXISTS employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    salary DECIMAL(10,2) NOT NULL,
    position VARCHAR(50) NOT NULL,
    company VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    department_id BIGINT,
    photo_file_name VARCHAR(255)
);

--CREATE TABLE IF NOT EXISTS departments (
--    id BIGINT AUTO_INCREMENT PRIMARY KEY,
--    name VARCHAR(255) NOT NULL,
--    description VARCHAR(1000),
--    manager_email VARCHAR(255),
--    budget DECIMAL(15,2)
--);