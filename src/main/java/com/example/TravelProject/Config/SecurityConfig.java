package com.example.TravelProject.Config;

import com.example.TravelProject.OAuth.OAuth2SuccessHandler;
import com.example.TravelProject.Service.PrincipalOauth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final PrincipalOauth2UserService principalOauth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    public SecurityConfig(PrincipalOauth2UserService principalOauth2UserService, OAuth2SuccessHandler oAuth2SuccessHandler) {
        this.principalOauth2UserService = principalOauth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/app/login") // 혹은 안 넣으면 기본 /oauth2/authorization/google
                        .redirectionEndpoint(redir -> redir
                                .baseUri("/app/login/oauth2/code/*")
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(principalOauth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                );
        return http.build();

    }

}