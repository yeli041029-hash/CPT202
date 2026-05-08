package com.group32.cpt202.frontend;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FrontendControllerTest {

    @Test
    void indexRedirectsToWelcomePage() {
        FrontendController controller = new FrontendController();

        assertThat(controller.index()).isEqualTo("redirect:/HTML/welcome.html");
    }
}
