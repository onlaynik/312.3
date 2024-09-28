package web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import web.config.handler.CustomAuthenticationFailureHandler;
import web.config.handler.CustomAuthenticationSuccessHandler;
import web.config.handler.CustomUrlLogoutSuccessHandler;
import web.service.UserService;

@EnableWebSecurity(debug = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserService userService;

    private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    private final CustomAuthenticationFailureHandler authenticationFailureHandler;

    private final CustomUrlLogoutSuccessHandler urlLogoutSuccessHandler;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(UserService userService,
                          CustomAuthenticationSuccessHandler authenticationSuccessHandler,
                          CustomAuthenticationFailureHandler authenticationFailureHandler,
                          CustomUrlLogoutSuccessHandler urlLogoutSuccessHandler,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.urlLogoutSuccessHandler = urlLogoutSuccessHandler;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests(authorize -> authorize
                        .antMatchers("/", "/css/*", "/js/*").permitAll()
                        .antMatchers("/admin/**").hasRole("ADMIN")
                        .antMatchers("/user/**").hasAnyRole("ADMIN", "USER")
                        .anyRequest().authenticated()
                );

        http.formLogin()
                .loginPage("/")
                .permitAll()
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler)
                .usernameParameter("email")
                .passwordParameter("password");

        http.logout()
                .permitAll()
                .logoutUrl("/logout")
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessUrl("/?logout")
                .logoutSuccessHandler(urlLogoutSuccessHandler);
    }
}