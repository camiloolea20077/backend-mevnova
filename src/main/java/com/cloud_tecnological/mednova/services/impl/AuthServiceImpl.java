package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.auth.*;
import com.cloud_tecnological.mednova.dto.auth.ChangePasswordRequestDto;
import com.cloud_tecnological.mednova.dto.auth.SuperAdminLoginRequestDto;
import com.cloud_tecnological.mednova.entity.*;
import com.cloud_tecnological.mednova.entity.HistorialPasswordEntity;
import com.cloud_tecnological.mednova.repositories.auth.*;
import com.cloud_tecnological.mednova.repositories.auth.HistorialPasswordJpaRepository;
import com.cloud_tecnological.mednova.repositories.auth.HistorialPasswordQueryRepository;
import com.cloud_tecnological.mednova.repositories.empresa.EmpresaQueryRepository;
import com.cloud_tecnological.mednova.repositories.sede.SedeQueryRepository;
import com.cloud_tecnological.mednova.repositories.usuario.UsuarioJpaRepository;
import com.cloud_tecnological.mednova.repositories.usuario.UsuarioQueryRepository;
import com.cloud_tecnological.mednova.security.IpRateLimiter;
import com.cloud_tecnological.mednova.security.JwtTokenProvider;
import com.cloud_tecnological.mednova.services.AuthService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantInfo;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String PASO_PRE_AUTH  = "PRE_AUTH";
    private static final String PASO_LOGIN     = "LOGIN";
    private static final String TIPO_PRE_AUTH  = "PRE_AUTH";
    private static final String TIPO_SESSION   = "SESSION";
    private static final String TIPO_ACCESS    = "ACCESS";
    private static final String TIPO_REFRESH   = "REFRESH";
    private static final int    RATE_LIMIT_MAX = 10;
    private static final int    RATE_LIMIT_WIN = 300;
    private static final int    MAX_FAILED     = 5;
    private static final String DUMMY_HASH     =
            "$2a$12$KIXQw1CmSbC6YSp9LLj2rOTzDlWkWXt1eVjHe3Xn3iHk2o6rFvRkO";

    @Value("${jwt.pre-auth.expiration-seconds:300}")   private long preAuthExp;
    @Value("${jwt.session.expiration-seconds:600}")    private long sessionExp;
    @Value("${jwt.access.expiration-seconds:86400}")   private long accessExp;
    @Value("${jwt.refresh.expiration-seconds:2592000}") private long refreshExp;

    private final EmpresaQueryRepository              empresaQueryRepository;
    private final UsuarioJpaRepository                usuarioJpaRepository;
    private final UsuarioQueryRepository              usuarioQueryRepository;
    private final SesionUsuarioJpaRepository          sesionJpaRepository;
    private final SesionUsuarioQueryRepository        sesionQueryRepository;
    private final IntentoAutenticacionJpaRepository   intentoJpaRepository;
    private final AuditoriaJpaRepository              auditoriaJpaRepository;
    private final SedeQueryRepository                 sedeQueryRepository;
    private final HistorialPasswordJpaRepository      historialPasswordJpaRepository;
    private final HistorialPasswordQueryRepository    historialPasswordQueryRepository;
    private final JwtTokenProvider                    jwtTokenProvider;
    private final PasswordEncoder                     passwordEncoder;
    private final IpRateLimiter                       ipRateLimiter;

    public AuthServiceImpl(
            EmpresaQueryRepository empresaQueryRepository,
            UsuarioJpaRepository usuarioJpaRepository,
            UsuarioQueryRepository usuarioQueryRepository,
            SesionUsuarioJpaRepository sesionJpaRepository,
            SesionUsuarioQueryRepository sesionQueryRepository,
            IntentoAutenticacionJpaRepository intentoJpaRepository,
            AuditoriaJpaRepository auditoriaJpaRepository,
            SedeQueryRepository sedeQueryRepository,
            HistorialPasswordJpaRepository historialPasswordJpaRepository,
            HistorialPasswordQueryRepository historialPasswordQueryRepository,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder,
            IpRateLimiter ipRateLimiter) {
        this.empresaQueryRepository          = empresaQueryRepository;
        this.usuarioJpaRepository            = usuarioJpaRepository;
        this.usuarioQueryRepository          = usuarioQueryRepository;
        this.sesionJpaRepository             = sesionJpaRepository;
        this.sesionQueryRepository           = sesionQueryRepository;
        this.intentoJpaRepository            = intentoJpaRepository;
        this.auditoriaJpaRepository          = auditoriaJpaRepository;
        this.sedeQueryRepository             = sedeQueryRepository;
        this.historialPasswordJpaRepository  = historialPasswordJpaRepository;
        this.historialPasswordQueryRepository = historialPasswordQueryRepository;
        this.jwtTokenProvider                = jwtTokenProvider;
        this.passwordEncoder                 = passwordEncoder;
        this.ipRateLimiter                   = ipRateLimiter;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HU-FASE1-001A: Pre-autenticación de empresa
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PreAuthResponseDto preAuth(PreAuthRequestDto request, String ip, String userAgent) {
        if ((request.getCompanyCodigo() == null || request.getCompanyCodigo().isBlank())
                && (request.getCompanyNit() == null || request.getCompanyNit().isBlank())) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "Debe proporcionar el código o NIT de la empresa");
        }

        if (ipRateLimiter.isBlocked(ip, RATE_LIMIT_MAX, RATE_LIMIT_WIN)) {
            registrarIntento(null, null, ip, userAgent, PASO_PRE_AUTH, false, "rate_limit_excedido");
            throw new GlobalException(HttpStatus.TOO_MANY_REQUESTS,
                    "Demasiados intentos. Intente de nuevo en unos minutos.");
        }

        Optional<EmpresaEntity> empresaOpt = resolveEmpresa(request);
        if (empresaOpt.isEmpty()) {
            registrarIntento(null, null, ip, userAgent, PASO_PRE_AUTH, false, "empresa_no_encontrada");
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Empresa no encontrada o inactiva");
        }

        EmpresaEntity empresa = empresaOpt.get();
        String token  = jwtTokenProvider.generatePreAuthToken(empresa.getId());
        Claims claims = jwtTokenProvider.validateAndParse(token, TIPO_PRE_AUTH);

        guardarSesion(claims.getId(), null, TIPO_PRE_AUTH, empresa.getId(), null, null, ip, preAuthExp);
        registrarIntento(empresa.getId(), null, ip, userAgent, PASO_PRE_AUTH, true, null);

        return PreAuthResponseDto.builder()
                .preAuthToken(token)
                .companyName(empresa.getNombre_comercial())
                .logoUrl(empresa.getLogo_url())
                .expiresInSeconds(preAuthExp)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HU-FASE1-001B: Login con credenciales
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LoginResponseDto login(String preAuthToken, LoginRequestDto request,
                                  String ip, String userAgent) {
        Claims claims;
        try {
            claims = jwtTokenProvider.validateAndParse(preAuthToken, TIPO_PRE_AUTH);
        } catch (Exception e) {
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Token de pre-autenticación inválido");
        }

        String jti      = claims.getId();
        Long   empresaId = claims.get("empresa_id", Long.class);

        SesionUsuarioEntity sesionPreAuth = sesionQueryRepository.findByJti(jti)
                .orElseThrow(() -> new GlobalException(HttpStatus.UNAUTHORIZED,
                        "Token de pre-autenticación inválido"));

        if (Boolean.TRUE.equals(sesionPreAuth.getUsado())) {
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Token de pre-autenticación ya utilizado");
        }

        Optional<UsuarioEntity> usuarioOpt = usuarioQueryRepository
                .findByUsernameAndEmpresa(request.getUsername(), empresaId);

        String  hashToCheck = usuarioOpt.map(UsuarioEntity::getHash_password).orElse(DUMMY_HASH);
        boolean passMatches = passwordEncoder.matches(request.getPassword(), hashToCheck);

        if (usuarioOpt.isEmpty()) {
            registrarIntento(empresaId, request.getUsername(), ip, userAgent, PASO_LOGIN, false, "usuario_no_encontrado");
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        UsuarioEntity usuario = usuarioOpt.get();

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            registrarIntento(empresaId, usuario.getNombre_usuario(), ip, userAgent, PASO_LOGIN, false, "usuario_inactivo");
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
        if (Boolean.TRUE.equals(usuario.getBloqueado())) {
            registrarIntento(empresaId, usuario.getNombre_usuario(), ip, userAgent, PASO_LOGIN, false, "usuario_bloqueado");
            throw new GlobalException(HttpStatus.UNAUTHORIZED,
                    "Usuario bloqueado. Contacte al administrador.");
        }

        if (!passMatches) {
            incrementarIntentosFallidos(usuario);
            registrarIntento(empresaId, usuario.getNombre_usuario(), ip, userAgent, PASO_LOGIN, false, "password_invalido");
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        resetearIntentosFallidos(usuario, ip);
        marcarSesionUsada(sesionPreAuth);
        registrarIntento(empresaId, usuario.getNombre_usuario(), ip, userAgent, PASO_LOGIN, true, null);

        if (Boolean.TRUE.equals(usuario.getRequiere_cambio_password())) {
            String pwdToken = jwtTokenProvider.generatePasswordChangeToken(usuario.getId(), empresaId);
            return LoginResponseDto.builder()
                    .requirePasswordChange(true)
                    .passwordChangeToken(pwdToken)
                    .build();
        }

        List<SedeDto> sedes = sedeQueryRepository.findAvailableByUserAndEmpresa(
                usuario.getId(), empresaId);

        if (sedes.isEmpty()) {
            throw new GlobalException(HttpStatus.FORBIDDEN, "Usuario sin sedes asignadas");
        }

        if (sedes.size() == 1) {
            AuthTokensDto tokens = emitirTokensFinal(usuario, empresaId, sedes.get(0).getId(), ip);
            return LoginResponseDto.builder()
                    .accessToken(tokens.getAccessToken())
                    .refreshToken(tokens.getRefreshToken())
                    .tokenType(tokens.getTokenType())
                    .user(tokens.getUser())
                    .requiresSedeSelection(false)
                    .build();
        }

        List<Long> sedeIds = sedes.stream().map(SedeDto::getId).toList();
        String sessionToken  = jwtTokenProvider.generateSessionToken(usuario.getId(), empresaId, sedeIds);
        Claims sessionClaims = jwtTokenProvider.validateAndParse(sessionToken, TIPO_SESSION);
        guardarSesion(sessionClaims.getId(), null, TIPO_SESSION,
                empresaId, usuario.getId(), null, ip, sessionExp);

        return LoginResponseDto.builder()
                .sessionToken(sessionToken)
                .requiresSedeSelection(true)
                .availableSedes(sedes)
                .expiresInSeconds(sessionExp)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HU-FASE1-001C: Selección de sede y emisión del JWT final
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthTokensDto selectSede(String sessionToken, SelectSedeRequestDto request,
                                    String ip, String userAgent) {
        Claims claims;
        try {
            claims = jwtTokenProvider.validateAndParse(sessionToken, TIPO_SESSION);
        } catch (Exception e) {
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Session token inválido");
        }

        String jti      = claims.getId();
        Long   empresaId = claims.get("empresa_id", Long.class);
        Long   usuarioId = claims.get("usuario_id", Long.class);

        SesionUsuarioEntity sesionSession = sesionQueryRepository.findByJti(jti)
                .orElseThrow(() -> new GlobalException(HttpStatus.UNAUTHORIZED, "Session token inválido"));

        if (Boolean.TRUE.equals(sesionSession.getUsado())) {
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Session token ya utilizado");
        }

        List<?> sedesRaw = claims.get("sedes_disponibles", List.class);
        List<Long> sedesDisponibles = sedesRaw.stream()
                .map(v -> ((Number) v).longValue())
                .toList();

        Long sedeId = request.getSedeId();
        if (!sedesDisponibles.contains(sedeId)) {
            throw new GlobalException(HttpStatus.FORBIDDEN,
                    "La sede seleccionada no está entre sus sedes disponibles");
        }

        if (!sedeQueryRepository.existsActiveByIdAndEmpresa(sedeId, empresaId)) {
            throw new GlobalException(HttpStatus.FORBIDDEN, "Sede no disponible");
        }

        UsuarioEntity usuario = usuarioJpaRepository.findById(usuarioId)
                .orElseThrow(() -> new GlobalException(HttpStatus.UNAUTHORIZED,
                        "Usuario no encontrado"));

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        marcarSesionUsada(sesionSession);

        AuthTokensDto tokens = emitirTokensFinal(usuario, empresaId, sedeId, ip);
        registrarAuditoria(empresaId, sedeId, usuarioId, "LOGIN", ip, userAgent);

        return tokens;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HU-FASE1-001E: Logout y revocación de tokens
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void logout(String accessToken, String ip, String userAgent) {
        // El token ya fue validado por JwtAuthenticationFilter antes de llegar aquí
        Claims claims;
        try {
            claims = jwtTokenProvider.validateAndParse(accessToken, TIPO_ACCESS);
        } catch (Exception e) {
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Token inválido");
        }

        String jti      = claims.getId();
        Long   empresaId = claims.get("empresa_id", Long.class);
        Long   sedeId    = claims.get("sede_id", Long.class);
        Long   usuarioId = claims.get("usuario_id", Long.class);

        // 1. Revocar access token
        SesionUsuarioEntity sesionAccess = sesionQueryRepository.findByJti(jti)
                .orElseThrow(() -> new GlobalException(HttpStatus.UNAUTHORIZED, "Sesión no encontrada"));
        revocarSesion(sesionAccess);

        // 2. Revocar refresh token vinculado (parent_jti = jti del access)
        sesionQueryRepository.findByParentJti(jti).ifPresent(this::revocarSesion);

        // 3. Auditoría
        registrarAuditoria(empresaId, sedeId, usuarioId, "LOGOUT", ip, userAgent);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HU-FASE1-001D: Refresh del access token (con rotación)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthTokensDto refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtTokenProvider.validateAndParse(refreshToken, TIPO_REFRESH);
        } catch (Exception e) {
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Refresh token inválido o expirado");
        }

        String jti       = claims.getId();
        Long   usuarioId = claims.get("usuario_id", Long.class);
        Long   empresaId = claims.get("empresa_id", Long.class);
        Long   sedeId    = claims.get("sede_id",    Long.class);

        SesionUsuarioEntity sesion = sesionQueryRepository.findByJti(jti)
                .orElseThrow(() -> new GlobalException(HttpStatus.UNAUTHORIZED, "Sesión no encontrada"));

        if (sesion.getFecha_revocacion() != null) {
            // Posible robo de token — revocar todas las sesiones del usuario
            sesionQueryRepository.revocarTodasLasSesiones(usuarioId);
            throw new GlobalException(HttpStatus.UNAUTHORIZED,
                    "Refresh token revocado. Todas las sesiones fueron cerradas por seguridad.");
        }

        UsuarioEntity usuario = usuarioJpaRepository.findById(usuarioId)
                .orElseThrow(() -> new GlobalException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        if (Boolean.FALSE.equals(usuario.getActivo()) || Boolean.TRUE.equals(usuario.getBloqueado())) {
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        // Rotar: revocar refresh anterior y su access vinculado
        revocarSesion(sesion);
        if (sesion.getParent_jti() != null) {
            sesionQueryRepository.findByJti(sesion.getParent_jti()).ifPresent(this::revocarSesion);
        }

        // Recalcular roles y permisos
        List<String> roles, permisos;
        if (empresaId != null && sedeId != null) {
            roles    = usuarioQueryRepository.findRolesByUserEmpresaSede(usuarioId, empresaId, sedeId);
            permisos = usuarioQueryRepository.findPermissionsByUserEmpresaSede(usuarioId, empresaId, sedeId);
        } else {
            roles    = List.of("SUPER_ADMIN");
            permisos = List.of();
        }

        TenantInfo tenantInfo = TenantInfo.builder()
                .usuario_id(usuarioId)
                .empresa_id(empresaId)
                .sede_id(sedeId)
                .username(usuario.getNombre_usuario())
                .roles(roles)
                .permisos(permisos)
                .build();

        String newAccess  = jwtTokenProvider.generateAccessToken(tenantInfo);
        String newRefresh = jwtTokenProvider.generateRefreshToken(tenantInfo);

        Claims newAccessClaims  = jwtTokenProvider.validateAndParse(newAccess,  TIPO_ACCESS);
        Claims newRefreshClaims = jwtTokenProvider.validateAndParse(newRefresh, TIPO_REFRESH);

        String newAccessJti  = newAccessClaims.getId();
        String newRefreshJti = newRefreshClaims.getId();

        guardarSesion(newAccessJti,  null,        TIPO_ACCESS,  empresaId, usuarioId, sedeId, sesion.getIp(), accessExp);
        guardarSesion(newRefreshJti, newAccessJti, TIPO_REFRESH, empresaId, usuarioId, sedeId, sesion.getIp(), refreshExp);

        UserInfoDto userInfo = UserInfoDto.builder()
                .id(usuarioId)
                .username(usuario.getNombre_usuario())
                .companyId(empresaId)
                .branchId(sedeId)
                .fullName(null)
                .roles(roles)
                .build();

        return AuthTokensDto.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .tokenType("Bearer")
                .expiresInSeconds(accessExp)
                .user(userInfo)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HU-FASE1-001F: Cambio de contraseña (primer login y voluntario)
    // ─────────────────────────────────────────────────────────────────────────

    private static final String TIPO_PWD_CHANGE = "PASSWORD_CHANGE";

    @Override
    @Transactional
    public void changePassword(String token, ChangePasswordRequestDto request,
                                String ip, String userAgent) {
        // Determinar tipo de token
        Claims claims;
        boolean esPrimerLogin;
        try {
            try {
                claims = jwtTokenProvider.validateAndParse(token, TIPO_ACCESS);
                esPrimerLogin = false;
            } catch (Exception e) {
                claims = jwtTokenProvider.validateAndParse(token, TIPO_PWD_CHANGE);
                esPrimerLogin = true;
            }
        } catch (Exception e) {
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Token inválido");
        }

        Long usuarioId = claims.get("usuario_id", Long.class);
        Long empresaId = claims.get("empresa_id", Long.class);

        // Para ACCESS token, verificar no revocado
        if (!esPrimerLogin && sesionQueryRepository.isRevoked(claims.getId())) {
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Token revocado");
        }

        UsuarioEntity usuario = usuarioJpaRepository.findById(usuarioId)
                .orElseThrow(() -> new GlobalException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        // Cambio voluntario: validar contraseña actual
        if (!esPrimerLogin) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new GlobalException(HttpStatus.BAD_REQUEST, "La contraseña actual es requerida");
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), usuario.getHash_password())) {
                throw new GlobalException(HttpStatus.BAD_REQUEST, "Contraseña actual incorrecta");
            }
        }

        // Validar política de contraseña
        validarPoliticaPassword(request.getNewPassword());

        // No puede ser igual a las últimas 3
        List<String> historial = historialPasswordQueryRepository.findRecentHashes(usuarioId, 3);
        if (passwordEncoder.matches(request.getNewPassword(), usuario.getHash_password())) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "La nueva contraseña no puede ser igual a la contraseña actual");
        }
        for (String oldHash : historial) {
            if (passwordEncoder.matches(request.getNewPassword(), oldHash)) {
                throw new GlobalException(HttpStatus.BAD_REQUEST,
                        "La nueva contraseña no puede ser igual a una de las últimas 3 contraseñas usadas");
            }
        }

        // Guardar hash actual en historial
        HistorialPasswordEntity entrada = new HistorialPasswordEntity();
        entrada.setUsuario_id(usuarioId);
        entrada.setHash_password(usuario.getHash_password());
        historialPasswordJpaRepository.save(entrada);

        // Actualizar contraseña
        usuario.setHash_password(passwordEncoder.encode(request.getNewPassword()));
        usuario.setRequiere_cambio_password(false);
        usuarioJpaRepository.save(usuario);

        // Revocar todas las sesiones activas
        sesionQueryRepository.revocarTodasLasSesiones(usuarioId);

        registrarAuditoria(empresaId, null, usuarioId, "UPDATE", ip, userAgent);
    }

    private void validarPoliticaPassword(String password) {
        if (password == null || password.length() < 8)
            throw new GlobalException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 8 caracteres");
        if (!password.matches(".*[A-Z].*"))
            throw new GlobalException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos una mayúscula");
        if (!password.matches(".*[a-z].*"))
            throw new GlobalException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos una minúscula");
        if (!password.matches(".*\\d.*"))
            throw new GlobalException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos un número");
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*"))
            throw new GlobalException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos un carácter especial");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HU-SUPER-ADMIN: Login directo sin pre-auth ni selección de sede
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthTokensDto superAdminLogin(SuperAdminLoginRequestDto request,
                                         String ip, String userAgent) {
        if (ipRateLimiter.isBlocked(ip, RATE_LIMIT_MAX, RATE_LIMIT_WIN)) {
            registrarIntento(null, request.getUsername(), ip, userAgent, PASO_LOGIN, false, "rate_limit_excedido");
            throw new GlobalException(HttpStatus.TOO_MANY_REQUESTS,
                    "Demasiados intentos. Intente de nuevo en unos minutos.");
        }

        Optional<UsuarioEntity> usuarioOpt = usuarioQueryRepository
                .findSuperAdminByUsername(request.getUsername());

        String hashToCheck = usuarioOpt.map(UsuarioEntity::getHash_password).orElse(DUMMY_HASH);
        boolean passMatches = passwordEncoder.matches(request.getPassword(), hashToCheck);

        if (usuarioOpt.isEmpty()) {
            registrarIntento(null, request.getUsername(), ip, userAgent, PASO_LOGIN, false, "usuario_no_encontrado");
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        UsuarioEntity usuario = usuarioOpt.get();

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            registrarIntento(null, usuario.getNombre_usuario(), ip, userAgent, PASO_LOGIN, false, "usuario_inactivo");
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
        if (Boolean.TRUE.equals(usuario.getBloqueado())) {
            registrarIntento(null, usuario.getNombre_usuario(), ip, userAgent, PASO_LOGIN, false, "usuario_bloqueado");
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Usuario bloqueado. Contacte al administrador.");
        }
        if (!passMatches) {
            incrementarIntentosFallidos(usuario);
            registrarIntento(null, usuario.getNombre_usuario(), ip, userAgent, PASO_LOGIN, false, "password_invalido");
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        resetearIntentosFallidos(usuario, ip);
        registrarIntento(null, usuario.getNombre_usuario(), ip, userAgent, PASO_LOGIN, true, null);

        TenantInfo tenantInfo = TenantInfo.builder()
                .usuario_id(usuario.getId())
                .empresa_id(null)
                .sede_id(null)
                .username(usuario.getNombre_usuario())
                .roles(List.of("SUPER_ADMIN"))
                .permisos(List.of())
                .build();

        String accessToken  = jwtTokenProvider.generateAccessToken(tenantInfo);
        String refreshToken = jwtTokenProvider.generateRefreshToken(tenantInfo);

        Claims accessClaims  = jwtTokenProvider.validateAndParse(accessToken, TIPO_ACCESS);
        Claims refreshClaims = jwtTokenProvider.validateAndParse(refreshToken, TIPO_REFRESH);

        String accessJti  = accessClaims.getId();
        String refreshJti = refreshClaims.getId();

        guardarSesion(accessJti,  null,      TIPO_ACCESS,  null, usuario.getId(), null, ip, accessExp);
        guardarSesion(refreshJti, accessJti, TIPO_REFRESH, null, usuario.getId(), null, ip, refreshExp);

        registrarAuditoria(null, null, usuario.getId(), "LOGIN", ip, userAgent);

        UserInfoDto userInfo = UserInfoDto.builder()
                .id(usuario.getId())
                .username(usuario.getNombre_usuario())
                .companyId(null)
                .branchId(null)
                .fullName(null)
                .roles(List.of("SUPER_ADMIN"))
                .build();

        return AuthTokensDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInSeconds(accessExp)
                .user(userInfo)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers privados
    // ─────────────────────────────────────────────────────────────────────────

    private AuthTokensDto emitirTokensFinal(UsuarioEntity usuario, Long empresaId,
                                            Long sedeId, String ip) {
        List<String> roles    = usuarioQueryRepository
                .findRolesByUserEmpresaSede(usuario.getId(), empresaId, sedeId);
        List<String> permisos = usuarioQueryRepository
                .findPermissionsByUserEmpresaSede(usuario.getId(), empresaId, sedeId);

        TenantInfo tenantInfo = TenantInfo.builder()
                .usuario_id(usuario.getId())
                .empresa_id(empresaId)
                .sede_id(sedeId)
                .username(usuario.getNombre_usuario())
                .roles(roles)
                .permisos(permisos)
                .build();

        String accessToken  = jwtTokenProvider.generateAccessToken(tenantInfo);
        String refreshToken = jwtTokenProvider.generateRefreshToken(tenantInfo);

        Claims accessClaims  = jwtTokenProvider.validateAndParse(accessToken, TIPO_ACCESS);
        Claims refreshClaims = jwtTokenProvider.validateAndParse(refreshToken, TIPO_REFRESH);

        String accessJti  = accessClaims.getId();
        String refreshJti = refreshClaims.getId();

        guardarSesion(accessJti, null,      TIPO_ACCESS,  empresaId, usuario.getId(), sedeId, ip, accessExp);
        // parent_jti del refresh apunta al access → permite revocar ambos en logout
        guardarSesion(refreshJti, accessJti, TIPO_REFRESH, empresaId, usuario.getId(), sedeId, ip, refreshExp);

        UserInfoDto userInfo = UserInfoDto.builder()
                .id(usuario.getId())
                .username(usuario.getNombre_usuario())
                .companyId(empresaId)
                .branchId(sedeId)
                .fullName(null)
                .roles(roles)
                .build();

        return AuthTokensDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInSeconds(accessExp)
                .user(userInfo)
                .build();
    }

    private void guardarSesion(String jti, String parentJti, String tipo, Long empresaId,
                                Long usuarioId, Long sedeId, String ip, long expSeconds) {
        SesionUsuarioEntity sesion = new SesionUsuarioEntity();
        sesion.setJti(jti);
        sesion.setParent_jti(parentJti);
        sesion.setTipo_token(tipo);
        sesion.setEmpresa_id(empresaId);
        sesion.setUsuario_id(usuarioId);
        sesion.setSede_id(sedeId);
        sesion.setUsado(false);
        sesion.setIp(ip);
        sesion.setFecha_expiracion(LocalDateTime.now().plusSeconds(expSeconds));
        sesionJpaRepository.save(sesion);
    }

    private void marcarSesionUsada(SesionUsuarioEntity sesion) {
        SesionUsuarioEntity entity = sesionJpaRepository.findById(sesion.getId())
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error interno de sesión"));
        entity.setUsado(true);
        entity.setFecha_uso(LocalDateTime.now());
        sesionJpaRepository.save(entity);
    }

    private void revocarSesion(SesionUsuarioEntity sesion) {
        SesionUsuarioEntity entity = sesionJpaRepository.findById(sesion.getId())
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error interno de sesión"));
        entity.setFecha_revocacion(LocalDateTime.now());
        sesionJpaRepository.save(entity);
    }

    private void incrementarIntentosFallidos(UsuarioEntity leido) {
        UsuarioEntity usuario = usuarioJpaRepository.findById(leido.getId())
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno"));
        int intentos = usuario.getIntentos_fallidos() + 1;
        usuario.setIntentos_fallidos(intentos);
        if (intentos >= MAX_FAILED) {
            usuario.setBloqueado(true);
            usuario.setFecha_bloqueo(LocalDateTime.now());
            usuario.setMotivo_bloqueo("intentos_excedidos");
        }
        usuarioJpaRepository.save(usuario);
    }

    private void resetearIntentosFallidos(UsuarioEntity leido, String ip) {
        UsuarioEntity usuario = usuarioJpaRepository.findById(leido.getId())
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno"));
        usuario.setIntentos_fallidos(0);
        usuario.setFecha_ultimo_ingreso(LocalDateTime.now());
        usuario.setIp_ultimo_ingreso(ip);
        usuarioJpaRepository.save(usuario);
    }

    private void registrarIntento(Long empresaId, String nombreUsuario, String ip, String userAgent,
                                   String paso, boolean exitoso, String motivoFallo) {
        IntentoAutenticacionEntity intento = new IntentoAutenticacionEntity();
        intento.setEmpresa_id(empresaId);
        intento.setNombre_usuario(nombreUsuario);
        intento.setPaso(paso);
        intento.setIp_origen(ip);
        intento.setUser_agent(userAgent);
        intento.setExitoso(exitoso);
        intento.setMotivo_fallo(motivoFallo);
        intentoJpaRepository.save(intento);
    }

    private void registrarAuditoria(Long empresaId, Long sedeId, Long usuarioId,
                                     String accion, String ip, String userAgent) {
        AuditoriaEntity auditoria = new AuditoriaEntity();
        auditoria.setEmpresa_id(empresaId);
        auditoria.setSede_id(sedeId);
        auditoria.setUsuario_id(usuarioId);
        auditoria.setTabla_afectada("sesion_usuario");
        auditoria.setAccion(accion);
        auditoria.setIp_origen(ip);
        auditoria.setUser_agent(userAgent);
        auditoriaJpaRepository.save(auditoria);
    }

    private Optional<EmpresaEntity> resolveEmpresa(PreAuthRequestDto request) {
        if (request.getCompanyCodigo() != null && !request.getCompanyCodigo().isBlank()) {
            return empresaQueryRepository.findActiveByCodigo(
                    request.getCompanyCodigo().trim().toUpperCase());
        }
        return empresaQueryRepository.findActiveByNit(request.getCompanyNit().trim());
    }

    @Override
    @Transactional
    public AuthTokensDto switchTenant(SwitchTenantRequestDto request, String ip, String userAgent) {
        Long usuarioId = TenantContext.getUsuarioId();

        if (Boolean.FALSE.equals(TenantContext.hasRole("SUPER_ADMIN"))) {
            throw new GlobalException(HttpStatus.FORBIDDEN, "Solo super-admin puede cambiar de tenant");
        }

        EmpresaEntity empresa = empresaQueryRepository.findActiveById(request.getEmpresaId())
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Empresa no encontrada"));

        Long sedeId = null;
        if (request.getSedeId() != null) {
            if (!sedeQueryRepository.existsActiveByIdAndEmpresa(request.getSedeId(), empresa.getId())) {
                throw new GlobalException(HttpStatus.BAD_REQUEST, "Sede no encontrada");
            }
            sedeId = request.getSedeId();
        }

        TenantInfo tenantInfo = TenantInfo.builder()
                .usuario_id(usuarioId)
                .empresa_id(empresa.getId())
                .sede_id(sedeId)
                .username(TenantContext.get().getUsername())
                .roles(List.of("SUPER_ADMIN"))
                .permisos(List.of())
                .build();

        String accessToken  = jwtTokenProvider.generateAccessToken(tenantInfo);
        String refreshToken = jwtTokenProvider.generateRefreshToken(tenantInfo);

        Claims accessClaims  = jwtTokenProvider.validateAndParse(accessToken, TIPO_ACCESS);
        Claims refreshClaims = jwtTokenProvider.validateAndParse(refreshToken, TIPO_REFRESH);

        String accessJti  = accessClaims.getId();
        String refreshJti = refreshClaims.getId();

        long accessExp  = jwtTokenProvider.getAccessExpiration();
        long refreshExp = jwtTokenProvider.getRefreshExpiration();

        guardarSesion(accessJti,  null,      TIPO_ACCESS,  empresa.getId(), usuarioId, sedeId, ip, accessExp);
        guardarSesion(refreshJti, accessJti, TIPO_REFRESH, empresa.getId(), usuarioId, sedeId, ip, refreshExp);

        registrarAuditoria(empresa.getId(), sedeId, usuarioId, "SWITCH_TENANT", ip, userAgent);

        UserInfoDto userInfo = UserInfoDto.builder()
                .id(usuarioId)
                .username(TenantContext.get().getUsername())
                .roles(List.of("SUPER_ADMIN"))
                .empresaId(empresa.getId())
                .empresaNombre(empresa.getRazon_social())
                .sedeId(sedeId)
                .build();

        return AuthTokensDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInSeconds(accessExp)
                .user(userInfo)
                .build();
    }
}
