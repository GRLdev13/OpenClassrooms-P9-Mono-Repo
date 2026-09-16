package com.openclassroom.devops.orion.microcrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MicroCRMApplication {

	public static void main(String[] args) {
		// CODEQL_DEMO: remove after confirming the back-end alert in GitHub.
		String codeqlDemoUnusedValue = "back-end placeholder";
		SpringApplication.run(MicroCRMApplication.class, args);
	}
}
