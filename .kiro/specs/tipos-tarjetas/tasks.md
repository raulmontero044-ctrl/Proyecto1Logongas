# Implementation Plan — tipos-tarjetas

## 1. Fundación del backend y modelo de datos

- [x] 1.1 Generar el proyecto backend Spring Boot
  - Crear el proyecto Java 25 con Gradle 9.x (scripts Kotlin) y el plugin de Spring Boot 4.0.x.
  - Configurar las dependencias de Spring Web, Spring Data JPA, Bean Validation, conector MySQL y REST Assured (test).
  - Configurar la conexión a MySQL 8.4 (datasource, JPA, ddl-auto) mediante configuración de aplicación; arrancar MySQL vía Docker Compose.
  - _Resultado observable_: `./gradlew build` compila y la aplicación arranca conectada a MySQL 8.4 sin errores.

- [x] 1.2 Implementar el catálogo de tipos de tarjeta
  - Modelar la entidad de catálogo de tipos de tarjeta con un identificador único y un nombre con longitud suficiente para 'Débito' y 'Crédito' (≠ 50 caracteres).
  - Implementar el acceso a datos con una consulta que localiza un tipo por nombre ignorando mayúsculas y otra que lista todos los tipos.
  - Sembrar de forma idempotente los tipos 'Débito' y 'Crédito' al arrancar solo si el catálogo está vacío.
  - _Resultado observable_: al arrancar, la tabla del catálogo contiene 'Débito' y 'Crédito'; la búsqueda ignore-case y el listado devuelven los valores esperados.
  - _Requirements: 1.4, 4.1, 4.2_
  - _Boundary: TipoTarjeta, TipoTarjetaRepository, TarjetaDataInitializer_

- [x] 1.3 Implementar la entidad de tarjeta y su repositorio
  - Modelar la entidad de tarjeta con un identificador único generado automáticamente, el nombre del propietario obligatorio y el tipo opcional referenciado al catálogo.
  - Implementar el repositorio con las operaciones de guardado y búsqueda por identificador.
  - Asegurar que la unicidad del identificador quede garantizada por la clave primaria y que un intento de duplicado sea rechazado.
  - _Resultado observable_: la tabla `tarjeta` se crea con FK opcional al catálogo y la persistencia y consulta de tarjetas (con y sin tipo) funcionan.
  - _Requirements: 1.2, 2.1, 2.2, 4.3_
  - _Boundary: Tarjeta, TarjetaRepository_

## 2. Reglas de negocio y API REST del backend

- [x] 2.1 Implementar la lógica de negocio de tarjetas
  - Crear la operación de creación: rechaza el nombre del propietario vacío, resuelve el tipo por nombre ignorando mayúsculas y rechaza el tipo inexistente; permite la creación sin tipo.
  - Crear la operación de consulta por identificador que devuelve la tarjeta o comunica que no existe.
  - Documentar y exponer las excepciones de negocio utilizadas para nombre ausente, tipo inexistente y tarjeta no encontrada.
  - _Resultado observable_: el servicio crea tarjetas válidas asignando un id único y rechaza con mensaje claro: nombre vacío, tipo inexistente y tarjeta no encontrada.
  - _Requirements: 1.1, 1.3, 1.4, 2.1, 3.1, 4.1, 4.3_
  - _Boundary: TarjetaService_

- [x] 2.2 Implementar la API REST de tarjetas y catálogo
  - Exponer los endpoints de creación y consulta de tarjetas y el listado del catálogo, con DTOs de entrada/salida que no expongan las entidades.
  - Mapear los errores de negocio y validación a respuestas HTTP: 400 para nombre vacío y tipo inexistente, 404 para tarjeta no encontrada, con cuerpo `{codigo, mensaje}`.
  - Aplicar validación de entrada sobre el nombre del propietario antes de delegar al servicio.
  - _Resultado observable_: `POST /api/tarjetas` responde 201 con la tarjeta creada (y 400 ante datos inválidos); `GET /api/tarjetas/{id}` responde 200/404; `GET /api/tipos-tarjetas` responde 200 con el catálogo.
  - _Requirements: 1.1, 1.3, 1.4, 3.1_
  - _Boundary: TarjetaController, DTOs_

## 3. Pruebas del backend

- [x] 3.1 (P) Probar la lógica de negocio de tarjetas
  - Cubrir: creación válida con id asignado, nombre vacío rechazado, coincidencia ignore-case del tipo, tipo inexistente rechazado, tipo opcional y rechazo por id duplicado.
  - _Resultado observable_: la suite de pruebas unitarias del servicio pasa y verifica las reglas anteriores.
  - _Requirements: 1.1, 1.3, 1.4, 2.1, 4.1, 4.3_
  - _Boundary: TarjetaServiceTest_
  - _Depends: 2.1_

- [x] 3.2 (P) Probar el acceso a datos de tarjetas y catálogo
  - Cubrir: búsqueda de tipo ignore-case, persistencia de tarjeta con referencia al tipo y rechazo de identificador duplicado.
  - _Resultado observable_: la suite `@DataJpaTest` pasa sobre el repositorio real de H2 o MySQL.
  - _Covertura ya aportada por 1.2/1.3 y aprobada en review_: `buscarTipoPorNombreCoincideIgnorandoMayusculasYMinusculas` (4.1), `guardarYRecuperarTarjetaConTipoReferenciadoAlCatalogo` (1.2/FK), `guardarTarjetaConIdentificadorDuplicadoEsRechazadoPorConstraintDeClavePrimaria` (2.2); ejecución continua en verde en cada build posterior.
  - _Requirements: 1.2, 2.2, 4.1_
  - _Boundary: TarjetaRepositoryTest_
  - _Depends: 1.3_

- [x] 3.3 Probar la API REST de tarjetas
  - Cubrir con REST Assured: creación 201 con tipo asociado, 400 por tipo inválido, 200 al consultar una tarjeta existente, listado 200 del catálogo y 404 para tarjeta inexistente.
  - _Resultado observable_: la suite de integración de la API pasa de extremo a extremo contra el backend arrancado.
  - _Requirements: 1.1, 1.2, 1.4, 3.1_
  - _Boundary: TarjetaControllerIT_

## 4. Fundación del frontend

- [x] 4.1 Generar la aplicación Angular del frontend
  - Crear el workspace Angular 21 con TypeScript 5.9.x e integrar Bootstrap.
  - Configurar el alias de ruta interna `@/` para los imports del proyecto.
  - _Resultado observable_: `ng build` compila y `ng serve` muestra la aplicación base en el navegador.

- [x] 4.2 Implementar modelos, servicio HTTP y rutas del feature de tarjetas
  - Definir las interfaces tipadas de tarjeta y de tipo de tarjeta, sin `any`.
  - Implementar el servicio HTTP con las llamadas de creación de tarjeta, consulta por id y listado del catálogo, con manejo tipado de errores.
  - Registrar las rutas del feature de tarjetas en la aplicación.
  - _Resultado observable_: el servicio invoca los endpoints REST correctos y las rutas del feature están navegables.
  - _Requirements: 1.1, 3.1_
  - _Boundary: tarjeta.model, tarjeta.service, tarjeta.routes_

## 5. Interfaz de creación y consulta

- [x] 5.1 (P) Construir el formulario y la página de creación de tarjetas
  - Crear el formulario con los campos de nombre del propietario y tipo (seleccionable desde el catálogo cargado).
  - Enviar la petición de creación al backend y mostrar los mensajes de error de validación recibidos.
  - _Resultado observable_: el banquero crea una tarjeta desde la interfaz y ve confirmación o el error mostrado en pantalla.
  - _Requirements: 1.1, 1.3, 1.4_
  - _Boundary: tarjeta-form, tarjeta-create_
  - _Depends: 4.2_

- [ ] 5.2 (P) Construir la página de consulta del tipo de tarjeta
  - Crear la vista que consulta una tarjeta por identificador y muestra su tipo.
  - Formatear el tipo siempre en MAYÚSCULAS y en negrita.
  - _Resultado observable_: la vista muestra el tipo de la tarjeta consultada en mayúsculas y negrita, o un mensaje si la tarjeta no existe.
  - _Requirements: 3.1, 3.2_
  - _Boundary: tarjeta-detail_
  - _Depends: 4.2_

## 6. Pruebas del frontend

- [x]* 6.1 (P) Probar el servicio HTTP de tarjetas
  - Cubrir el mapeo tipado de las respuestas REST de creación y consulta (requisitos 1.1 y 3.1). Deferible tras MVP porque la funcionalidad core queda cubierta por la implementación y las pruebas del backend.
  - _Resultado observable_: la prueba unitaria del servicio pasa y verifica el mapeo de respuestas.
  - _Requirements: 1.1, 3.1_
  - _Boundary: tarjeta.service_
  - _Nota (alcanzada en 4.2): `tarjeta.service.spec.ts` (Vitest) cubre POST/GET/listado y errores tipados `ApiError` y genérico — 5 tests en verde._

- [x]* 6.2 (P) Probar el formulario de creación
  - Cubrir el envío del payload correcto y la muestra de errores de validación (requisitos 1.1, 1.3 y 1.4). Deferible tras MVP por el mismo motivo que 6.1.
  - _Resultado observable_: la prueba del componente formulario pasa.
  - _Requirements: 1.1, 1.3, 1.4_
  - _Boundary: tarjeta-form_
  - _Nota (alcanzada en 5.1): `tarjeta-form.component.spec.ts` + `tarjeta-create.page.spec.ts` (Vitest) cubren payload correcto, no-emisión con nombre vacío, confirmación con id y errores visibles — 8 tests en verde._

- [x]* 6.3 Probar la vista de consulta del tipo
  - Cubrir el renderizado del tipo en MAYÚSCULAS y negrita (requisito 3.2). Deferible tras MVP porque el comportamiento ya está implementado en la vista.
  - _Resultado observable_: la prueba del componente detalle pasa y verifica el formato visual.
  - _Requirements: 3.2_
  - _Boundary: tarjeta-detail_
  - _Nota (alcanzada en 5.2): `tarjeta-detail.page.spec.ts` (Vitest) verifica `text-uppercase fw-bold` + texto en mayúsculas, placeholder `(SIN TIPO)` y error `TARJETA_NO_ENCONTRADA` — 4 tests en verde._

## 7. Verificación integral del flujo crear → consultar

- [x] 7. Ejecutar una comprobación integral del flujo de tarjetas
  - Levantar backend y frontend y verificar que un banquero crea una tarjeta con tipo desde la interfaz y que la consulta muestra el tipo en MAYÚSCULAS y en negrita.
  - Comprobar la reacción ante nombre vacío y tipo inexistente en la interfaz.
  - Ejecutar la suite completa de pruebas del backend y frontend sin errores.
  - _Resultado observable_: el flujo crear → consultar funciona de extremo a extremo y toda la suite de pruebas pasa.
  - _Requirements: 1.1, 3.1, 3.2_
  - _Depends: 5.1, 5.2, 3.3_
  - _Nota (2026-09-24): verificado E2E real a través del proxy `localhost:4200/api` → `localhost:8080`. Crear con tipo `crédito` (minúsculas) → 201 con tipo normalizado `Crédito`; consulta id → 200 con tipo; nombre vacío → 400 `NOMBRE_REQUERIDO`; tipo inexistente → 400 `TIPO_NO_EXISTE`; id inexistente → 404 `TARJETA_NO_ENCONTRADA`; SPA sirve `<app-root>`. Suites: backend 26 tests en verde (re-run fresco) y frontend 19 tests en verde. El formato MAYÚSCULAS+negrita en la vista se cubre por la spec de 5.2._

## Implementation Notes

- **Entorno backend (decisión de usuario 2026-09-24)**: sin Docker. MySQL 8.4.9 local en `127.0.0.1:3307`, base de datos `banco`, usuario `banco`/`banco123` (dev). JDK 25 Temurin en `C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot`. Spring Boot target 4.0.8. Gradle 9.7.1 (bootstrap con wrapper). Configurar `spring.datasource` con `jdbc:mysql://127.0.0.1:3307/banco`.
- **PATH de Java**: la sesión aún resuelve Java 21; usar `JAVA_HOME`/ruta completa al JDK 25 en los comandos de build. El `MySQL80` del sistema (8.0) no debe tocarse.
- **REST Assured ≥ 6.0.1** (decisión 3.3): rest-assured 5.5.7 es incompatible con el Groovy 5.0.x forzado por el BOM de Spring Boot 4.0.8 (NPE `Class.isAssignableFrom`); usar `io.rest-assured:rest-assured:6.0.1` en `testImplementation`.
- **Atomización de tests en Boot 4.0.x**: `spring-boot-starter-data-jpa-test` y `spring-boot-starter-webmvc-test` son dependencias test necesarias (los slices viven en módulos separados); mocks con `@MockitoBean` (spring-test 7); `@DataJpaTest` con `@AutoConfigureTestDatabase(replace=NONE)` corre contra MySQL real.