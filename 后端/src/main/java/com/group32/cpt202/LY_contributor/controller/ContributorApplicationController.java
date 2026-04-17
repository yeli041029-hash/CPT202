//用户层面的Controller
package com.group32.cpt202.LY_contributor.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group32.cpt202.LY_contributor.dto.ApplicationRequest;
import com.group32.cpt202.LY_contributor.entity.ContributorApplication;
import com.group32.cpt202.LY_contributor.service.ContributorApplicationService;

@RestController //这个类是一个后端接口类
@RequestMapping("/contributor-applications") //这个类下面所有接口，都以 /contributor-applications 开头
@CrossOrigin
public class ContributorApplicationController {
    //把 Service 接进来，Controller 不自己处理逻辑，而是调用 Service
    private ContributorApplicationService contributorApplicationService;

    public ContributorApplicationController(ContributorApplicationService contributorApplicationService) {
        this.contributorApplicationService = contributorApplicationService;
    }

    @PostMapping //这个接口是 POST 请求，如果前端发POST /contributor-applications，就会进这个方法。
    public String submitApplication(@RequestBody ApplicationRequest request) { //前端传来的 JSON，会自动变成 Java 对象
        contributorApplicationService.submitApplication(
                request.getUserId(),
                request.getApplicationReason()
        );
        return "application submitted\n";
    }

    @GetMapping("/my/{userId}")
    public ContributorApplication getMyApplication(@PathVariable Long userId) {
        return contributorApplicationService.getMyApplication(userId);
    }
}
