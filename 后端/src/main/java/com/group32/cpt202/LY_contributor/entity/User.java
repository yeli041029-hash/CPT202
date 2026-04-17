// 用户实体类
// 该类使用 JPA 注解定义了一个名为 User 的实体类，
// 对应数据库中的 users 表。它包含了用户的基本信息，如用户名、密码和角色。
// 该类还定义了一个枚举类型 Role，表示用户的角色，可以是管理员、普通用户或贡献者。通过使用 Lombok 的 @Data 注解，
// 该类自动生成了 getter、setter、toString、equals 和 hashCode 方法，简化了代码的编写。这个实体类是系统中用户数据的基础，其他服务和功能都会依赖于它来进行用户相关的操作，如身份验证、权限控制和贡献者申请等。


package com.group32.cpt202.LY_contributor.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role {
        ADMIN,
        USER,
        CONTRIBUTOR
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    // role 的 getter 和 setter
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
    
    

}
