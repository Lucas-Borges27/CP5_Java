package br.com.fiap.produtosms.configs;

import br.com.fiap.produtosms.service.CustomOAuth2UserService;
import br.com.fiap.produtosms.service.LocalUserDetailsService;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final LocalUserDetailsService localUserDetailsService;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService, LocalUserDetailsService localUserDetailsService) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.localUserDetailsService = localUserDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                        .requestMatchers("/login", "/cadastro", "/oauth2/authorization/**").permitAll()
                        .requestMatchers("/produtos/novo").hasRole("PRODUTO")
                        .requestMatchers("/produtos/detalhe/**").hasRole("PRODUTO")
                        .requestMatchers("/produtos/salvar").hasRole("PRODUTO")
                        .requestMatchers("/produtos/excluir").hasRole("PRODUTO")
                        .requestMatchers("/produtos").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "produtos-ms-lang")
                )
                .exceptionHandling(ex -> ex.accessDeniedPage("/403"))
                .userDetailsService(localUserDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
