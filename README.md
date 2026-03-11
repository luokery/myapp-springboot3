# myapp-springboot3
myapp-springboot3



[project]
requires = ["java-17"]

[dev]
build = ["mvn", "clean", "compile", "-DskipTests"]
run = ["mvn", "spring-boot:run", "-Dspring-boot.run.arguments=--server.port=5000"]

[deploy]
build = ["mvn", "clean", "package", "-DskipTests"]
run = ["java", "-jar", "target/user-management-1.0.0.jar", "--server.port=5000"]


# 编译
cd /workspace/projects && mvn clean compile -DskipTests 2>&1 | tail -100
cd /workspace/projects && nohup mvn compile -DskipTests > /tmp/mvn-compile.log 2>&1 &
--杀maven进程
pkill -9 -f mvn; sleep 2; ps aux | grep mvn | grep -v grep
# 检查环境
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

# 检查java进程
ps aux | grep java | grep -v grep | head -3


# 修复环境
apt-get update && apt-get install -y default-jdk maven 2>&1 | tail -20

kill -9 438 2>/dev/null; sleep 5; apt-get install -y openjdk-17-jdk maven 2>&1 | tail -30
# 杀进程并检查端口
kill -9 3493 2>/dev/null; sleep 2; ss -lptn 'sport = :5000' 2>/dev/null
# 注册
curl -s -X POST http://localhost:5000/api/auth/register \ -H "Content-Type: application/json" \ -d '{"username":"testuser","password":"Test123!","email":"test@example.com"}'
# 获取token
curl -s -X POST http://localhost:5000/api/auth/login \ -H "Content-Type: application/json" \ -d '{"username":"testuser","password":"Test123!"}'