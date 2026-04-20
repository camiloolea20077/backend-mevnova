package com.cloud_tecnological.mednova.security;

import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.pre-auth.expiration-seconds:300}")
    private long preAuthExpirationSeconds;

    @Value("${jwt.session.expiration-seconds:600}")
    private long sessionExpirationSeconds;

    @Value("${jwt.access.expiration-seconds:86400}")
    private long accessExpirationSeconds;

    @Value("${jwt.refresh.expiration-seconds:2592000}")
    private long refreshExpirationSeconds;

    @Value("${jwt.password-change.expiration-seconds:900}")
    private long passwordChangeExpirationSeconds;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public String generatePreAuthToken(Long empresaId) {
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .setSubject("pre_auth")
                .claim("tipo_token", "PRE_AUTH")
                .claim("empresa_id", empresaId)
                .setId(jti)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + preAuthExpirationSeconds * 1000))
                .signWith(getKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateSessionToken(Long usuarioId, Long empresaId, List<Long> sedesDisponibles) {
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .setSubject("session")
                .claim("tipo_token", "SESSION")
                .claim("empresa_id", empresaId)
                .claim("usuario_id", usuarioId)
                .claim("sedes_disponibles", sedesDisponibles)
                .setId(jti)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + sessionExpirationSeconds * 1000))
                .signWith(getKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateAccessToken(TenantInfo info) {
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .setSubject(String.valueOf(info.getUsuario_id()))
                .claim("tipo_token", "ACCESS")
                .claim("usuario_id", info.getUsuario_id())
                .claim("empresa_id", info.getEmpresa_id())
                .claim("sede_id", info.getSede_id())
                .claim("username", info.getUsername())
                .claim("roles", info.getRoles())
                .claim("permisos", info.getPermisos())
                .setId(jti)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpirationSeconds * 1000))
                .signWith(getKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(TenantInfo info) {
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .setSubject(String.valueOf(info.getUsuario_id()))
                .claim("tipo_token", "REFRESH")
                .claim("usuario_id", info.getUsuario_id())
                .claim("empresa_id", info.getEmpresa_id())
                .claim("sede_id", info.getSede_id())
                .setId(jti)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationSeconds * 1000))
                .signWith(getKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String generatePasswordChangeToken(Long usuarioId, Long empresaId) {
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .setSubject(String.valueOf(usuarioId))
                .claim("tipo_token", "PASSWORD_CHANGE")
                .claim("usuario_id", usuarioId)
                .claim("empresa_id", empresaId)
                .setId(jti)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + passwordChangeExpirationSeconds * 1000))
                .signWith(getKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public Claims validateAndParse(String token, String expectedType) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String tipo = claims.get("tipo_token", String.class);
        if (!expectedType.equals(tipo)) {
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Tipo de token incorrecto");
        }
        return claims;
    }

    public String extractJti(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getId();
    }
}
