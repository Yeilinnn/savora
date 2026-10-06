package cr.ac.una.savora.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey llave;
    private final Duration vigencia;
    private final Clock clock;

    public JwtService(
            @Value("${savora.jwt.secret}") String secreto,
            @Value("${savora.jwt.vigencia-minutos:120}") long vigenciaMinutos,
            Clock clock) {
        this.llave = Keys.hmacShaKeyFor(secreto.getBytes());
        this.vigencia = Duration.ofMinutes(vigenciaMinutos);
        this.clock = clock;
    }

    public String generar(UsuarioPrincipal principal) {
        Instant ahora = clock.instant();
        return Jwts.builder()
                .subject(principal.getUsername())
                .claim("rol", principal.getRol().name())
                .claim("negocioId", principal.getNegocioId())
                .claim("clienteId", principal.getClienteId())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(vigencia)))
                .signWith(llave)
                .compact();
    }

    public Claims analizar(String token) {
        return Jwts.parser()
                .verifyWith(llave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long vigenciaSegundos() {
        return vigencia.toSeconds();
    }
}