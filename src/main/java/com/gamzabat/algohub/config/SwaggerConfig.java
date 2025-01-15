package com.gamzabat.algohub.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gamzabat.algohub.constants.ApiConstants;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

	@Value("${server.port}")
	private String serverPort;

	@Bean
	public OpenAPI openAPI() {
		SecurityScheme scheme = new SecurityScheme()
			.type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
			.in(SecurityScheme.In.HEADER).name("Authorization");
		SecurityRequirement requirement = new SecurityRequirement().addList("bearerAuth");

		Server prodServer = new Server();
		prodServer.setDescription("Algohub API");
		prodServer.setUrl(ApiConstants.SERVER_HTTPS_ENDPOINT);

		Server localServer = new Server();
		localServer.setDescription("Algohub API for LOCAL");
		localServer.setUrl("http://localhost:" + serverPort);

		return new OpenAPI()
			.components(new Components().addSecuritySchemes("bearerAuth", scheme))
			.security(Collections.singletonList(requirement))
			.info(new Info()
				.title("AlgoHub API 명세서")
				.description("AlgoHub API 명세서 입니다.")
				.version("1.0.0"))
			.servers(Arrays.asList(localServer, prodServer));

	}
}
