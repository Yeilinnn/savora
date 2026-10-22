package cr.ac.una.savora.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UsuarioDetailsService usuarioDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UsuarioDetailsService usuarioDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource)
            throws Exception {
        http
                .securityMatcher("/api/**", "/auth/**")
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/api/v1/reservas/**").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/api/v1/paquetes").hasRole("NEGOCIO")
                        .requestMatchers(HttpMethod.POST, "/api/v1/paquetes/*/cierre").hasRole("NEGOCIO")
                        .anyRequest().authenticated())
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(this::noAutenticadoApi)
                        .accessDeniedHandler(this::accesoDenegadoApi))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain documentacionSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(
                        "/actuator/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Order(3)
    SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/login", "/logout", "/admin/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/admin/**").hasRole("NEGOCIO")
                        .anyRequest().authenticated())
                .userDetailsService(usuarioDetailsService)
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/admin/categorias", true)
                        .permitAll())
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout").permitAll())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(eh -> eh.accessDeniedPage("/login?denegado"));
        return http.build();
    }

    private void noAutenticadoApi(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
            throws IOException {
        escribirProblemaJson(
                response, HttpStatus.UNAUTHORIZED, "No autenticado", "Se requiere un token Bearer válido");
    }

    private void accesoDenegadoApi(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException {
        escribirProblemaJson(
                response,
                HttpStatus.FORBIDDEN,
                "Acceso denegado",
                "El usuario autenticado no tiene permiso para este recurso");
    }

    private void escribirProblemaJson(HttpServletResponse response, HttpStatus estado, String titulo, String detalle)
            throws IOException {
        String json = "{\"type\":\"about:blank\",\"title\":\"" + titulo + "\",\"status\":" + estado.value()
                + ",\"detail\":\"" + detalle + "\"}";
        response.setStatus(estado.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write(json);
    }
}
