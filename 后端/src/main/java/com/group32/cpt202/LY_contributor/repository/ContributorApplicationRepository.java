// 贡献者申请仓库
// 该接口继承自 JpaRepository，提供了对 ContributorApplication 实体的基本数据库操作方法，如保存、删除、查找等。它还定义了两个自定义查询方法：findByUserId 和 findByStatus，分别用于根据用户ID和申请状态查询申请列表。通过使用 Spring Data JPA，开发者可以直接调用这些方法来访问数据库中的贡献者申请数据，而无需编写具体的 SQL 查询语句。这使得数据访问变得更加简洁和高效，同时也提高了代码的可读性和维护性  
// 该接口会被 Spring Boot 自动实现，并且会在运行时生成相应的数据库查询代码。开发者只需要注入这个接口，就可以方便地进行贡献者申请相关的数据操作，如保存新的申请、查询用户的申请记录、以及查询待审批的申请列表等。这是系统中处理贡献者申请数据的核心组件之一，其他服务和功能都会依赖于它来进行数据访问和操作。
package com.group32.cpt202.LY_contributor.repository;

import com.group32.cpt202.LY_contributor.entity.ContributorApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContributorApplicationRepository extends JpaRepository<ContributorApplication, Long> {

    // 根据用户ID查申请
    List<ContributorApplication> findByUserId(Long userId);

    // 按创建时间和ID倒序查用户申请，确保最新记录排在最前
    List<ContributorApplication> findByUserIdOrderByCreatedAtDescIdDesc(Long userId);

    // 根据状态查申请
    List<ContributorApplication> findByStatus(ContributorApplication.Status status);
}
