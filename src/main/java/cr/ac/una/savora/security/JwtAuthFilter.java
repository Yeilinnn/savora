package cr.ac.una.savora.security;

import cr.ac.una.savora.data.RolUsuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String encabezado = request.getHeader("Authorization");
        if (encabezado != null && encabezado.startsWith("Bearer ")) {
            String token = encabezado.substring(7);
            try {
                Claims claims = jwtService.analizar(token);
                Number negocioIdRaw = claims.get("negocioId", Number.class);
                Number clienteIdRaw = claims.get("clienteId", Number.class);

                UsuarioPrincipal principal = new UsuarioPrincipal(
                        null,
                        claims.getSubject(),
                        "",
                        RolUsuario.valueOf(claims.get("rol", String.class)),
                        negocioIdRaw != null ? negocioIdRaw.longValue() : null,
                        clienteIdRaw != null ? clienteIdRaw.longValue() : null);

                var autenticacion =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            } catch (JwtException | IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}