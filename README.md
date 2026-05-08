# CPT202
文化遗产项目网站

## 服务器适配

后端默认从环境变量读取数据库和端口配置；如果没有提供环境变量，会回退到本机 MySQL 默认值。

### 1. 导入数据库

在服务器上先创建数据库，再导入项目根目录下的 SQL 文件：

```bash
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS heritage_platform_test DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -uroot -p heritage_platform_test < heritage_platform_test_schema.sql
```

### 2. 设置运行环境变量

```bash
export DB_URL='jdbc:mysql://127.0.0.1:3306/heritage_platform_test?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true'
export DB_USERNAME='root'
export DB_PASSWORD='你的数据库密码'
export SERVER_PORT='8090'
export SHOW_SQL='false'
```

如果你要连接别的数据库，只覆盖 DB_URL 即可，不需要改源码。

### 3. 启动后端

在项目目录执行：

```bash
SERVER_PORT=8090 ./mvnw -pl 后端 spring-boot:run
```

或先打包再运行：

```bash
./mvnw -pl 后端 -DskipTests package
java -jar 后端/target/cpt202-0.0.1-SNAPSHOT.jar --server.port=8090
```

### 4. 说明

- `code` 目录里的前端页面会被 Maven 复制到 Spring Boot 的静态资源目录，不需要单独部署前端服务。
- `target` 目录里保留的 Windows 路径只是旧的编译产物痕迹，服务器上重新构建后会被新的 Linux 产物覆盖。
- 如果服务器要对外访问，还需要放行 `SERVER_PORT` 对应端口，并确保 MySQL 监听地址和账号权限正确。
