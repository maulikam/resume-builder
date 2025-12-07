package com.resumebuilder.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.resumebuilder.backend.config.AppProperties;
import com.resumebuilder.backend.config.JwtConfig;

@SpringBootApplication
@EnableConfigurationProperties({AppProperties.class, JwtConfig.class})
public class ResumeBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResumeBackendApplication.class, args);
	}

}
