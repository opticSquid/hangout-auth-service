package com.hangout.core.auth_api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.hangout.core.auth_api.entity.Roles;
import com.hangout.core.auth_api.filter.JwtFilter;
import com.hangout.core.auth_api.filter.UserAuthenticationFilter;
import com.hangout.core.auth_api.service.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
	@Autowired
	private UserDetailsServiceImpl userDetailsService;
	@Autowired
	private JwtFilter jwtFilter;
	@Autowired
	private UserAuthenticationFilter userAuthenticationFilter;
	@Autowired
	PasswordEncoder passwordEncoder;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// @formatter:off
		return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("v1/public/**").permitAll()
						.requestMatchers("api-docs/**","swagger-ui/**").permitAll()
                        .requestMatchers("v1/user/**").authenticated()
                        .requestMatchers("v1/admin/**").hasRole(Roles.ADMIN.name())
						// had to do this just to make actuator work
                        .anyRequest().permitAll())
				.addFilterBefore(jwtFilter, UserAuthenticationFilter.class)
                .addFilterBefore(userAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
		// @formatter:on
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration auth) throws Exception {
		return auth.getAuthenticationManager();
	}
}
