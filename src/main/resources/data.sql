-- 初始化示例数据
-- 默认密码: 123456 (BCrypt加密)
-- 密码哈希: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
INSERT INTO users (username, password, email, phone, age, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@example.com', '13800138000', 28, 'admin', 1),
('张三', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'zhangsan@example.com', '13800138001', 28, 'admin', 1),
('李四', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'lisi@example.com', '13800138002', 25, 'user', 1),
('王五', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'wangwu@example.com', '13800138003', 30, 'user', 1),
('赵六', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'zhaoliu@example.com', '13800138004', 22, 'user', 1),
('钱七', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'qianqi@example.com', '13800138005', 35, 'user', 0);

-- 初始化项目示例数据
INSERT INTO projects (project_code, project_name, description, status, start_date, end_date) VALUES
('PRJ-2024-001', '智慧城市建设项目', '打造智能化城市管理系统，包括交通监控、环境监测、公共安全等子系统', 1, '2024-01-01 00:00:00', '2024-12-31 23:59:59'),
('PRJ-2024-002', '企业数字化转型', '帮助企业实现业务流程数字化，提升运营效率', 1, '2024-03-01 00:00:00', '2024-09-30 23:59:59'),
('PRJ-2024-003', '电商平台升级', '对现有电商平台进行架构升级和性能优化', 2, '2024-02-01 00:00:00', '2024-06-30 23:59:59'),
('PRJ-2024-004', '移动APP开发', '开发企业移动办公APP，支持iOS和Android平台', 0, '2024-04-01 00:00:00', '2024-08-31 23:59:59'),
('PRJ-2024-005', '数据分析平台', '构建企业级数据分析和可视化平台', 1, '2024-05-01 00:00:00', '2024-11-30 23:59:59');
