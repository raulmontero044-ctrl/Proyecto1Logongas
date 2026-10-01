---
name: git-commit-planner
description: Analyze uncommitted Git changes (staged, unstaged, untracked, deleted, renamed) and propose a plan of atomic, coherent Conventional Commits grouped by logical functionality. Use when asked to plan commits, split changes into commits, organize pending work for commit, or review dirty state before committing. Read-only - never commits.
---

# git-commit-planner

Analiza el estado actual de Git y produce un **plan de commits** atómicos y coherentes.

## Límite absoluto (solo lectura)

Esta skill NO modifica el repositorio. Prohibido:

- `git add`, `git commit`, `git commit -a`, `git stash`, `git restore`, `git checkout --`
- `git reset` (incluido `--soft`, `--mixed`, `--hard`)
- `git clean`, `git rm`, `git mv`, `git revert`
- Editar, crear, renombrar o borrar cualquier archivo
- `git pull`, `git push`, `git merge`, `git rebase`, `git cherry-pick`, `git am`

Comandos permitidos: `git status`, `git diff`, `git log`, `git show`, `git ls-files`, `git rev-parse`, `git cat-file`, `git blame`, `git check-ignore`, `git stash list` (todos solo lectura). Cualquier otro comando de escritura se omite y se reporta en "Cambios dudosos".

El único resultado es el plan. El usuario decide si lo ejecuta.

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

```text
Commit 1
Tipo: feat
Scope: book
Mensaje:
feat(book): add book search functionality

Archivos:
- src/.../BookController.java
- src/.../BookService.java
- src/.../BookRepository.java
- src/.../BookControllerTest.java

Motivo:
<unidad funcional, dependencias mantenidas juntas>
Depende de: <commit N | ninguno>
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

## Cierre

Tras el plan, indicar de forma explícita: **no se ha modificado nada ni se ha creado ningún commit**; los comandos `git add` exactos quedan en manos del usuario.
