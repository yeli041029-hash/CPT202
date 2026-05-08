package com.group32.cpt202.frontend;

import java.time.Duration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceCacheConfig implements WebMvcConfigurer {

    private static final CacheControl HTML_CACHE = CacheControl.noCache().mustRevalidate();
    private static final CacheControl ASSET_CACHE = CacheControl.maxAge(Duration.ofDays(30)).cachePublic().mustRevalidate();

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/HTML/**")
            .addResourceLocations("classpath:/static/HTML/")
            .setCacheControl(HTML_CACHE);

        registry.addResourceHandler("/CSS/**")
            .addResourceLocations("classpath:/static/CSS/")
            .setCacheControl(ASSET_CACHE);

        registry.addResourceHandler("/JS/**")
            .addResourceLocations("classpath:/static/JS/")
            .setCacheControl(ASSET_CACHE);

        registry.addResourceHandler("/Resources/**")
            .addResourceLocations("classpath:/static/Resources/")
            .setCacheControl(ASSET_CACHE);
    }
}