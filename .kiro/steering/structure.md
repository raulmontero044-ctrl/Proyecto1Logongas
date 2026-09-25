# Project Structure

## Organization Philosophy

El proyecto se organiza separando el backend y el frontend.

El backend está desarrollado con Java y Spring Boot y sigue una arquitectura por capas, separando la lógica de negocio, el acceso a datos y la exposición de la API.

El frontend está desarrollado con Angular y se organiza por funcionalidades, manteniendo separada la interfaz de usuario de la lógica de comunicación con el backend.

Las funcionalidades principales del banco se organizan alrededor de las entidades y operaciones del dominio, como clientes, cuentas bancarias, tarjetas de débito y operaciones bancarias.

## Directory Patterns

### Backend

**Location**: `/backend/`  
**Purpose**: Contiene toda la aplicación Java con Spring Boot.  
**Example**: Controladores REST, servicios, entidades, repositorios y configuración.

El backend se organiza por funcionalidades:

/backend/
└── src/
    ├── main/
    │   └── java/
    │       └── .../
    │           ├── cliente/
    │           ├── cuenta/
    │           ├── tarjeta/
    │           ├── movimiento/
    │           └── transferencia/
    │
    └── test/
        └── java/
            └── .../
                ├── cliente/
                ├── cuenta/
                ├── tarjeta/
                ├── movimiento/
                └── transferencia/

Cada funcionalidad contiene únicamente las capas que necesita:

<feature>/
├── controller/
├── service/
├── repository/
├── entity/
└── dto/

No es obligatorio que todos los features tengan todas las carpetas. Si una funcionalidad no necesita una determinada capa, no se debe crear innecesariamente.

### Backend Features
**Location**: `/backend/src/main/java/.../<feature>/`
**Purpose**: Contiene todos los elementos relacionados con una funcionalidad concreta del banco.
**Example**: /cuenta/
├── controller/
│   └── CuentaController.java
├── service/
│   └── CuentaService.java
├── repository/
│   └── CuentaRepository.java
├── entity/
│   └── CuentaBancaria.java
└── dto/
    ├── CuentaRequest.java
    └── CuentaResponse.java

Las funcionalidades principales pueden seguir esta estructura:

/cliente/
├── controller/
├── service/
├── repository/
├── entity/
└── dto/

/cuenta/
├── controller/
├── service/
├── repository/
├── entity/
└── dto/

/tarjeta/
├── controller/
├── service/
├── repository/
├── entity/
└── dto/

/movimiento/
├── controller/
├── service/
├── repository/
├── entity/
└── dto/

/transferencia/
├── controller/
├── service/
├── repository/
├── entity/
└── dto/

### Controllers

**Location**: `/backend/src/main/java/.../<feature>/controller/`  
**Purpose**: Contiene los controladores REST responsables de recibir las peticiones del frontend y devolver las respuestas correspondientes. No deben contener lógica de negocio compleja.  
**Example**: Controladores relacionados con cuentas bancarias, tarjetas de débito y operaciones bancarias.

### Services

**Location**: `/backend/src/main/java/.../<feature>/service/`  
**Purpose**: Contiene la lógica de negocio de la aplicación. Las operaciones como ingresos, retiradas, transferencias y gestión de tarjetas deben resolverse en esta capa.  
**Example**: Servicios para gestionar cuentas, tarjetas y transferencias.

### Repositories

**Location**: `/backend/src/main/java/.../<feature>/repository/`  
**Purpose**: Contiene los componentes responsables de acceder a los datos persistidos mediante JPA.  
**Example**: Repositorios para cuentas bancarias, clientes y tarjetas.

### Entities

**Location**: `/backend/src/main/java/.../<feature>/entity/`  
**Purpose**: Contiene las entidades del dominio que se persisten en la base de datos mediante JPA.  
**Example**: Cliente, CuentaBancaria, TarjetaDebito y Movimiento.

### DTOs

**Location**: `/backend/src/main/java/.../<feature>/dto/`  
**Purpose**: Contiene los objetos utilizados para transportar datos entre la API y el resto de la aplicación, evitando exponer directamente las entidades cuando no sea necesario.  
**Example**: Datos necesarios para realizar una transferencia o crear una cuenta.

### Frontend

**Location**: `/frontend/`  
**Purpose**: Contiene la aplicación Angular utilizada por clientes y banqueros.  
**Example**: Componentes, servicios, modelos y vistas de la aplicación bancaria.

### Frontend Features
**Location**: `/frontend/src/app/<feature>/`
**Purpose**: Contiene todos los elementos necesarios para implementar una funcionalidad concreta en Angular.
**Example**: <feature>/
├── components/
├── pages/
├── services/
├── models/
└── <feature>.routes.ts

Ejemplo:

/cuenta/
├── components/
│   └── cuenta-card/
├── pages/
│   ├── cuenta-list/
│   └── cuenta-detail/
├── services/
│   └── cuenta.service.ts
├── models/
│   └── cuenta.model.ts
└── cuenta.routes.ts

Las demás funcionalidades seguirán el mismo patrón:

/cliente/
├── components/
├── pages/
├── services/
├── models/
└── cliente.routes.ts

/tarjeta/
├── components/
├── pages/
├── services/
├── models/
└── tarjeta.routes.ts

/movimiento/
├── components/
├── pages/
├── services/
├── models/
└── movimiento.routes.ts

/transferencia/
├── components/
├── pages/
├── services/
├── models/
└── transferencia.routes.ts

No es obligatorio crear todas las carpetas en cada feature. Solo deben existir las que sean necesarias.

### Angular

**Location**: `/frontend/src/app/`  
**Purpose**: Las funcionalidades del frontend deben organizarse por áreas funcionales de la aplicación.  
**Example**: cuentas, tarjetas, transferencias y operaciones bancarias.

### Tests

**Location**: Junto a la parte del proyecto que se está probando o en las ubicaciones establecidas por cada framework.  
**Purpose**: Contiene las pruebas unitarias, de integración y de API.  
**Example**: Tests con JUnit 5, Mockito, REST Assured y Pit (Mutation Testing).

## Naming Conventions

- **Files**: Los nombres deben seguir las convenciones establecidas por el lenguaje y framework utilizado.
- **Components**: Los componentes de Angular deben utilizar nombres descriptivos y seguir las convenciones de Angular.
- **Functions**: Utilizar nombres descriptivos en `camelCase`.

En Java, las clases deben utilizar `PascalCase`.

## Import Organization

Los imports deben mantenerse organizados y separados de forma consistente.

En Java se deben utilizar los imports necesarios de forma explícita y evitar imports que no se utilicen.

En Angular/TypeScript se deben priorizar los imports mediante los alias de ruta configurados cuando estén disponibles y utilizar imports relativos para elementos del mismo módulo o funcionalidad.

**Path Aliases**:
- `@/`: Se utilizará para referenciar rutas internas del proyecto cuando el alias esté configurado.

## Code Organization Principles

- Los controladores REST deben encargarse únicamente de recibir peticiones, validar los datos necesarios a nivel de entrada y delegar las operaciones a los servicios.
- La lógica de negocio debe permanecer en la capa de servicios.
- El acceso a la base de datos debe realizarse mediante repositorios y JPA.
- Los controladores no deben acceder directamente a los repositorios.
- Las entidades representan los datos persistidos y no deben utilizarse como sustituto de los DTOs cuando sea necesario controlar los datos expuestos por la API.
- Las operaciones bancarias deben realizarse mediante la capa de servicios para mantener centralizadas las reglas de negocio.
- El frontend Angular debe comunicarse con el backend mediante la API REST.
- La lógica de presentación debe permanecer en Angular y la lógica de negocio bancaria debe permanecer en el backend.
- Las funcionalidades nuevas deben seguir los patrones existentes en lugar de crear estructuras diferentes para cada funcionalidad.
- La estructura debe permitir mantener separadas las responsabilidades del cliente y del banquero.
- Las reglas de arquitectura deben poder comprobarse mediante ArchUnit cuando sea apropiado.

---
_Document patterns, not file trees. New files following patterns shouldn't require updates_