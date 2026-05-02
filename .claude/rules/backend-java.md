# Regla Backend Java SGH

Aplicar cuando se modifiquen archivos:
- src/main/java/**/*.java

Reglas:
- Java 17.
- Spring Boot 3.x.
- Inyección por constructor.
- No usar @Autowired en campos.
- No usar @Data.
- Usar @Getter y @Setter.
- DTOs en inglés camelCase.
- Entidades en español snake_case como la BD.
- Services y controllers en inglés.
- Mensajes de error en español.
- Controller retorna ApiResponse<T>.
- No poner lógica de negocio en controller.
- Service maneja transacciones.
- Mapper con MapStruct.