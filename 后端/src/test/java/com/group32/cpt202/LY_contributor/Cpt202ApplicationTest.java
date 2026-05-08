package com.group32.cpt202.LY_contributor;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class Cpt202ApplicationTest {

    @Test
    void mainDelegatesToSpringApplicationRun() {
        String[] args = {"--spring.main.web-application-type=none"};

        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            Cpt202Application.main(args);

            mocked.verify(() -> SpringApplication.run(Cpt202Application.class, args));
        }
    }
}
