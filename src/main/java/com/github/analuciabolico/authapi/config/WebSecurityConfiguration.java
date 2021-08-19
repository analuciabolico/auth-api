package com.github.analuciabolico.authapi.config;

import java.util.Collections;
import java.util.List;

import com.github.analuciabolico.authapi.security.JwtAuthenticationEntryPoint;
import com.github.analuciabolico.authapi.security.JwtRequestFilter;
import com.github.analuciabolico.authapi.service.JwtUserDetailService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.AllArgsConstructor;

@Configuration(value = "AuthWebSecurityConfiguration")
@EnableWebSecurity
@AllArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final SwaggerProperties swaggerProperties;
    private final JwtUserDetailService jwtUserDetailService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;

    @Override
    protected void configure(final AuthenticationManagerBuilder builder) throws Exception {
        builder.userDetailsService(this.jwtUserDetailService).passwordEncoder(this.passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests().antMatchers("/", "/api/", "/api/login", "/api/home", "/h2-console/**")
                .permitAll().anyRequest().authenticated();

        http.cors().configurationSource(this.corsConfigurationSource());
        http.exceptionHandling().authenticationEntryPoint(this.jwtAuthenticationEntryPoint);
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER);
        http.addFilterBefore(this.jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        http.headers().frameOptions().sameOrigin();
    }

    @Override
    public void configure(final WebSecurity web) throws Exception {
        if (swaggerProperties.enabled) {
            web.ignoring().antMatchers("/v2/api-docs", "/v3/api-docs", "/swagger-resources/**", "/swagger-ui/**",
                    "/swagger-resources/configuration/ui", "/swagger-resources",
                    "/swagger-resources/configuration/security", "/swagger-ui.html", "/webjars/**");
        }
    }

    private CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        configuration.setAllowedOrigins(Collections.singletonList(CorsConfiguration.ALL));
        configuration.setAllowedMethods(List.of(HttpMethod.GET.name(), HttpMethod.PUT.name(), HttpMethod.PATCH.name(),
                HttpMethod.POST.name(), HttpMethod.DELETE.name(), HttpMethod.HEAD.name(), HttpMethod.OPTIONS.name()));

        source.registerCorsConfiguration("/**", configuration.applyPermitDefaultValues());

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
