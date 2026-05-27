package com.howprom.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;
import com.howprom.user.service.UserService;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
	
	private final UserService userService;

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
                .requestMatchers("/api/code/**").authenticated()
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
            .rememberMe(remember -> remember
        	    .key("howPromRememberKey")          // 토큰 서명용 비밀 키 (임의의 문자열)
        	    .tokenValiditySeconds(60 * 60 * 24 * 14)   // 14일 = 1,209,600초
        	    .rememberMeParameter("remember-me")  // login.html의 체크박스 name과 일치
        	    .userDetailsService(userService)     // UserDetailsService 명시
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
}