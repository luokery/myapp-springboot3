# myapp-springboot3
myapp-springboot3

## 功能特性

- **用户管理**：用户 CRUD、角色管理、状态管理
- **项目管理**：项目 CRUD、分页查询、状态管理
- **认证授权**：JWT Token 认证、Shiro 权限控制
- **缓存支持**：Caffeine 本地缓存
- **数据库连接池**：Druid 连接池 + 监控
- **优雅停机**：支持 Spring Boot 优雅停机，确保请求不中断

## 优雅停机

### 配置说明

```yaml
server:
  shutdown: graceful  # 启用优雅停机

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s  # 停机超时时间
```

### 触发方式

1. **SIGTERM 信号**（推荐）：
   ```bash
   # 发送 SIGTERM 信号触发优雅停机
   kill -15 <pid>
   ```

2. **API 接口**（仅用于测试）：
   ```bash
   # 通过 API 触发优雅停机
   curl -X POST http://localhost:5000/api/admin/shutdown \
     -H "Authorization: Bearer <token>"
   ```

### 停机流程

1. 停止接收新请求
2. 等待正在处理的请求完成（最长 30 秒）
3. 清理缓存
4. 关闭线程池
5. 关闭数据库连接池

### 应用状态查询

```bash
# 获取应用运行状态
curl -X POST http://localhost:5000/api/admin/status \
  -H "Authorization: Bearer <token>"
```



[project]
requires = ["java-17"]

[dev]
build = ["mvn", "clean", "compile", "-DskipTests"]
run = ["mvn", "spring-boot:run", "-Dspring-boot.run.arguments=--server.port=5000"]

[deploy]
build = ["mvn", "clean", "package", "-DskipTests"]
run = ["java", "-jar", "target/user-management-1.0.0.jar", "--server.port=5000"]

-- 编译
mvn clean package -DskipTests
-- 运行
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=5000
java -jar target/user-management-1.0.0.jar --server.port=5000

# 编译
```sh
cd /workspace/projects && mvn clean compile -DskipTests 2>&1 | tail -100
cd /workspace/projects && nohup mvn compile -DskipTests > /tmp/mvn-compile.log 2>&1 &

--杀maven进程
pkill -9 -f mvn; sleep 2; ps aux | grep mvn | grep -v grep
```


# 检查环境
```sh
which java && which mvn
-- 检查mvn
which mvn && mvn --version | head -3
-- 检查java 和maven
java -version 2>&1 && echo "---" && ls -la /usr/share/maven/ 2>/dev/null || echo "no maven dir"
-- 检查文件系统是否存在
ls -la /usr/share/maven/bin/mvn 2>/dev/null || ls -la /usr/local/bin/mvn 2>/dev/null || find /usr -name "mvn" -type f 2>/dev/null | head -5
-- 检查java环境
java -version 2>&1 && echo "---" && ls -la /usr/share/maven/ 2>/dev/null || echo "no maven dir"

find /usr -name "java" -type f 2>/dev/null | head -5; find /usr -name "mvn" -type f 2>/dev/null | head -5
```

# 检查java进程

```sh
ps aux | grep java | grep -v grep | head -3
```

# 修复环境
```sh
-- 改阿里源
sudo cp /etc/apt/sources.list /etc/apt/sources.list.bak
sudo sed -i 's/us.archive.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list
sudo sed -i 's/security.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list
sudo apt update

sudo cp /etc/apt/sources.list.bak /etc/apt/sources.list
sudo apt update
-- 改阿里源(非标)
cp /etc/apt/sources.list.d/ubuntu.sources /etc/apt/sources.list.d/ubuntu.sources.bak 
sed -i 's/us.archive.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list.d/ubuntu.sources
sed -i 's/security.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list.d/ubuntu.sources
sed -i 's/archive.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list.d/ubuntu.sources
apt update

apt-get install -y openjdk-17-jdk maven 2>&1 | tail -30
或者
apt-get update && apt-get install -y default-jdk maven 2>&1 | tail -20

kill -9 438 2>/dev/null; sleep 5; apt-get install -y openjdk-17-jdk maven 2>&1 | tail -30
```

```sh
-- maven配置修改
/usr/share/maven/conf/settings.xml
cp /workspace/projects/settings.xml /usr/share/maven/conf/settings.xml

```

# 杀进程并检查端口
```sh
kill -9 3493 2>/dev/null; sleep 2; ss -lptn 'sport = :5000' 2>/dev/null
```

# 注册
```sh
curl -s -X POST http://localhost:5000/api/auth/register \ -H "Content-Type: application/json" \ -d '{"username":"testuser","password":"Test123!","email":"test@example.com"}'
```

# 获取token
```sh
curl -s -X POST http://localhost:5000/api/auth/login \ -H "Content-Type: application/json" \ -d '{"username":"testuser","password":"Test123!"}'
```
