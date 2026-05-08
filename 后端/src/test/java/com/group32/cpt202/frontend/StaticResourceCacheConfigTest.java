package com.group32.cpt202.frontend;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import static org.assertj.core.api.Assertions.assertThat;

class StaticResourceCacheConfigTest {

    @Test
    void addResourceHandlersRegistersExpectedMappings() {
        StaticWebApplicationContext applicationContext = new StaticWebApplicationContext();
        MockServletContext servletContext = new MockServletContext();
        ResourceHandlerRegistry registry = new ResourceHandlerRegistry(applicationContext, servletContext);

        new StaticResourceCacheConfig().addResourceHandlers(registry);

        assertThat(registry.hasMappingForPattern("/HTML/**")).isTrue();
        assertThat(registry.hasMappingForPattern("/CSS/**")).isTrue();
        assertThat(registry.hasMappingForPattern("/JS/**")).isTrue();
        assertThat(registry.hasMappingForPattern("/Resources/**")).isTrue();
    }
}
