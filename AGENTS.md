# Contexto del repositorio

- No hay manifiesto ni tarea global: `backend/` y `frontend/` son aplicaciones independientes; ejecuta sus comandos desde la carpeta correspondiente.
- La implementación actual está concentrada en la feature `tarjeta`; no asumas que los módulos futuros descritos en `.kiro/steering/structure.md` ya existen.
- `.kiro/steering/` contiene las decisiones de proyecto y `.kiro/specs/<feature>/` contiene requisitos, diseño, tareas y estado de cada feature. Consulta `/kiro-spec-status <feature>` antes de implementar y respeta sus aprobaciones.
- Escribe la documentación de cada spec en el idioma de `spec.json.language`; las respuestas de este repositorio son en español.

## Requisitos de ejecución

- Backend: Java 25 y el wrapper de Gradle 9.7.1. Si `java -version` no devuelve Java 25, corrige `JAVA_HOME` antes de compilar.
- El backend usa MySQL local en `127.0.0.1:3307`, base `banco`, sin Docker. Las credenciales de desarrollo están en `backend/src/main/resources/application.yml`; `ddl-auto` es `update`.
- No existen `run.sh`, `run.bat` ni Docker Compose en el repositorio, aunque algunos documentos de steering los mencionan; no los propongas como comandos.
- Frontend: usa npm y `frontend/package-lock.json`; no cambies el gestor de paquetes. El proxy de desarrollo envía `/api` a `http://localhost:8080`.

## Comandos

Desde `backend/`:

```powershell
.\gradlew.bat test
.\gradlew.bat build
.\gradlew.bat test --tests com.mibanco.tarjeta.TarjetaServiceTest
.\gradlew.bat bootRun
```

Desde `frontend/`:

```powershell
npm ci
npm start                         # servidor en http://localhost:4200/
npm run build
npm test -- --watch=false
npm test -- --watch=false --include src/app/tarjeta/services/tarjeta.service.spec.ts
```

- No hay scripts `lint` ni `typecheck`; `npm run build` es la verificación de compilación/typeScript de Angular.
- Las pruebas frontend usan Vitest con jsdom y están junto al código como `*.spec.ts`; `ng e2e` no está configurado aunque aparezca en el README generado.
- La suite backend necesita MySQL disponible: las pruebas JPA desactivan el reemplazo de base de datos y las pruebas de aplicación/integración arrancan Spring contra la configuración real.

## Arquitectura y contratos

- El entrypoint backend es `com.mibanco.TarjetaApplication`. Dentro de `com.mibanco.tarjeta`, los controllers solo validan/atienden HTTP, `service` contiene reglas de negocio, `repository` accede a JPA y `dto` define el contrato de API.
- La API actual es `POST /api/tarjetas`, `GET /api/tarjetas/{id}` y `GET /api/tipos-tarjetas`; los errores de negocio usan `{codigo, mensaje}`. `TarjetaDataInitializer` siembra `Débito` y `Crédito` solo cuando el catálogo está vacío.
- El entrypoint frontend es `frontend/src/main.ts`; la feature vive en `frontend/src/app/tarjeta/`, se carga mediante `tarjeta.routes.ts` y usa el alias `@/*` definido en `tsconfig.json`.
- Mantén TypeScript estricto y sin `any`; el formato usa 2 espacios, comillas simples y ancho de 100 columnas según `.editorconfig` y `.prettierrc`.
- No edites artefactos generados: `backend/build/` y `frontend/dist/`; las cachés de Angular/Gradle también son generados.

## Flujo de cambios

- Para trabajo nuevo, sigue las fases de Kiro (`/kiro-discovery`, requirements, design, tasks y aprobación) y luego `/kiro-impl`; usa `/kiro-validate-impl` para la validación de integración.
- Aplica `/kiro-review` tras una implementación, `/kiro-debug` ante fallos repetidos y `/kiro-verify-completion` antes de afirmar que una tarea o build está verificado.
