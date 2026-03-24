package org.prm.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        			  .authorizeHttpRequests(auth-> auth
        					.requestMatchers("/home","/signup","/authenticate",
                                    "/refresh","/logout","/swagger-ui/**",
                                    "/error",
                                    "/v3/api-docs/**").permitAll()
        					.anyRequest().authenticated())
                      .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                      // REST API: no HTML login page redirects, no default logout endpoint
                      .formLogin(AbstractHttpConfigurer::disable)
                      .httpBasic(AbstractHttpConfigurer::disable)
                      .logout(AbstractHttpConfigurer::disable)
                      .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    // InMemoryUserDetailsService
//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails user1 = User
//                .withUsername("priti")
//                .password("{noop}123456")
//                .roles("USER")
//                .build();
//        UserDetails user2 = User
//                .withUsername("simun")
//                .password("{noop}123456")
//                .roles("USER")
//                .build();
//        UserDetails user3 = User
//                .withUsername("simun1")
//                .password("{noop}123456")
//                .roles("USER")
//                .build();
//        return new InMemoryUserDetailsManager(user1,user2,user3);
//    }
    
    // Database Authentication

    @Bean
    AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(getEncoder());
//        authProvider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
        return new ProviderManager(authProvider);
    }


    @Bean
    BCryptPasswordEncoder getEncoder() {
    		return new BCryptPasswordEncoder();
    }
//    @Bean
//    AuthenticationManager authenticationManager(
//            AuthenticationConfiguration configuration) throws Exception {
//        return configuration.getAuthenticationManager();
//    }
//    @Bean
//    AuthenticationManager authenticationManager() {
//        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//        authProvider.setUserDetailsService(getCustomUserDetailsService());
//        authProvider.setPasswordEncoder(getEncoder());
//        authProvider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
//        return authProvider;
//    }

}
