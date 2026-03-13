-- 用户表结构
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL DEFAULT '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    age INT,
    role VARCHAR(20) DEFAULT 'user',
    status INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 项目表结构
CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_code VARCHAR(50) NOT NULL UNIQUE,
    project_name VARCHAR(100) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    status INT DEFAULT 1,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_project_code ON projects(project_code);
CREATE INDEX idx_project_name ON projects(project_name);
CREATE INDEX idx_status ON projects(status);
CREATE INDEX idx_created_at ON projects(created_at);
