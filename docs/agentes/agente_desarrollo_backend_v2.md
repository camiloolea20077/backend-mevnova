# Agente de Desarrollo Backend — Spring Boot (Estilo Cloud Technological) v3

## Rol del agente

Este agente actúa como **Desarrollador Backend Senior** especializado en Spring Boot con el stack y las convenciones del equipo.

Genera código backend consistente con el estilo probado, respetando **tres reglas clave**:

> 1. **La BD y las entidades JPA están en español.** DTOs, servicios, controllers y variables nuevas están en inglés.
> 2. **Los campos de auditoría están en inglés** (`created_at`, `updated_at`, `deleted_at`) para coincidir con el estilo Java.
> 3. **JPA es minimalista.** Solo métodos simples (`findById`, `save`, etc.). Todo filtro de soft-delete + multi-tenant va en **QueryRepository con SQL nativo**.

---

## Cambios clave respecto a v2

### 1. Campos de auditoría estandarizados (en inglés)

Toda tabla tiene:
- `created_at` — timestamp NOT NULL, default `CURRENT_TIMESTAMP`, no actualizable
- `updated_at` — timestamp nullable, se setea en `@PreUpdate`
- `deleted_at` — timestamp nullable (soft-delete)
- `usuario_creacion` — Long, se setea desde `TenantContext.getUsuarioId()`
- `usuario_modificacion` — Long, se setea desde `TenantContext.getUsuarioId()`
- `activo` — Boolean (bandera de negocio, complementa el soft-delete)
- `empresa_id` — Long NOT NULL (multi-tenant)
- `sede_id` — Long NULL/NOT NULL según el dominio

### 2. Regla de oro: JPA minimalista

**Métodos JPA permitidos**:
```java
public interface EmpresaJpaRepository extends JpaRepository<EmpresaEntity, Long> {
    // Nada más. Todo lo demás va en QueryRepository.
}
```

**Métodos JPA prohibidos** (ejemplos de lo que NO hacer):
```java
// ❌ NUNCA - nombres largos, frágiles, ilegibles
Optional<EmpresaEntity> findByCodigoAndActivoTrueAndDeleted_atIsNull(String codigo);
Optional<EmpresaEntity> findByIdAndEmpresa_idAndDeleted_atIsNull(Long id, Long empresa_id);
List<T> findAllByEmpresa_idAndActivoTrueAndDeleted_atIsNullOrderByCreated_atDesc(...);
```

**Motivos**:
- Los nombres largos son frágiles (cualquier renombre rompe el repositorio).
- Spring Data JPA genera SQL inferior al que puedes escribir tú.
- Mezclar filtros de soft-delete, multi-tenant y búsqueda en un solo método viola el principio de responsabilidad única.
- Son difíciles de leer, refactorizar y probar.

**Regla práctica**:
- Si el filtro es `findById` solo → JpaRepository.
- Si requiere `deleted_at IS NULL`, `empresa_id = ?`, `sede_id = ?`, joins, búsquedas, paginación → QueryRepository.

### 3. QueryRepository como único punto de filtrado

Todas las consultas con filtros de negocio van ahí, con SQL nativo:

```java
public Optional<EmpresaDto> findActiveByCodigo(String codigo) {
    String sql = """
        SELECT id, codigo, nit, razon_social, activo
        FROM empresa
        WHERE codigo = :codigo
          AND activo = true
          AND deleted_at IS NULL
    """;
    // ...
}
```

### 4. Patrón de soft-delete

**Entity**:
```java
@Column(name = "deleted_at")
private LocalDateTime deleted_at;
```

**En el Service al eliminar**:
```java
@Transactional
public Boolean delete(Long id) {
    EmpresaEntity entity = empresaJpaRepository.findById(id)
        .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "No encontrado"));

    // Validar tenant ANTES de tocar nada
    validateTenant(entity);

    // Validar que no esté ya eliminado
    if (entity.getDeleted_at() != null) {
        throw new GlobalException(HttpStatus.BAD_REQUEST, "Registro ya eliminado");
    }

    entity.setDeleted_at(LocalDateTime.now());
    entity.setUsuario_modificacion(TenantContext.getUsuarioId());
    empresaJpaRepository.save(entity);
    return true;
}
```

**En el Service al leer por ID**:
```java
public EmpresaResponseDto findById(Long id) {
    EmpresaEntity entity = empresaJpaRepository.findById(id)
        .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "No encontrado"));

    // Validaciones en código, no en la query
    if (entity.getDeleted_at() != null) {
        throw new GlobalException(HttpStatus.NOT_FOUND, "No encontrado");
    }
    validateTenant(entity);

    return empresaMapper.toResponseDto(entity);
}
```

**Helper de validación tenant**:
```java
private void validateTenant(EmpresaEntity entity) {
    Long userEmpresaId = TenantContext.getEmpresaId();
    // Para tabla empresa, si no es super-admin, solo puede ver la suya
    if (!TenantContext.isSuperAdmin() && !entity.getId().equals(userEmpresaId)) {
        throw new GlobalException(HttpStatus.NOT_FOUND, "No encontrado");
    }
}
```

Para tablas con `empresa_id`:
```java
private void validateTenant(PacienteEntity entity) {
    Long userEmpresaId = TenantContext.getEmpresaId();
    if (!entity.getEmpresa_id().equals(userEmpresaId)) {
        throw new GlobalException(HttpStatus.NOT_FOUND, "No encontrado");
    }
}
```

---

## Stack tecnológico

- **Lenguaje**: Java 17+
- **Framework**: Spring Boot 3.x
- **Persistencia**: Spring Data JPA (mínimo) + NamedParameterJdbcTemplate (principal)
- **BD**: PostgreSQL 16+
- **Mapeo**: MapStruct (`componentModel = "spring"`)
- **Validación**: `jakarta.validation`
- **Lombok**: `@Getter`, `@Setter` (no `@Data`)
- **Seguridad**: JWT multi-tenant (ver `agente_jwt_multitenant.md`)
- **Respuesta HTTP**: wrapper `ApiResponse<T>`

---

## Convenciones NO NEGOCIABLES

### 1. Idioma por capa

| Capa | Idioma |
|------|--------|
| Tablas y columnas BD | Español (`tercero`, `empresa_id`) excepto auditoría (`created_at`) |
| Entidades JPA | Español con `snake_case` (`private String nombre_completo`) |
| DTOs | Inglés con `camelCase` (`private String fullName`) |
| Servicios/controllers | Inglés (`PatientService`, `PatientController`) |
| Variables locales | Inglés |
| Mensajes de error al usuario | Español |

### 2. Estructura de paquetes

```
com.<org>.<proyecto>
├── controller/                    # PatientController.java
├── dto/
│   ├── patient/
│   │   ├── CreatePatientRequestDto.java
│   │   ├── UpdatePatientRequestDto.java
│   │   ├── PatientResponseDto.java
│   │   └── PatientTableDto.java
│   ├── auth/
│   └── common/
│       ├── ApiResponse.java
│       └── PageableRequestDto.java
├── entity/                        # PacienteEntity.java (español)
├── mapper/
│   └── patient/
│       └── PatientMapper.java
├── repository/
│   └── patient/
│       ├── PatientJpaRepository.java   ← MÍNIMO
│       └── PatientQueryRepository.java ← TODO LO DEMÁS
├── service/
│   ├── PatientService.java
│   └── impl/
│       └── PatientServiceImpl.java
├── security/
│   ├── TenantContext.java
│   ├── TenantInfo.java
│   └── JwtAuthenticationFilter.java
└── util/
    ├── GlobalException.java
    └── MapperRepository.java
```

### 3. Regla de auditoría al crear/modificar

- Crear: setear `created_at` (via `@PrePersist`), `usuario_creacion` (desde TenantContext), `activo = true`, `empresa_id`, `sede_id`.
- Modificar: setear `updated_at` (via `@PreUpdate`), `usuario_modificacion`.
- Eliminar: setear `deleted_at = LocalDateTime.now()` y `usuario_modificacion`. Nunca `DELETE` físico.

---

## Plantillas de código (v3)

### Plantilla 1 — Entity JPA

```java
package com.<org>.<proyecto>.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "paciente")
@Getter
@Setter
public class PacienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Multi-tenant
    @Column(name = "empresa_id", nullable = false)
    private Long empresa_id;

    @Column(name = "tercero_id", nullable = false)
    private Long tercero_id;

    @Column(name = "grupo_sanguineo_id")
    private Long grupo_sanguineo_id;

    @Column(name = "factor_rh_id")
    private Long factor_rh_id;

    @Column(name = "alergias_conocidas")
    private String alergias_conocidas;

    @Column(name = "observaciones_clinicas")
    private String observaciones_clinicas;

    // Bandera de negocio
    @Column(nullable = false)
    private Boolean activo;

    // Auditoría estandar (en inglés, como la BD)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @Column(name = "deleted_at")
    private LocalDateTime deleted_at;

    @Column(name = "usuario_creacion")
    private Long usuario_creacion;

    @Column(name = "usuario_modificacion")
    private Long usuario_modificacion;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        if (activo == null) activo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}
```

### Plantilla 2 — JpaRepository (MÍNIMO)

```java
package com.<org>.<proyecto>.repository.patient;

import org.springframework.data.jpa.repository.JpaRepository;
import com.<org>.<proyecto>.entity.PacienteEntity;

public interface PatientJpaRepository extends JpaRepository<PacienteEntity, Long> {
    // Intencionalmente vacío.
    // Todo filtro de negocio va en PatientQueryRepository.
}
```

### Plantilla 3 — QueryRepository (TODO EL FILTRADO)

```java
package com.<org>.<proyecto>.repository.patient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.<org>.<proyecto>.dto.patient.PatientTableDto;
import com.<org>.<proyecto>.dto.patient.PatientResponseDto;
import com.<org>.<proyecto>.dto.common.PageableRequestDto;
import com.<org>.<proyecto>.util.MapperRepository;

@Repository
public class PatientQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public PatientQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ---------- Validaciones de existencia ----------

    public boolean existsActiveByTercero(Long tercero_id, Long empresa_id) {
        String sql = """
            SELECT COUNT(*)
            FROM paciente
            WHERE tercero_id = :tercero_id
              AND empresa_id = :empresa_id
              AND deleted_at IS NULL
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("tercero_id", tercero_id)
            .addValue("empresa_id", empresa_id);

        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    // ---------- Lecturas por ID (con tenant + soft-delete) ----------

    public Optional<PatientResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
            SELECT
                p.id,
                p.tercero_id,
                p.grupo_sanguineo_id,
                p.factor_rh_id,
                p.alergias_conocidas,
                p.observaciones_clinicas,
                p.activo,
                p.created_at
            FROM paciente p
            WHERE p.id = :id
              AND p.empresa_id = :empresa_id
              AND p.deleted_at IS NULL
            LIMIT 1
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("empresa_id", empresa_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();

        List<PatientResponseDto> mapped = MapperRepository.mapListToDtoList(rows, PatientResponseDto.class);
        return Optional.of(mapped.get(0));
    }

    // ---------- Paginación con búsqueda ----------

    public PageImpl<PatientTableDto> listPatients(PageableRequestDto request, Long empresa_id) {
        int pageNumber = request.getPage() != null ? request.getPage() : 0;
        int pageSize = request.getRows() != null ? request.getRows() : 10;
        String search = request.getSearch() != null ? request.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
            SELECT
                p.id,
                td.codigo AS document_type,
                t.numero_documento AS document_number,
                t.nombre_completo AS full_name,
                EXTRACT(YEAR FROM age(t.fecha_nacimiento))::int AS age,
                s.codigo AS sex,
                p.activo AS active,
                COUNT(*) OVER() AS total_rows
            FROM paciente p
            INNER JOIN tercero t ON t.id = p.tercero_id AND t.deleted_at IS NULL
            LEFT JOIN tipo_documento td ON td.id = t.tipo_documento_id
            LEFT JOIN sexo s ON s.id = t.sexo_id
            WHERE p.empresa_id = :empresa_id
              AND p.deleted_at IS NULL
        """);

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("empresa_id", empresa_id);

        if (search != null && !search.isEmpty()) {
            sql.append("""
                AND (
                    unaccent(LOWER(t.nombre_completo)) ILIKE unaccent(LOWER(:search))
                    OR t.numero_documento = :exactSearch
                )
            """);
            params.addValue("search", "%" + search + "%");
            params.addValue("exactSearch", search);
        }

        String orderBy = request.getOrderBy() != null ? request.getOrderBy() : "t.nombre_completo";
        String order = "DESC".equalsIgnoreCase(request.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) pageNumber * pageSize);
        params.addValue("limit", pageSize);

        List<Map<String, Object>> rows = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());

        List<PatientTableDto> result = MapperRepository.mapListToDtoList(rows, PatientTableDto.class);
        long count = rows.isEmpty() ? 0 : ((Number) rows.get(0).get("total_rows")).longValue();

        return new PageImpl<>(result, PageRequest.of(pageNumber, pageSize), count);
    }
}
```

### Plantilla 4 — Service Interface

```java
package com.<org>.<proyecto>.service;

import java.util.List;
import org.springframework.data.domain.PageImpl;

import com.<org>.<proyecto>.dto.patient.*;
import com.<org>.<proyecto>.dto.common.PageableRequestDto;

public interface PatientService {
    PatientResponseDto create(CreatePatientRequestDto dto);
    Boolean update(UpdatePatientRequestDto dto);
    Boolean delete(Long id);
    PatientResponseDto findById(Long id);
    PageImpl<PatientTableDto> listPatients(PageableRequestDto request);
}
```

### Plantilla 5 — Service Implementation

```java
package com.<org>.<proyecto>.service.impl;

import java.time.LocalDateTime;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.<org>.<proyecto>.dto.patient.*;
import com.<org>.<proyecto>.dto.common.PageableRequestDto;
import com.<org>.<proyecto>.entity.PacienteEntity;
import com.<org>.<proyecto>.mapper.patient.PatientMapper;
import com.<org>.<proyecto>.repository.patient.*;
import com.<org>.<proyecto>.security.TenantContext;
import com.<org>.<proyecto>.service.PatientService;
import com.<org>.<proyecto>.util.GlobalException;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientJpaRepository patientJpaRepository;
    private final PatientQueryRepository patientQueryRepository;
    private final PatientMapper patientMapper;

    public PatientServiceImpl(
        PatientJpaRepository patientJpaRepository,
        PatientQueryRepository patientQueryRepository,
        PatientMapper patientMapper
    ) {
        this.patientJpaRepository = patientJpaRepository;
        this.patientQueryRepository = patientQueryRepository;
        this.patientMapper = patientMapper;
    }

    @Override
    @Transactional
    public PatientResponseDto create(CreatePatientRequestDto dto) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        if (patientQueryRepository.existsActiveByTercero(dto.getThirdPartyId(), empresa_id)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "El tercero ya está registrado como paciente");
        }

        try {
            PacienteEntity entity = patientMapper.toEntity(dto);
            entity.setEmpresa_id(empresa_id);
            entity.setUsuario_creacion(usuario_id);
            entity.setActivo(true);

            PacienteEntity saved = patientJpaRepository.save(entity);
            return patientMapper.toResponseDto(saved);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear el paciente: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Boolean update(UpdatePatientRequestDto dto) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        // JPA simple - la validación del tenant y soft-delete se hace en código
        PacienteEntity entity = patientJpaRepository.findById(dto.getId())
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));

        validateTenantAndNotDeleted(entity, empresa_id);

        try {
            patientMapper.updateEntityFromDto(dto, entity);
            entity.setUsuario_modificacion(usuario_id);
            patientJpaRepository.save(entity);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        PacienteEntity entity = patientJpaRepository.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));

        validateTenantAndNotDeleted(entity, empresa_id);

        entity.setDeleted_at(LocalDateTime.now());
        entity.setUsuario_modificacion(usuario_id);
        patientJpaRepository.save(entity);
        return true;
    }

    @Override
    public PatientResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return patientQueryRepository.findActiveById(id, empresa_id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
    }

    @Override
    public PageImpl<PatientTableDto> listPatients(PageableRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        return patientQueryRepository.listPatients(request, empresa_id);
    }

    // ---------- helpers ----------

    private void validateTenantAndNotDeleted(PacienteEntity entity, Long empresa_id) {
        if (entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }
        if (!entity.getEmpresa_id().equals(empresa_id)) {
            // Mismo mensaje para evitar filtración de información cross-tenant
            throw new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }
    }
}
```

### Plantilla 6 — Mapper (igual que v2, con traducción EN↔ES)

```java
@Mapper(componentModel = "spring")
public interface PatientMapper {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "empresa_id", ignore = true),
        @Mapping(target = "usuario_creacion", ignore = true),
        @Mapping(target = "usuario_modificacion", ignore = true),
        @Mapping(target = "created_at", ignore = true),
        @Mapping(target = "updated_at", ignore = true),
        @Mapping(target = "deleted_at", ignore = true),
        @Mapping(target = "activo", ignore = true),

        @Mapping(source = "thirdPartyId", target = "tercero_id"),
        @Mapping(source = "bloodGroupId", target = "grupo_sanguineo_id"),
        @Mapping(source = "rhFactorId", target = "factor_rh_id"),
        @Mapping(source = "knownAllergies", target = "alergias_conocidas"),
        @Mapping(source = "clinicalNotes", target = "observaciones_clinicas")
    })
    PacienteEntity toEntity(CreatePatientRequestDto dto);

    @Mappings({
        @Mapping(source = "tercero_id", target = "thirdPartyId"),
        @Mapping(source = "grupo_sanguineo_id", target = "bloodGroupId"),
        @Mapping(source = "factor_rh_id", target = "rhFactorId"),
        @Mapping(source = "alergias_conocidas", target = "knownAllergies"),
        @Mapping(source = "observaciones_clinicas", target = "clinicalNotes"),
        @Mapping(source = "activo", target = "active"),
        @Mapping(source = "created_at", target = "createdAt")
    })
    PatientResponseDto toResponseDto(PacienteEntity entity);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "empresa_id", ignore = true),
        @Mapping(target = "tercero_id", ignore = true),
        @Mapping(target = "usuario_creacion", ignore = true),
        @Mapping(target = "usuario_modificacion", ignore = true),
        @Mapping(target = "created_at", ignore = true),
        @Mapping(target = "updated_at", ignore = true),
        @Mapping(target = "deleted_at", ignore = true),

        @Mapping(source = "bloodGroupId", target = "grupo_sanguineo_id"),
        @Mapping(source = "rhFactorId", target = "factor_rh_id"),
        @Mapping(source = "knownAllergies", target = "alergias_conocidas"),
        @Mapping(source = "clinicalNotes", target = "observaciones_clinicas")
    })
    void updateEntityFromDto(UpdatePatientRequestDto dto, @MappingTarget PacienteEntity entity);
}
```

### Plantilla 7 — Controller (igual que v2)

Controllers no cambian respecto a v2. Usar `ApiResponse<T>`, inyección por constructor, endpoints en inglés (`/api/patients`, `/api/companies`).

---

## Decisión clave de diseño: JPA vs QueryRepository

**Regla única**:

| Operación | Dónde se hace |
|-----------|----------------|
| `save()` (insertar/actualizar) | JpaRepository |
| `findById()` puro (sin filtros) | JpaRepository |
| `findById` + validación tenant + soft-delete | **QueryRepository** (retorna DTO) o **Service** (valida tras JPA) |
| Listados con filtros | QueryRepository |
| Paginación | QueryRepository |
| `exists...` | QueryRepository |
| Búsquedas complejas | QueryRepository |
| Joins con múltiples tablas | QueryRepository |

**Ventajas**:
- JPA repository siempre tiene 0 líneas útiles más allá del `extends JpaRepository`.
- SQL nativo visible, auditable, optimizable.
- Filtros de multi-tenant controlados en un solo lugar.
- Nunca más nombres de método de 80 caracteres.

---

## Qué NO hacer (reglas duras)

- ❌ Métodos JPA con nombres derivados largos (`findByCodigoAndActivoTrueAndDeleted_atIsNull`).
- ❌ `@Query` con JPQL largo en JpaRepository.
- ❌ Campos de entidad en camelCase (deben ser snake_case como la BD).
- ❌ Campos de DTO en snake_case (deben ser camelCase como Java).
- ❌ Aceptar `empresa_id` o `sede_id` en DTOs de request.
- ❌ `@Data` de Lombok.
- ❌ `@Autowired` en campo (usar constructor).
- ❌ DELETE físico (usar `deleted_at`).
- ❌ Respuestas HTTP fuera de `ApiResponse<T>`.
- ❌ Revelar cross-tenant: si el registro es de otra empresa, mensaje igual que "no existe" (404, no 403).

---

## Checklist antes de entregar código

- [ ] Entity con `created_at`, `updated_at`, `deleted_at`, `usuario_creacion`, `usuario_modificacion`, `activo`, `empresa_id`, `sede_id` (si aplica).
- [ ] JpaRepository vacío (solo hereda de `JpaRepository`).
- [ ] QueryRepository con SQL nativo y filtros explícitos (`deleted_at IS NULL`, `empresa_id = ?`).
- [ ] Service usa `findById()` simple + validación en código.
- [ ] Delete siempre es soft-delete (`deleted_at = now()`).
- [ ] Mapper traduce EN↔ES explícitamente.
- [ ] Controller retorna `ApiResponse<T>` sin try/catch innecesarios.
- [ ] DTOs en inglés con `camelCase`.
- [ ] Sin `@Autowired` en campos.
- [ ] Mensajes de error en español.

---

## Instrucción final

Este agente respeta la regla de oro: **JPA mínimo, QueryRepository para todo lo demás**. Los repositorios JPA nunca tienen métodos derivados largos; los filtros de multi-tenant y soft-delete viven en el QueryRepository con SQL nativo claro y optimizable.