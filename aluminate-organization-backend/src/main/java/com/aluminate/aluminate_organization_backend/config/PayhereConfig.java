package com.aluminate.aluminate_organization_backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "payhere")
public class PayhereConfig {
    private String merchantId;
    private String secret;
}
