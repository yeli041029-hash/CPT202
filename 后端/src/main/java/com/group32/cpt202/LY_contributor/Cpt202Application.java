// 贡献者申请服务
// 负责处理用户提交的贡献者申请，以及管理员查看待审批的申请列表
package com.group32.cpt202.LY_contributor;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.group32.cpt202")
@EnableJpaRepositories(basePackages = "com.group32.cpt202")
@EntityScan(basePackages = "com.group32.cpt202")
public class Cpt202Application {
    public static void main(String[] args) {
        SpringApplication.run(Cpt202Application.class, args);
    }
}
