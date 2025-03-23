package com.gamzabat.algohub.config;

import java.util.Collections;
import java.util.List;

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

	@Value("${spring.profiles.active:dev}") // 기본값을 local로 설정
	private String activeProfile;

	@Bean
	public OpenAPI openAPI() {
		SecurityScheme scheme = new SecurityScheme()
			.type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
			.in(SecurityScheme.In.HEADER).name("Authorization");
		SecurityRequirement requirement = new SecurityRequirement().addList("bearerAuth");

		List<Server> servers = createServers();

		return new OpenAPI()
			.components(new Components().addSecuritySchemes("bearerAuth", scheme))
			.security(Collections.singletonList(requirement))
			.info(new Info()
				.title("AlgoHub API 명세서")
				.description("AlgoHub API 명세서 입니다.")
				.version("1.0.0"))
			.servers(servers);

	}

	private List<Server> createServers() {
		Server prodServer = new Server()
			.description("Algohub API")
			.url(ApiConstants.SERVER_HTTPS_ENDPOINT);

		if ("prod".equalsIgnoreCase(activeProfile)) {
			return Collections.singletonList(prodServer);
		}

		Server rcServer = new Server()
			.description("Algohub RC API")
			.url(ApiConstants.RC_SERVER_HTTPS_ENDPOINT);

		if ("rc".equalsIgnoreCase(activeProfile)) {
			return Collections.singletonList(rcServer);
		}

		Server localServer = new Server()
			.description("Algohub API for LOCAL")
			.url("http://localhost:" + serverPort);

		return List.of(localServer, prodServer);
	}
}
