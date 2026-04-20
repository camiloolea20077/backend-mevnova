package com.cloud_tecnological.mednova.security;

import com.cloud_tecnological.mednova.repositories.auth.SesionUsuarioQueryRepository;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import com.cloud_tecnological.mednova.util.TenantInfo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final SesionUsuarioQueryRepository sesionQueryRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   SesionUsuarioQueryRepository sesionQueryRepository) {
        this.jwtTokenProvider    = jwtTokenProvider;
        this.sesionQueryRepository = sesionQueryRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {
        try {
            String token = extractBearerToken(request);
            if (token != null) {
                Claims claims = jwtTokenProvider.validateAndParse(token, "ACCESS");
                String jti    = claims.getId();

                // Verificar que el token no esté revocado en DB
                if (sesionQueryRepository.isRevoked(jti)) {
                    throw new GlobalException(HttpStatus.UNAUTHORIZED, "Token revocado");
                }

                TenantInfo info = TenantInfo.builder()
                        .usuario_id(claims.get("usuario_id", Long.class))
                        .empresa_id(claims.get("empresa_id", Long.class))
                        .sede_id(claims.get("sede_id", Long.class))
                        .username(claims.get("username", String.class))
                        .roles(claims.get("roles", List.class))
                        .permisos(claims.get("permisos", List.class))
                        .jti(jti)
                        .build();

                TenantContext.set(info);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                info.getUsername(), null,
                                info.getRoles().stream()
                                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                                        .collect(Collectors.toList())
                        );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
