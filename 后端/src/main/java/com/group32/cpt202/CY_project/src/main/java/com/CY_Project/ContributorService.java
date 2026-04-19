package com.CY_Project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ContributorService {
    @Autowired
    private ContributorMapper mapper;

    public List<ContributorApplications> getAll() {
        return mapper.getAllApplications();
    }
}