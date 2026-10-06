package cr.ac.una.savora.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        // Las dos reglas de abajo son las únicas que exigen autenticación en este
                        // laboratorio: crear y cerrar paquetes (lo demás -reservas, negocios, etc.-
                        // ya lo cubre Nazareth en su parte y queda abierto como estaba).
                        .requestMatchers(HttpMethod.POST, "/api/v1/paquetes").hasRole("NEGOCIO")
                        .requestMatchers(HttpMethod.POST, "/api/v1/paquetes/*/cierre").hasRole("NEGOCIO")
                        .anyRequest().permitAll())
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(this::noAutenticado)
                        .accessDeniedHandler(this::accesoDenegado))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void noAutenticado(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
            throws IOException {
        escribirProblema(response, HttpStatus.UNAUTHORIZED, "No autenticado", "Se requiere un token Bearer válido");
    }

    private void accesoDenegado(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException {
        escribirProblema(
                response, HttpStatus.FORBIDDEN, "Acceso denegado",
                "El usuario autenticado no tiene permiso para este recurso");
    }

    /**
     * Escribe el Problem Details (RFC 9457) a mano para no depender de la versión de Jackson
     * (Spring Boot 4 usa Jackson 3, y aquí el cuerpo es chico y de textos fijos).
     */
    private void escribirProblema(HttpServletResponse response, HttpStatus estado, String titulo, String detalle)
            throws IOException {
        String json = "{\"type\":\"about:blank\",\"title\":\"" + titulo + "\",\"status\":" + estado.value()
                + ",\"detail\":\"" + detalle + "\"}";
        response.setStatus(estado.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write(json);
    }
}