package com.goodmanltd.order.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Value("${auth0.audience}")
	private String expectedAudience;

	@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
	private String issuerUri;

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration cfg = new CorsConfiguration();
		// If you do NOT need cookies: allow all origins
		cfg.setAllowedOriginPatterns(List.of("*"));
		cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
		cfg.setAllowedHeaders(List.of("Authorization","Content-Type"));
		cfg.setAllowCredentials(false); // keep false when using "*"
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", cfg);
		return source;
	}


	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.cors(c -> {}) // enable CORS
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.GET, "/orders/health").permitAll()
				.requestMatchers(HttpMethod.GET,"/orders/**").authenticated()
				.requestMatchers(HttpMethod.GET,"/orders/my-orders").authenticated()
				.requestMatchers(HttpMethod.GET,"/orders/postId/*").authenticated()
				.requestMatchers(HttpMethod.POST, "/orders").authenticated()
				.requestMatchers(HttpMethod.PUT, "/orders/*/completed").authenticated()
				.requestMatchers(HttpMethod.PUT, "/orders/*/cancel").authenticated()
				.anyRequest().permitAll()
		)
				.oauth2ResourceServer(resourceServer ->
						resourceServer.jwt(jwt ->
								jwt.decoder(jwtDecoder())
						)
				);
		return http.build();
	}


	@Bean
	public JwtDecoder jwtDecoder() {
		NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);

		OAuth2TokenValidator<Jwt> audienceValidator = token -> {
			List<String> audiences = token.getAudience();
			if (audiences.contains(expectedAudience)) {
				return OAuth2TokenValidatorResult.success();
			} else {
				return OAuth2TokenValidatorResult.failure(
						new OAuth2Error("invalid_token", "Invalid audience", null)
				);
			}
		};

		OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
		OAuth2TokenValidator<Jwt> combined = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

		jwtDecoder.setJwtValidator(combined);
		return jwtDecoder;
	}

}
