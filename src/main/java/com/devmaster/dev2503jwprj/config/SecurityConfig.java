package com.devmaster.dev2503jwprj.config;

import com.devmaster.dev2503jwprj.service.CartService;
import com.devmaster.dev2503jwprj.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import com.devmaster.dev2503jwprj.entity.Customer;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
                .requestMatchers("/account/register", "/account/login", "/css/**", "/js/**", "/images/**", "/products/**", "/home", "/cart/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/account/login")
                .loginProcessingUrl("/login")
                .successHandler((request, response, authentication) -> {
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        logger.info("Session ID: {}", session.getId());
                        logger.info("Session Attributes:");
                        Collections.list(session.getAttributeNames())
                                .forEach(name -> logger.info("  {}: {}", name, session.getAttribute(name)));
                    } else {
                        logger.info("Không có session sau khi đăng nhập.");
                    }
                    // Đồng bộ giỏ hàng
                    cartService.syncCartFromSession(request.getSession(), authentication);
                    if (authentication.getAuthorities().stream()
                            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
                        response.sendRedirect("/admin/dashboard");
                    } else {
                        response.sendRedirect("/home");
                    }
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/account/logout")
                .logoutSuccessHandler(logoutSuccessHandler)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}