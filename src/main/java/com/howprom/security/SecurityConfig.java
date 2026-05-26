package com.howprom.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    	http
    	.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/main", "/problems", "/auth/**",
                                 "/css/**", "/js/**", "/images/**",
                                 "/error/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/profile/**").authenticated()
                .requestMatchers("/api/chat/**").authenticated()
                .anyRequest().authenticated()
            )
    		.csrf(csrf -> csrf.ignoringRequestMatchers(
    				"/profile/**",
    				"/api/**"
    		))
            .formLogin(form -> form
        	    .loginPage("/auth/login")
        	    .loginProcessingUrl("/login")
        	    .usernameParameter("email")
        	    .defaultSuccessUrl("/", true)
        	    .failureUrl("/auth/login?error")
        	    .permitAll()
        	)
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) -> {
                        // 비로그인 → 메인으로 (기존 정책 유지)
                        response.sendRedirect("/");
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        // 권한 부족 (USER가 /admin 접근 등) → 커스텀 403
                        response.sendRedirect("/error/403");
                    })
                )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}