## 后端部署说明

这个模块是 Spring Boot 后端，运行时需要 Java 17 和 MySQL 8。

### 1. 初始化数据库

先在服务器上创建数据库，然后从项目根目录导入 SQL：

```bash
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS heritage_platform_test DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -uroot -p heritage_platform_test < heritage_platform_test_schema.sql
```

如果你要连接别的数据库，只需要覆盖 DB_URL。

### 2. 配置环境变量

后端已经改成优先读取环境变量；没有提供时，会回退到本机默认值：

```bash
export DB_URL='jdbc:mysql://127.0.0.1:3306/heritage_platform_test?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true'
export DB_USERNAME='root'
export DB_PASSWORD='你的数据库密码'
export SERVER_PORT='8090'
export SHOW_SQL='false'
export DDL_AUTO='update'
```

### 3. 启动后端

在 CPT202 根目录运行：

```bash
SERVER_PORT=8090 ./mvnw -pl 后端 spring-boot:run
```

或者先打包再运行：

```bash
./mvnw -pl 后端 -DskipTests package
java -jar target/cpt202-0.0.1-SNAPSHOT.jar --server.port=8090
```

上面的 jar 启动命令需要在后端目录中执行；如果在项目根目录执行，路径应改成 后端/target/cpt202-0.0.1-SNAPSHOT.jar。

### 4. 服务器注意事项

- 前端静态资源来自 ../code，Maven 打包时会自动复制到 static 目录。
- 如果服务器对外提供访问，还需要放行 SERVER_PORT 对应端口。
- 如果 MySQL 不在本机，直接修改 DB_URL，不要再改源码里的 application.properties。
