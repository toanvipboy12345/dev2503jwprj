package com.devmaster.dev2503jwprj.config;

import com.devmaster.dev2503jwprj.service.CartService;
import com.devmaster.dev2503jwprj.service.CustomerService;
import com.devmaster.dev2503jwprj.entity.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.servlet.http.HttpSession;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CartService cartService;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Customer customer = customerService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));
            String role = customer.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER";
            return org.springframework.security.core.userdetails.User
                    .withUsername(customer.getUsername())
                    .password(customer.getPassword())
                    .roles(role.replace("ROLE_", ""))
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        SimpleUrlLogoutSuccessHandler logoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
        logoutSuccessHandler.setDefaultTargetUrl("/account/login?logout");
        logoutSuccessHandler.setAlwaysUseDefaultTargetUrl(true);

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/account/register", "/account/login", "/css/**", "/js/**", "/images/**", "/products/**", "/home", "/cart/**", "/orders/checkout").permitAll()
                .requestMatchers("/orders/submit", "/orders/success").authenticated()
                .anyRequest().authenticated()
            )
            .requestCache(cache -> cache
                .requestCache(new NullRequestCache()) // Xóa lưu trữ request cũ như /error?continue
            )
            .formLogin(form -> form
                .loginPage("/account/login")
                .loginProcessingUrl("/login")
                .successHandler((request, response, authentication) -> {
                    HttpSession session = request.getSession();
                    logger.info("Login successful for user: {}, Session ID: {}", authentication.getName(), session.getId());
                    logger.info("Session Attributes:");
                    Collections.list(session.getAttributeNames())
                            .forEach(name -> logger.info("  {}: {}", name, session.getAttribute(name)));
                    cartService.syncCartFromSession(session, authentication);
                    if (authentication.getAuthorities().stream()
                            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
                        response.sendRedirect("/admin/dashboard");
                    } else {
                        response.sendRedirect("/home");
                    }
                })
                .failureHandler((request, response, exception) -> {
                    logger.error("Login failed: {}", exception.getMessage());
                    response.sendRedirect("/account/login?error");
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/account/logout")
                .logoutSuccessHandler(logoutSuccessHandler)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/account/login")
                .defaultAuthenticationEntryPointFor((req, res, e) -> {
                    logger.error("Authentication failed: {}, Redirecting to login", e.getMessage());
                    if (!res.isCommitted()) {
                        res.sendRedirect("/account/login?error");
                    } else {
                        logger.warn("Response already committed, cannot redirect");
                    }
                }, new AntPathRequestMatcher("/**"))
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/orders/submit") // Tạm thời bỏ CSRF
            );

        return http.build();
    }
}