---
name: git-commit-planner
description: Analyze uncommitted Git changes (staged, unstaged, untracked, deleted, renamed) and propose a plan of atomic, coherent Conventional Commits grouped by logical functionality, then ask before executing them. Use when asked to plan commits, split changes into commits, organize pending work for commit, or review dirty state before committing.
---

# git-commit-planner

Analiza el estado actual de Git y produce un **plan de commits** atómicos y coherentes. Al final pregunta al usuario si quiere ejecutar esos commits.

## Límite absoluto (solo lectura durante el análisis)

Durante el análisis del repositorio, esta skill NO modifica nada. Prohibido:

- `git add`, `git commit`, `git commit -a`, `git stash`, `git restore`, `git checkout --`
- `git reset` (incluido `--soft`, `--mixed`, `--hard`)
- `git clean`, `git rm`, `git mv`, `git revert`
- Editar, crear, renombrar o borrar cualquier archivo
- `git pull`, `git push`, `git merge`, `git rebase`, `git cherry-pick`, `git am`

Comandos permitidos en esta fase: `git status`, `git diff`, `git log`, `git show`, `git ls-files`, `git rev-parse`, `git cat-file`, `git blame`, `git check-ignore`, `git stash list` (todos solo lectura).

La única excepción son las fases 8 (Commit) y 10 (Push), que se ejecutan **solo** tras confirmación explícita del usuario.

## Fase de commit (tras confirmación)

Solo si el usuario responde afirmativamente en la pregunta final:

- Permitidos: `git add <rutas explícitas>`, `git commit -m`, `git status`, `git diff --cached`, `git log -1`.
- Prohibido siempre: `git commit --amend`, `git reset`, `git rebase`, `git merge`, `--force`, modificar el contenido de los archivos, `git add -A` o `git add .` sin rutas explícitas.
- Añadir solo las rutas exactas de cada commit del plan aprobado. Nada más entra en el commit.
- Respetar el orden `#` del plan. Si un commit falla, detenerse y reportar; no continuar con los siguientes.
- Verificar antes de cada commit que el diff staged corresponde a lo planificado con `git diff --cached --stat`.

Si el usuario dice `[n]` o "cancelar", no se ejecuta nada en el repositorio: el plan queda como resultado y se pasa igualmente a la fase 9 (informe de estado y decisión de push).

## Fase de push (tras confirmación)

Solo si el usuario responde afirmativamente en la pregunta de push:

- Permitido: `git push` (y `git status -sb`, `git log --oneline origin/<rama>..HEAD` para verificar).
- Prohibido: `--force`, `--force-with-lease`, cambiar de rama, `git push --delete`, `git merge`.

Si el usuario dice `[n]`, no hacer push y terminar.

## Fase de push fallido (tras confirmación)

Si `git push` es rechazado, no intentar reintentar a ciegas. Clasificar primero el motivo real en la salida de error:

| Motivo del rechazo                                  | ¿Rebase?                                   |
| --------------------------------------------------- | ------------------------------------------ |
| `non-fast-forward`, `fetch first`, `rejected`        | Sí: el remoto tiene commits que tú no      |
| `Updates were rejected because the remote contains work that you do not have locally` | Sí, mismo caso               |
| `Permission denied` / `403` / rama protegida        | No: es política del remoto, un rebase no lo arregla |
| `Authentication failed` / `could not read Username`  | No: faltan credenciales                    |
| `remote ref does not exist` / `no upstream`         | No: hay que configurar el upstream, no rebasear |
| Red o timeout                                        | No: reintentar el mismo `git push` una vez |

Solo en el caso de `non-fast-forward`:

1. Preguntar con `question` (`multiple: false`) si quiere resolverlo con rebase.
2. Si confirma, ejecutar `git pull --rebase` y después `git push` de nuevo.
3. Si el rebase para con conflictos, detenerse: **no** resolverlos automáticamente, no usar `git rebase --abort` por cuenta propia y no hacer push. Informar de los archivos en conflicto con `git status` y preguntar al usuario cómo quiere continuar.

Límites del rebase en esta skill:

- Nunca `git push --force` como "solución" a un rechazo.
- Nunca `git rebase` sobre commits que ya estén publicados en otra rama remota compartida sin decirlo antes.
- El rebase reescribe los hashes de los commits locales: mencionarlo en la pregunta para que el usuario sea consciente.

## Selección interactiva

Todas las decisiones de esta skill se toman con la herramienta `question` (interfaz de botones), no escribiendo números en texto libre:

- Preguntas de sí/no: `multiple: false` con dos opciones, `[Y]` recomendada primero y `[n]` después.
- Selección de commits: `multiple: true`, una opción por commit con el número del plan en la etiqueta, y una primera opción "Todos los commits" para seleccionarlos de una vez.
- Título de cada pregunta: máx. 30 caracteres. Texto de la pregunta: el mensaje literal que se indica en cada paso.

## Inputs

- Ninguno obligatorio. Opcionalmente el usuario puede pedir: un scope concreto, un número máximo de commits, o un tipo de commit forzado.
- Si el repositorio está limpio, indicarlo y terminar.

## Workflow

### 1. Estado de Git

Ejecutar y sintetizar:

```bash
git status --porcelain=v1 -b
git status
git diff --stat
git diff --cached --stat
git diff --name-status
git diff --cached --name-status
git diff --diff-filter=D --name-only          # borrados
git diff --diff-filter=R --name-status        # renombrados
git ls-files --others --exclude-standard       # untracked
git log --oneline -20
git log --oneline -20 -- <archivo>            # historia reciente de un archivo
```

Clasificar cada ruta: added / modified / deleted / renamed / untracked, y staged / unstaged / ambos. Distinguir cambios ya staged de los que aún no lo están: informa de ambos, no asumas que son el mismo trabajo. Reportar además archivos ignorados relevantes si `git check-ignore` los señala.

### 2. Análisis de diffs

Para cada archivo relevante, leer el diff real (nunca solo el nombre):

- `git diff -- <archivo>` y `git diff --cached -- <archivo>` según corresponda.
- Para untracked, leer el archivo completo.
- Para binarios o diffs enormes, usar `--stat` + lectura dirigida y declararlo.

Determinar por archivo:
- Qué cambió realmente (símbolos, endpoints, campos, dependencias, flags).
- Qué funcionalidad / módulo / componente afecta.
- Clasificación: nueva funcionalidad, corrección, refactor, test, documentación, configuración, build/CI, estilo.
- Tipo de Conventional Commit y scope candidato.

Para archivos renombrados, usar `git diff -M` y tratar la pareja origen/destino como **una sola unidad lógica**.

### 3. Relaciones entre archivos

Buscar dependencias reales, no coincidencias de carpeta. Señales típicas (adaptar al lenguaje del proyecto):

- Caller → provider (controller → service, componente → servicio, cliente → API).
- Servicio → repositorio / datasource / migración de esquema.
- Entidad / modelo → repositorio, serializador, esquema.
- DTO / contrato → quien lo consume (controller, cliente, otros servicios).
- Test → clase o componente que prueba.
- Interfaz → implementación.
- Configuración / variables de entorno → funcionalidad que las consume.
- Migración → código que depende de la nueva columna/tabla.
- Manifiesto de dependencias (`pom.xml`, `build.gradle`, `package.json`, `go.mod`, `Cargo.toml`, `pyproject.toml`) → qué código usa lo añadido.
- Archivos de tooling (`.editorconfig`, `.prettierrc`, CI) → qué código pasan a cumplir.

Usar grep/glob sobre el árbol para confirmar cada relación. Una relación no confirmada se reporta como duda, no se inventa.

### 4. Agrupar y separar

Objetivo: **el menor número de commits coherentes**, no el mayor número de commits.

Unificar en un mismo grupo:
- Todo el código de una funcionalidad junto con sus tests y su configuración necesaria (regla por defecto).
- Separar por capas **solo** si el commit queda igualmente completo y revisable por sí solo (por ejemplo, un test que pasa sin el código, o un fix sobre código ya existente).

Separar en grupos distintos:
- Funcionalidades independientes aunque compartan dominio o carpeta.
- Cambios de documentación, tooling o build sin relación funcional con el código.
- Cambios que parecen accidentales, experimentales o de otra tarea (flag, depuración, `console.log`, secretos, archivos locales).

### 5. Detectar cambios dudosos

Marcar explícitamente, con motivo, todo archivo cuya pertenencia no pueda justificarse con el diff, la estructura o las dependencias: noise probable, cambios mezclados en un mismo archivo que pertenecen a dos funcionalidades, archivos generados o lockfiles, secretos potenciales. No inventar la relación: `desconocido` es una respuesta válida.

### 6. Orden

Ordenar los commits por dependencia: infraestructura/configuración → capas inferiores → capas superiores → tests → docs. Solo cuando un commit posterior realmente rompa o quede incompleto sin uno previo. Si un commit es autónomo, no forzar el orden por capas.

### 7. Preguntar si quiere commitear

Una vez entregado el plan completo, preguntar con `question` (`multiple: false`). Mostrar además este bloque en el texto de la respuesta:

```text
¿Desea Commitear los cambios?

[y]-Commitea los cambios.

¿Qué cambios desea commitear?
1-Commit-1
2-Commit-2
...

[n]-Cancelar
```

Opciones de la pregunta:

- `[Y] Commitear` (Recomendado) — pasar al paso 8.
- `[n] Cancelar` — no tocar el repositorio y pasar al paso 9.

Si el plan tiene 0 commits (árbol limpio o cambios no commiteables), no preguntar nada: informar y pasar directamente al paso 9.

### 8. Seleccionar y crear los commits

**8.1 Selección interactiva.** Preguntar con `question` (`multiple: true`) para que el usuario pulse los commits que quiera:

- Texto de la pregunta: `¿Qué cambios desea commitear?`
- Opción 1: `Todos` — selecciona todos los commits del plan.
- Una opción por commit, con el número y el mensaje: `1 — feat(book): add book search`, `2 — fix(auth): handle expired token`, etc.
- Si el usuario no pulsa ninguno, se interpreta como cancelar: no crear nada y pasar al paso 9.

**8.2 Creación.** Crear únicamente los commits seleccionados, respetando el orden `#` del plan aunque los pulse en otro orden. Si selecciona un commit pero omite otro del que depende, avisar antes de empezar.

Para cada commit seleccionado, en orden:

```bash
git add <rutas exactas del commit>
git diff --cached --stat          # verificar que coincide con lo planificado
git commit -m "<mensaje del plan>"
```

Si un commit falla, detenerse, reportar el error y no continuar con los siguientes.

### 9. Informar del estado y recomendar push

Ejecutar siempre, tanto si se han creado commits como si se han cancelado:

```bash
git status
git status -sb                  # rama y respecto a origin
git log --oneline -10
git log --oneline origin/<rama>..HEAD   # commits pendientes de push
```

Redactar un informe con:

1. **Commits creados**: hash corto y mensaje de cada uno, o `ninguno` si se canceló.
2. **Estado del árbol**: limpio o con qué archivos quedan sin commitear.
3. **Estado de la rama**: nombre, respecto a `origin` (`ahead N`, `behind N`, `diverged`, `up to date`).
4. **Commits pendientes de push**: lista, o `ninguno`.
5. **Cambios fuera de los commits**: archivos en "Cambios dudosos", no seleccionados o no commiteables.
6. **Recomendación de push**: sí o no, con el motivo concreto.

Criterios para recomendar `push`:

- **Recomendar sí** si: el árbol está limpio, todos los commits son commiteables, no hay commits dudosos pendientes y no hay Tests/build sin verificar en los commits creados.
- **Recomendar no** si: el árbol sigue sucio, queda algún commit sin crear, hay cambios dudosos, o el build está roto.
- Si la rama está `behind`, advertir que hace falta `git pull` antes de pushear, y no hacerlo automáticamente.

Después del informe, preguntar con `question` (`multiple: false`), mostrando este bloque:

```text
¿Desea hacer push de los cambios?

[Y]-Hacer push de la rama actual.

[n]-No hacer push, dejar los commits en local.
```

Opciones:

- `[Y] Hacer push` (Recomendado) — ejecutar `git push` y verificar con `git status -sb`.
- `[n] No hacer push` — terminar indicando que los commits quedan en local.

Si la rama no tiene upstream o no hay commits pendientes de push, no preguntar y explicarlo en el informe.

### 10. Push y, si falla, resolución por rebase

Ejecutar `git push` y comprobar el resultado.

**10.1 Push correcto.** Verificar con `git status -sb` que la rama queda sincronizada y mostrar los hashes subidos. Terminar.

**10.2 Push rechazado.** Leer el mensaje de error completo y clasificarlo con la tabla de "Fase de push fallido". No reintentar sin saber la causa.

- Si el motivo **no** es `non-fast-forward` (permisos, autenticación, upstream inexistente, red): informar de la causa concreta y de lo que el usuario debe hacer. No ejecutar rebase ni tocar el historial.
- Si el motivo es `non-fast-forward`: informar de que el remoto tiene commits nuevos y preguntar con `question` (`multiple: false`):

```text
El push fue rechazado: la rama remota tiene commits que no están en local.
Un rebase de tus commits locales sobre origin los reescribirá y cambiará sus hashes.

¿Desea resolverlo con rebase?

[Y]-Hacer git pull --rebase y volver a intentar el push.

[n]-Cancelar, dejar los commits en local.
```

  - `[Y] Rebase` — ejecutar `git pull --rebase`; si termina limpio, reintentar `git push` una vez y verificar con `git status -sb`. Si el rebase para con conflictos, detenerse e informar con `git status` de los archivos en conflicto, sin resolverlos, sin `git rebase --abort` y sin push; preguntar al usuario cómo continuar.
  - `[n] Cancelar` — terminar indicando que los commits quedan en local y que el remoto está adelantado.

## Output Format

Seguir exactamente esta estructura, en el idioma del usuario (por defecto español).

### Resumen

- Archivos afectados: N (X staged, Y unstaged, Z untracked)
- Grupos detectados: N
- Commits propuestos: N

### Análisis de cambios

Para cada grupo:

```text
Grupo 1 — <nombre corto de la funcionalidad>
Clasificación: <feat | fix | refactor | test | docs | chore | build | ci | style>
Archivos:
- <ruta> (staged/unstaged, added/modified/deleted/renamed)
Motivo:
<por qué estos archivos forman una unidad lógica, citando el diff>
```

### Plan de commits

Primero un **índice por tipo**, donde cada tipo es una fila y solo aparecen los que tengan commits:

```text
| Tipo | Nº | Scope | Mensaje |
|------|----|-------|---------|
| feat | 1 | book | feat(book): add book search functionality |
| fix | 2 | auth | fix(auth): handle expired token on refresh |
| test | 3 | book | test(book): add search controller tests |
| refactor | 4 | user | refactor(user): simplify user service mapping |
| docs | 5 | — | docs: update installation instructions |
```

Después la **tabla detallada**, una fila por commit, en orden de ejecución:

```text
| # | Tipo | Scope | Mensaje | Archivos | Depende de |
|---|------|-------|---------|----------|------------|
| 1 | feat | book | feat(book): add book search functionality | BookController.java, BookService.java, BookRepository.java | — |
| 2 | fix | auth | fix(auth): handle expired token on refresh | AuthService.java, AuthServiceTest.java | — |
| 3 | test | book | test(book): add search controller tests | BookControllerTest.java | 1 |
| 4 | refactor | user | refactor(user): simplify user service mapping | UserService.java | — |
| 5 | docs | — | docs: update installation instructions | README.md | — |
```

Reglas de la tabla:

- `#` es el orden de ejecución, respetando dependencias.
- `Scope` es `—` cuando el tipo no aporta contexto (típico en `docs`, `chore`, `ci`, `build`).
- `Archivos` lista todas las rutas del commit, separadas por comas.
- `Depende de` es `—` si el commit es autónomo, o `N` si necesita antes al commit N.
- Un tipo sin commits se indica con `(sin commits de este tipo)` bajo la tabla, para que quede explícito que no se olvidó.
- Los archivos que ya están staged se marcan con `(staged)` tras su nombre.

### Detalle por commit

Tras la tabla, una ficha corta por commit solo si hace falta justificarlo:

```text
Commit 1 — feat(book)
Mensaje: feat(book): add book search functionality
Archivos:
- src/main/java/.../BookController.java
- src/main/java/.../BookService.java
- src/main/java/.../BookRepository.java
- src/test/java/.../BookControllerTest.java
Motivo: única unidad funcional; controller, service, repository y su test se mantienen
juntos para que el commit sea completo y revisable por sí solo.
```

### Cambios dudosos

```text
Cambios que requieren revisión:

- <ruta>
  Motivo: <razón concreta de la incertidumbre>
  Acción sugerida: <commit propio | revisar con el autor | descartar>
```

Si no hay cambios dudosos, indicarlo explícitamente.

## Reglas de los mensajes

Conventional Commits. Tipos: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `build`, `ci`, `style`, `perf`, `revert`. `scope` solo cuando aporte información real (módulo, feature, paquete); si no, omitirlo. El mensaje describe el cambio real extraído del diff, nunca algo genérico como `feat: update application`. Si un commit mezcla tipos, dividirlo.

## Anti-patterns

- No crear un único commit genérico cuando hay cambios independientes.
- No dividir por carpetas sin relación funcional real.
- No agrupar por dominio compartido si son funcionalidades independientes.
- No separar código y sus tests salvo justificación explícita.
- No dejar commits que rompan el build o dejen la funcionalidad incompleta.
- No inventar relaciones para que el plan quede "completo".
- No preguntar por la ejecución si el plan tiene 0 commits.
- No crear commits sin confirmación explícita del usuario, aunque el plan sea válido.
- No crear commits que el usuario no haya seleccionado en la pregunta interactiva.
- No incluir en un commit archivos que no estaban en la fila correspondiente del plan.
- No saltar el informe de estado (paso 9), se haya commitado o cancelado.
- No hacer push sin confirmación explícita, ni con `--force`.
- No reintentar un `git push` rechazado sin leer antes el motivo del error.
- No hacer rebase si el rechazo no es `non-fast-forward` (permisos, auth, upstream, red).
- No resolver conflictos de rebase automáticamente, ni hacer push con el rebase a medias.
- No usar `git push --force` como solución a un rechazo.
- No hacer `git pull --rebase` sobre commits ya publicados en otra rama compartida sin avisar antes.
- No hacer `git rebase --abort` por cuenta propia.
- No hacer `git merge` ni reescribir historia para "dejarlo limpio".

## Cierre

El desenlace depende de las fases 9 y 10:

- Si en 9 el usuario elige `[n]` o no hay commits pendientes de push: indicar que los commits quedan en local, que nada se ha pusheado y listar qué commits están pendientes frente a `origin`.
- Si en 10.1 el push fue exitoso: mostrar que el push se realizó y que la rama está sincronizada con `origin` (hashes subidos).
- Si en 10.2 el push fue rechazado por otra causa distinta a `non-fast-forward`: explicar la causa y qué debe hacer el usuario. Nada se reescribe.
- Si en 10.2 el push fue rechazado por `non-fast-forward` y el usuario eligió `[n]`: indicar que los commits quedan en local y que `origin` está adelantado. Nada se reescribe.
- Si en 10.2 el push fue rechazado por `non-fast-forward` y se hizo rebase + push: indicar el resultado final y que los hashes locales fueron reescritos por el rebase.

El informe del paso 9 ya cubre los commits creados, el estado del árbol, la relación con `origin` y los cambios que quedaron fuera. En el cierre solo se confirma el desenlace.
