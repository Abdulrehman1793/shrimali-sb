package com.shrimali.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private List<String> admins;
    private List<String> superAdmins;

    public void setAdmins(List<String> admins) {
        this.admins = admins;
    }

    public void setSuperAdmins(List<String> superAdmins) {
        this.superAdmins = superAdmins;
    }
}
