// 用户仓库
// 该接口继承自 JpaRepository，提供了对 User 实体的基本数据库操作方法，如保存、删除、查找等。通过使用 Spring Data JPA，开发者可以直接调用这些方法来访问数据库中的用户数据，而无需编写具体的 SQL 查询语句。这使得数据访问变得更加简洁和高效，同时也提高了代码的可读性和维护性  
package com.group32.cpt202.LY_contributor.repository;

import com.group32.cpt202.LY_contributor.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
