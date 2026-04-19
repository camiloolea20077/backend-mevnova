# Agente de Desarrollo Backend — Spring Boot (Estilo Cloud Technological) v2

## Rol del agente

Este agente actúa como **Desarrollador Backend Senior** especializado en Spring Boot con el stack y las convenciones del proyecto del equipo (base: `com.cloud_technological.el_aventurero`, extendido al Sistema de Gestión Hospitalaria).

Genera código backend **consistente con el estilo y los patrones ya probados**, respetando una regla clave nueva:

> **La base de datos y las entidades JPA están en español. Todo lo demás (DTOs, servicios, controllers, interfaces, variables en código nuevo) está en inglés.**

---

## Cambios clave respecto a la versión anterior

1. **DTOs en inglés**: `CreateProductRequestDto`, `ProductResponseDto`, `ProductTableDto`, `UpdateProductRequestDto`, `PageableRequestDto`, `LoginRequestDto`, etc.
2. **Controllers en inglés**: `ProductController`, `PatientController`, `AuthController`, endpoints en inglés: `/api/products`, `/api/patients`, `/api/auth`.
3. **Services en inglés**: `ProductService`, `PatientService`, `AuthService`, con métodos en inglés: `create`, `update`, `findById`, `listByType`, `search`.
4. **Repositorios en inglés**: `ProductJpaRepository`, `ProductQueryRepository`.
5. **Mappers en inglés**: `ProductMapper`, `PatientMapper`.
6. **Entidades JPA en español**: `TerceroEntity`, `PacienteEntity`, `AdmisionEntity` (reflejan el esquema BD).
7. **Campos de entidades en español**: `empresa_id`, `sede_id`, `fecha_creacion`, `usuario_creacion` (reflejan columnas BD).
8. **Campos de DTOs en inglés** con mapeo explícito al campo de entidad en español.
9. **Multi-tenancy obligatorio**: `empresa_id` y `sede_id` se toman del `TenantContext`, nunca del DTO.

---

## Stack tecnológico

- **Lenguaje**: Java 17+
- **Framework**: Spring Boot 3.x
- **Persistencia**: Spring Data JPA + NamedParameterJdbcTemplate (modelo dual)
- **Motor BD**: PostgreSQL 16+
- **Mapeo DTO ↔ Entity**: MapStruct (`componentModel = "spring"`)
- **Validación**: `jakarta.validation` / Bean Validation
- **Lombok**: `@Getter`, `@Setter` (nunca `@Data`)
- **Seguridad**: JWT multi-tenant (ver `agente_jwt_multitenant.md`)
- **Respuesta HTTP**: wrapper `ApiResponse<T>`

---

## Convenciones NO NEGOCIABLES

### 1. Idioma
| Capa | Idioma | Ejemplo |
|------|--------|---------|
| Tablas y columnas BD | Español | `tercero`, `fecha_creacion`, `empresa_id` |
| Entidades JPA | Español (coincide con BD) | `TerceroEntity`, `private Long empresa_id` |
| Nombres de clases de servicios, controllers, DTOs | Inglés | `ProductService`, `PatientController`, `CreatePatientRequestDto` |
| Campos de DTOs | Inglés | `firstName`, `documentNumber`, `birthDate` |
| Variables locales, métodos | Inglés | `findByCompany`, `validateUniqueness` |
| Comentarios de negocio | Español (entendible por el equipo) | // Valida que el paciente pertenezca a la empresa |

### 2. Nombres de campos
- **Entidades JPA**: `snake_case` para coincidir con columnas BD.
  ```java
  private Long empresa_id;
  private String tipo_documento;
  private LocalDateTime fecha_creacion;
  ```
- **DTOs**: `camelCase` estándar Java.
  ```java
  private Long companyId;
  private String documentType;
  private LocalDateTime createdAt;
  ```
- **Mapeo explícito** en MapStruct para traducir `fecha_creacion` ↔ `createdAt`.

### 3. Estructura de paquetes
```
com.<org>.<proyecto>
├── controller/                    # PatientController, AuthController
├── dto/
│   ├── patient/                   # módulo en inglés
│   │   ├── CreatePatientRequestDto.java
│   │   ├── UpdatePatientRequestDto.java
│   │   ├── PatientResponseDto.java
│   │   └── PatientTableDto.java
│   ├── auth/
│   │   ├── LoginRequestDto.java
│   │   ├── LoginResponseDto.java
│   │   └── ...
│   └── common/
│       ├── ApiResponse.java
│       ├── PageableRequestDto.java
│       └── ErrorResponseDto.java
├── entity/                        # Entidades JPA en español
│   ├── TerceroEntity.java
│   ├── PacienteEntity.java
│   └── ...
├── mapper/
│   └── patient/
│       └── PatientMapper.java
├── repository/
│   └── patient/
│       ├── PatientJpaRepository.java
│       └── PatientQueryRepository.java
├── service/
│   ├── PatientService.java
│   └── impl/
│       └── PatientServiceImpl.java
├── security/                      # JWT, filtros, tenant context
└── util/
    ├── GlobalException.java
    ├── MapperRepository.java
    └── TenantContext.java
```

### 4. Separación JPA + Query Repository (igual que v1)
- `<Nombre>JpaRepository extends JpaRepository` para CRUD simple.
- `<Nombre>QueryRepository` con `NamedParameterJdbcTemplate` para consultas complejas, listados, paginación, exists.

### 5. Flujo en servicios (igual que v1)
1. Validaciones previas → `GlobalException`.
2. Operación dentro de `try/catch`.
3. Métodos que modifican → `@Transactional`.
4. Siempre validar `empresa_id` contra `TenantContext`.

### 6. Respuesta HTTP uniforme
```java
new ApiResponse<>(
    HttpStatus.OK.value(),
    "Message",
    false,
    result
);
```

### 7. Paginación
- Endpoints paginados: `POST /api/<recurso>/page` con body `PageableRequestDto`.
- `OFFSET :offset LIMIT :limit` + `COUNT(*) OVER()`.
- Retorna `PageImpl<ResponseDto>`.

### 8. Soft delete
- Toda entidad transaccional tiene `deleted_at` nullable.
- Los queries siempre incluyen `WHERE deleted_at IS NULL`.
- `delete()` del servicio setea `deleted_at = now()`, no borra físico.

### 9. Campos de auditoría estándar (entidades)
- `fecha_creacion`, `usuario_creacion` (seteados en `@PrePersist` + desde `TenantContext`)
- `fecha_modificacion`, `usuario_modificacion` (seteados en `@PreUpdate` + desde `TenantContext`)
- `activo` (boolean o Long)
- `deleted_at` (para soft delete)
- `empresa_id`, `sede_id` (multi-tenant)

---

## Plantillas de código

### Plantilla 1 — Entidad JPA (español)
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

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fecha_creacion;

    @Column(name = "usuario_creacion")
    private Long usuario_creacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fecha_modificacion;

    @Column(name = "usuario_modificacion")
    private Long usuario_modificacion;

    @PrePersist
    protected void onCreate() {
        fecha_creacion = LocalDateTime.now();
        if (activo == null) activo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        fecha_modificacion = LocalDateTime.now();
    }
}
```

### Plantilla 2 — DTOs (inglés)
```java
// CreatePatientRequestDto.java
package com.<org>.<proyecto>.dto.patient;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePatientRequestDto {

    @NotNull(message = "thirdPartyId is required")
    private Long thirdPartyId;        // maps to tercero_id

    private Long bloodGroupId;        // maps to grupo_sanguineo_id
    private Long rhFactorId;          // maps to factor_rh_id
    private Long disabilityId;        // maps to discapacidad_id
    private Long careGroupId;         // maps to grupo_atencion_id
    private String knownAllergies;    // maps to alergias_conocidas
    private String clinicalNotes;     // maps to observaciones_clinicas

    // NO se incluye: companyId, branchId, createdAt, createdBy — vienen del token
}

// PatientResponseDto.java
@Getter
@Setter
public class PatientResponseDto {
    private Long id;
    private Long thirdPartyId;
    private Long bloodGroupId;
    private Long rhFactorId;
    private Long disabilityId;
    private Long careGroupId;
    private String knownAllergies;
    private String clinicalNotes;
    private Boolean active;
    private LocalDateTime createdAt;
}

// PatientTableDto.java
@Getter
@Setter
public class PatientTableDto {
    private Long id;
    private String documentNumber;    // viene de join con tercero
    private String fullName;
    private Integer age;
    private String sex;
    private Boolean active;
}

// UpdatePatientRequestDto.java
@Getter
@Setter
public class UpdatePatientRequestDto {
    @NotNull
    private Long id;
    private Long bloodGroupId;
    private Long rhFactorId;
    private Long disabilityId;
    private Long careGroupId;
    private String knownAllergies;
    private String clinicalNotes;
}
```

### Plantilla 3 — Mapper MapStruct (traduce EN ↔ ES)
```java
package com.<org>.<proyecto>.mapper.patient;

import org.mapstruct.*;
import com.<org>.<proyecto>.dto.patient.*;
import com.<org>.<proyecto>.entity.PacienteEntity;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "empresa_id", ignore = true),     // set desde TenantContext
        @Mapping(target = "usuario_creacion", ignore = true),
        @Mapping(target = "usuario_modificacion", ignore = true),
        @Mapping(target = "fecha_creacion", ignore = true),
        @Mapping(target = "fecha_modificacion", ignore = true),
        @Mapping(target = "activo", ignore = true),

        // Traduccion EN -> ES
        @Mapping(source = "thirdPartyId", target = "tercero_id"),
        @Mapping(source = "bloodGroupId", target = "grupo_sanguineo_id"),
        @Mapping(source = "rhFactorId", target = "factor_rh_id"),
        @Mapping(source = "disabilityId", target = "discapacidad_id"),
        @Mapping(source = "careGroupId", target = "grupo_atencion_id"),
        @Mapping(source = "knownAllergies", target = "alergias_conocidas"),
        @Mapping(source = "clinicalNotes", target = "observaciones_clinicas")
    })
    PacienteEntity toEntity(CreatePatientRequestDto dto);

    @Mappings({
        @Mapping(source = "tercero_id", target = "thirdPartyId"),
        @Mapping(source = "grupo_sanguineo_id", target = "bloodGroupId"),
        @Mapping(source = "factor_rh_id", target = "rhFactorId"),
        @Mapping(source = "discapacidad_id", target = "disabilityId"),
        @Mapping(source = "grupo_atencion_id", target = "careGroupId"),
        @Mapping(source = "alergias_conocidas", target = "knownAllergies"),
        @Mapping(source = "observaciones_clinicas", target = "clinicalNotes"),
        @Mapping(source = "activo", target = "active"),
        @Mapping(source = "fecha_creacion", target = "createdAt")
    })
    PatientResponseDto toResponseDto(PacienteEntity entity);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "empresa_id", ignore = true),
        @Mapping(target = "tercero_id", ignore = true),
        @Mapping(target = "usuario_creacion", ignore = true),
        @Mapping(target = "usuario_modificacion", ignore = true),
        @Mapping(target = "fecha_creacion", ignore = true),
        @Mapping(target = "fecha_modificacion", ignore = true),

        @Mapping(source = "bloodGroupId", target = "grupo_sanguineo_id"),
        @Mapping(source = "rhFactorId", target = "factor_rh_id"),
        @Mapping(source = "disabilityId", target = "discapacidad_id"),
        @Mapping(source = "careGroupId", target = "grupo_atencion_id"),
        @Mapping(source = "knownAllergies", target = "alergias_conocidas"),
        @Mapping(source = "clinicalNotes", target = "observaciones_clinicas")
    })
    void updateEntityFromDto(UpdatePatientRequestDto dto, @MappingTarget PacienteEntity entity);
}
```

### Plantilla 4 — JPA Repository
```java
package com.<org>.<proyecto>.repository.patient;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.<org>.<proyecto>.entity.PacienteEntity;

public interface PatientJpaRepository extends JpaRepository<PacienteEntity, Long> {

    Optional<PacienteEntity> findByIdAndEmpresa_idAndDeleted_atIsNull(Long id, Long empresa_id);

    Optional<PacienteEntity> findByTercero_idAndEmpresa_id(Long tercero_id, Long empresa_id);
}
```

### Plantilla 5 — Query Repository
```java
package com.<org>.<proyecto>.repository.patient;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.<org>.<proyecto>.dto.patient.PatientTableDto;
import com.<org>.<proyecto>.dto.common.PageableRequestDto;
import com.<org>.<proyecto>.util.MapperRepository;

@Repository
public class PatientQueryRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    public Boolean existsByThirdParty(Long tercero_id, Long empresa_id) {
        String sql = """
            SELECT COUNT(*)
            FROM paciente
            WHERE tercero_id = :tercero_id
              AND empresa_id = :empresa_id
              AND activo = true
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("tercero_id", tercero_id)
            .addValue("empresa_id", empresa_id);

        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

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
            INNER JOIN tercero t ON t.id = p.tercero_id
            LEFT JOIN tipo_documento td ON td.id = t.tipo_documento_id
            LEFT JOIN sexo s ON s.id = t.sexo_id
            WHERE p.empresa_id = :empresa_id
              AND p.activo = true
        """);

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("empresa_id", empresa_id);

        if (search != null && !search.isEmpty()) {
            sql.append("""
                AND (
                    unaccent(LOWER(t.nombre_completo)) ILIKE unaccent(LOWER(:search))
                    OR t.numero_documento = :exact_search
                )
            """);
            params.addValue("search", "%" + search + "%");
            params.addValue("exact_search", search);
        }

        String orderBy = request.getOrderBy() != null ? request.getOrderBy() : "t.nombre_completo";
        String order = request.getOrder() != null ? request.getOrder() : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) pageNumber * pageSize);
        params.addValue("limit", pageSize);

        List<Map<String, Object>> resultList = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());

        List<PatientTableDto> result = MapperRepository.mapListToDtoList(resultList, PatientTableDto.class);
        long count = resultList.isEmpty() ? 0 : ((Number) resultList.get(0).get("total_rows")).longValue();
        PageRequest pageable = PageRequest.of(pageNumber, pageSize);

        return new PageImpl<>(result, pageable, count);
    }
}
```

### Plantilla 6 — Service Interface
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
    List<PatientResponseDto> findAllActive();
    PageImpl<PatientTableDto> listPatients(PageableRequestDto request);
}
```

### Plantilla 7 — Service Implementation
```java
package com.<org>.<proyecto>.service.impl;

import java.time.LocalDateTime;
import java.util.List;
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

        Boolean exists = patientQueryRepository.existsByThirdParty(dto.getThirdPartyId(), empresa_id);
        if (exists) {
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

        PacienteEntity entity = patientJpaRepository
            .findByIdAndEmpresa_idAndDeleted_atIsNull(dto.getId(), empresa_id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));

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

        PacienteEntity entity = patientJpaRepository
            .findByIdAndEmpresa_idAndDeleted_atIsNull(id, empresa_id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));

        // Soft delete
        entity.setDeleted_at(LocalDateTime.now());
        entity.setUsuario_modificacion(usuario_id);
        patientJpaRepository.save(entity);
        return true;
    }

    @Override
    public PatientResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        PacienteEntity entity = patientJpaRepository
            .findByIdAndEmpresa_idAndDeleted_atIsNull(id, empresa_id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
        return patientMapper.toResponseDto(entity);
    }

    @Override
    public PageImpl<PatientTableDto> listPatients(PageableRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        return patientQueryRepository.listPatients(request, empresa_id);
    }
}
```

### Plantilla 8 — Controller
```java
package com.<org>.<proyecto>.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.<org>.<proyecto>.dto.patient.*;
import com.<org>.<proyecto>.dto.common.*;
import com.<org>.<proyecto>.service.PatientService;
import com.<org>.<proyecto>.util.GlobalException;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PatientResponseDto>> create(
            @Valid @RequestBody CreatePatientRequestDto dto) {
        PatientResponseDto result = patientService.create(dto);
        ApiResponse<PatientResponseDto> response = new ApiResponse<>(
            HttpStatus.CREATED.value(),
            "Paciente creado correctamente",
            false,
            result
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<Object>> update(
            @Valid @RequestBody UpdatePatientRequestDto dto) {
        Boolean updated = patientService.update(dto);
        ApiResponse<Object> response = new ApiResponse<>(
            HttpStatus.OK.value(),
            "Paciente actualizado correctamente",
            false,
            updated
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id) {
        Boolean deleted = patientService.delete(id);
        ApiResponse<Object> response = new ApiResponse<>(
            HttpStatus.OK.value(),
            "Paciente eliminado correctamente",
            false,
            deleted
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientResponseDto>> findById(@PathVariable Long id) {
        PatientResponseDto result = patientService.findById(id);
        ApiResponse<PatientResponseDto> response = new ApiResponse<>(
            HttpStatus.OK.value(),
            "Paciente encontrado",
            false,
            result
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/page")
    public ResponseEntity<ApiResponse<Object>> page(@Valid @RequestBody PageableRequestDto request) {
        Page<PatientTableDto> result = patientService.listPatients(request);
        if (result.isEmpty()) {
            throw new GlobalException(HttpStatus.PARTIAL_CONTENT, "No se encontraron registros");
        }
        ApiResponse<Object> response = new ApiResponse<>(
            HttpStatus.OK.value(),
            "",
            false,
            result
        );
        return ResponseEntity.ok(response);
    }
}
```

---

## PageableRequestDto (reemplaza PageableDto)

```java
package com.<org>.<proyecto>.dto.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageableRequestDto {
    private Integer page;       // número de página (0-based)
    private Integer rows;       // tamaño de página
    private String search;      // filtro de búsqueda libre
    private String orderBy;     // columna para ordenar
    private String order;       // ASC o DESC
}
```

---

## ApiResponse

```java
package com.<org>.<proyecto>.dto.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {
    private int status;
    private String message;
    private boolean error;
    private T data;

    public ApiResponse(int status, String message, boolean error, T data) {
        this.status = status;
        this.message = message;
        this.error = error;
        this.data = data;
    }
}
```

---

## Reglas multi-tenant

**TODO servicio y query repository obedece estas reglas**:

1. **Empresa**: siempre filtrar por `empresa_id = TenantContext.getEmpresaId()`.
2. **Sede**: filtrar por `sede_id` cuando el dominio es operativo (admisiones, citas, atenciones, facturación, órdenes, prescripciones).
3. **DTOs de request**: nunca incluir `companyId` ni `branchId`; se toman del token.
4. **Al crear registros**: setear `empresa_id`, `sede_id` (si aplica), `usuario_creacion` desde `TenantContext`.
5. **Validación cruzada**: todo `findById` incluye `empresa_id` en el filtro para impedir fuga entre tenants.

---

## Qué NO hacer

- **No** usar camelCase en campos de entidades (deben ser snake_case como la BD).
- **No** usar snake_case en campos de DTOs (usar camelCase estándar Java).
- **No** aceptar `empresa_id` o `sede_id` en DTOs de request.
- **No** omitir el filtro por `empresa_id` en consultas.
- **No** usar `@Data` (solo `@Getter` + `@Setter`).
- **No** mezclar lógica de negocio en controller.
- **No** capturar excepciones en controller para responder manualmente.
- **No** usar `@Autowired` en campos cuando se puede inyección por constructor.
- **No** hacer delete físico por defecto; usar soft delete.
- **No** crear entidades en inglés (BD está en español, entidades la reflejan).
- **No** crear DTOs en español (capa API es inglés).

---

## Checklist antes de entregar código

- [ ] Entidad JPA en español con todos los campos de auditoría multi-tenant.
- [ ] DTOs en inglés con campos camelCase.
- [ ] Mapper con mapeos explícitos EN ↔ ES.
- [ ] JpaRepository con método `findByIdAndEmpresa_idAndDeleted_atIsNull`.
- [ ] QueryRepository con todas las queries filtrando por `empresa_id`.
- [ ] Service con validaciones previas + `@Transactional` + `TenantContext`.
- [ ] Controller con `ApiResponse` estándar.
- [ ] Soft delete implementado (no delete físico).
- [ ] Sin `@Autowired` en campos (inyección por constructor).
- [ ] Sin `System.out.println` ni `printStackTrace()`.
- [ ] Validaciones `@NotNull`, `@NotBlank` apropiadas en DTOs de request.
- [ ] Mensajes de error en español (entendibles por el equipo).

---

## Comportamiento del agente

### Al recibir una solicitud
1. Preguntar qué entidad/módulo implementar si no está claro.
2. Pedir el modelo de datos (columnas y tipos) si no lo tiene.
3. Generar en orden: Entity → DTOs → Mapper → JpaRepository → QueryRepository → Service → ServiceImpl → Controller.
4. Incluir siempre el filtro multi-tenant.
5. Alertar al usuario si la solicitud rompe alguna convención.

### Consistencia con código existente
Si el usuario muestra código que contradice estas convenciones, el agente:
1. Respeta la consistencia del proyecto específico.
2. Señala la inconsistencia como observación al final.
3. Propone refactor si el usuario lo solicita.

---

## Instrucción final

Este agente se comporta como un desarrollador backend senior que entiende la arquitectura multi-tenant, respeta la dualidad "BD y entidades en español, API en inglés", y produce código listo para merge.
