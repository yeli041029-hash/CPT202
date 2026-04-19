package com.CY_Project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class TestController {
    @Autowired
    private ContributorService service;

    @GetMapping("/test")
    public List<ContributorApplications> test() {
        return service.getAll();
    }
}