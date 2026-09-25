# Research & Design Decisions

---
**Propósito**: Capturar hallazgos de descubrimiento e investigaciones que fundamentan el diseño técnico.
---

## Summary
- **Feature**: `tipos-tarjetas`
- **Discovery Scope**: Simple Addition (greenfield, sin código previo)
- **Key Findings**:
  - El stack técnico ya está fijado en steering (Java 25, Spring Boot 4.0.x, JPA, MySQL 8.4, Angular 21, Gradle Kotlin) → sin investigación externa de dependencias nuevas.
  - La feature cubre la entidad *Tarjetas* (con propietario) cuyo tipo referencia el catálogo *TiposDeTarjetas*; el nombre de la spec (`tipos-tarjetas`) induce a error pero el alcance confirmado apunta a tarjetas.
  - El catálogo es dato de referencia de solo lectura (seed 'Débito'/'Crédito'); su gestión CRUD está fuera de alcance.

## Research Log

### Modelo de datos: catálogo vs entidad Tarjeta
- **Context**: La descripción original hablaba de una tabla *TiposDeTarjetas*; el usuario confirmó que el modelo es la entidad *Tarjetas con propietario* y el tipo se indica por referencia al catálogo.
- **Sources Consulted**: `.kiro/steering/product.md`, `.kiro/steering/structure.md`, `requirements.md` de la spec.
- **Findings**:
  - La tarjeta guarda id, nombre del propietario y tipo (referencia al catálogo).
  - El catálogo debe existir y estar poblado al menos con 'Débito' y 'Crédito' (AC 1.4, 4.2).
  - El tipo es opcional (FK nullable) y la coincidencia ignora mayúsculas (AC 4.1, 4.3).
- **Implications**: El catálogo vive dentro del feature package `tarjeta` como dato de referencia de solo lectura; el seed se garantiza con un inicializador idempotente.

### Identificación de tarjeta
- **Context**: AC 2.1–2.2 exigen identificador único y rechazo de duplicados.
- **Sources Consulted**: requirements.md.
- **Findings**: PK autoincremental con `GenerationType.IDENTITY` cumple unicidad; un intento de duplicado es rechazado por la constraint de clave primaria.
- **Implications**: Sin generación manual de id; el rechazo del segundo insert es responsabilidad del repositorio/BD.

## Architecture Pattern Evaluation

| Option | Description | Strengths | Risks / Limitations | Notes |
|--------|-------------|-----------|---------------------|-------|
| Layered por feature | Un paquete `tarjeta` con subcarpetas controller/service/repository/entity/dto | Consistente con steering structure.md; tareas acotadas | Riesgo de duplicar lógica entre features | Elegido |
| Hexagonal | Puertos y adaptadores alrededor del dominio | Desacoplamiento fuerte | Sobrecarga para un CRUD simple | Rechazado: sobrediseño |
| Feature global en capas | `controller/`, `service/` globales | Simple | Contradice steering structure.md | Rechazado |

## Design Decisions

### Decision: Ubicación del catálogo TiposDeTarjetas
- **Context**: La tarjeta referencia al catálogo; product.md lo describe como tabla propia, pero ninguna otra spec existe aún.
- **Alternatives Considered**:
  1. Paquete propio `tipo-tarjeta/` — separación máxima del catálogo
  2. Datos de referencia dentro del feature `tarjeta/` — agrupa tarjeta + su catálogo
- **Selected Approach**: Entidad `TipoTarjeta` y repositorio dentro del feature `tarjeta/`.
- **Rationale**: Evita un feature artificial para un catálogo de solo lectura; el patrón de steering organiza por funcionalidad y el catálogo pertenece funcionalmente a tarjeta.
- **Trade-offs**: El feature `tarjeta/` incluye más que la entidad Tarjeta; se documenta la frontera.
- **Follow-up**: Si más adelante el catálogo se gestiona, moverlo a feature propio y revalidar.

### Decision: Nombre de la entidad `Tarjeta` (no `TarjetaDebito`)
- **Context**: structure.md ejemplifica `TarjetaDebito`, pero el tipo de tarjeta es variable (Débito o Crédito).
- **Alternatives Considered**:
  1. `TarjetaDebito` — seguir el ejemplo de steering
  2. `Tarjeta` — el tipo es un atributo, no la identidad
- **Selected Approach**: Entidad `Tarjeta`.
- **Rationale**: Una entidad `TarjetaDebito` que pueda contener tipo 'Crédito' sería engañosa.
- **Trade-offs**: Pequeña desviación del ejemplo de steering, coherente con el modelo de datos confirmado.

### Decision: Seed del catálogo sin Flyway
- **Context**: Necesario poblar 'Débito'/'Crédito' para AC 1.4 y 4.2.
- **Alternatives Considered**:
  1. Flyway/Liquibase — gestión de migraciones estándar
  2. `CommandLineRunner` idempotente que inserta si está vacío
- **Selected Approach**: `TarjetaDataInitializer` (`CommandLineRunner`) con chequeo de vacío.
- **Rationale**: Sin dependencia nueva en un greenfield mínimo; seed simple y determinista.
- **Trade-offs**: Menos robusto que migraciones versionadas; suficiente para datos de referencia fijos.
- **Follow-up**: Migrar a Flyway si el proyecto adopta migraciones estructuradas.

### Decision: Sin interfaz de servicio
- **Context**: Simplificación para el patrón capa→servicio.
- **Alternatives Considered**:
  1. Interfaz `TarjetaService` + impl
  2. Clase concreta `TarjetaService`
- **Selected Approach**: Clase concreta.
- **Rationale**: Una única implementación sin segunda prevista no requiere indirección.
- **Trade-offs**: La sustitución por mock en tests se hace vía Mockito sobre la clase.
- **Follow-up**: Introducir interfaz si aparece un segundo impl o se exige aislamiento de módulo.

## Risks & Mitigations
- Spring Boot 4.0.x muy reciente — mitigación: validar anotaciones de controlador/validación durante implementación; versiones fijadas en steering.
- El nombre de la spec (`tipos-tarjetas`) no refleja la entidad Tarjetas — mitigación: documentación de frontera en design.md; posible renombrado a `tarjetas` acordado por el usuario.
- Coincidencia case-insensitive con acentos ('Débito' vs 'débito') — mitigación: comparación ignore-case gestionada por JPA; test específico.

## References
- `.kiro/steering/product.md`, `tech.md`, `structure.md` — contexto de producto y estructura.
- `requirements.md` de la spec — criterios de aceptación trazables.