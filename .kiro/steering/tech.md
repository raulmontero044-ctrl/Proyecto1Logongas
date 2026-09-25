# Technology Stack

## Architecture

Aplicacion en Java con Spring Boot y Angular en cliente.

## Core Technologies

- **Language**: Java 25, TypeScript: 5.9.x, HTML y CSS
- **Framework**: Spring Boot, bootstrap, JPA, Angular
- **Runtime**: JDK

## Key Libraries

Spring Boot: 4.0.x
bootstrap
JPA
Angular: 21.x

## Development Standards

### Type Safety
Alto nivel de seguridad con los tipos

### Code Quality
Usa en Java: ArchUnit, Google Error Prone

### Testing
Usa en Java:JUnit 5, Mockito, REST Assured, Pit (Mutation testing)

## Development Environment

### Required Tools
gradle: 9.x con los scripts en kotlin
MySQL: 8.4 (instancia local, sin Docker)
JDK: 25 (Temurin)

### Common Commands

    * Compilar, Test y Desplegar:
```bash
./run.sh
```

    * Backend (build y test):
```bash
cd backend && ./gradlew build
```

## Key Technical Decisions

Base de datos MySQL 8.4 en instancia local (127.0.0.1:3307, base de datos `banco`, usuario `banco`). Sin Docker.

---
_Document standards and patterns, not every dependency_
