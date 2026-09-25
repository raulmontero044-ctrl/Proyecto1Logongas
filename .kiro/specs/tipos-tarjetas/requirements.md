# Requirements Document

## Project Description (Input)
Creación de la entidad "Tarjetas", que representa las tarjetas de débito emitidas a los clientes. Cada tarjeta tiene un identificador único, el nombre del propietario y un tipo de tarjeta referenciado al catálogo "TiposDeTarjetas" (tabla de tipos de tarjetas bancarias con identificador y nombre).

## Boundary Context
- **In scope**: Creación de tarjetas indicando el propietario y su tipo; consulta del tipo de una tarjeta; identificador único por tarjeta; validaciones de nombre obligatorio y tipo opcional.
- **Out of scope**: Activación, bloqueo, establecimiento y solicitud de la clave de la tarjeta; pagos con tarjeta; operaciones monetarias (ingresos, retiradas, transferencias) y creación/eliminación de cuentas. Estas funcionalidades pertenecen a otras specs del producto.
- **Adjacent expectations**: Se asume la existencia del catálogo "TiposDeTarjetas", consultado como referencia para indicar el tipo de cada tarjeta. Las tarjetas solo podrán registrarse con tipos existentes en dicho catálogo.

## Requirements

### 1. Creación de tarjeta
**Objective:** Como banquero, quiero crear tarjetas de débito indicando el propietario y su tipo, para poder emitirlas a los clientes.

#### Acceptance Criteria
1. When el banquero solicita crear una tarjeta con el nombre del propietario y un tipo válido, el sistema shall crear la tarjeta.
2. When el sistema crea una tarjeta, esta shall quedar asociada a su tipo mediante una referencia al catálogo TiposDeTarjetas.
3. If el nombre del propietario está vacío, el sistema shall rechazar la creación de la tarjeta y mostrar un mensaje de error.
4. If el tipo indicado no existe en el catálogo TiposDeTarjetas, el sistema shall rechazar la creación de la tarjeta y mostrar un mensaje de error.

### 2. Identificador único de tarjeta
**Objective:** Como banquero, quiero que cada tarjeta tenga un identificador único, para poder distinguirla de las demás.

#### Acceptance Criteria
1. The sistema shall asignar a cada tarjeta registrada un identificador único.
2. If se intenta registrar una tarjeta cuyo identificador ya existe, el sistema shall rechazar el registro y mostrar un mensaje de error.

### 3. Consulta del tipo de tarjeta
**Objective:** Como cliente o banquero, quiero consultar el tipo de una tarjeta, para saber si es de débito o de crédito.

#### Acceptance Criteria
1. When se consulta una tarjeta existente, el sistema shall mostrar el tipo de tarjeta asociado.
2. The sistema shall mostrar el tipo de tarjeta en MAYÚSCULAS y en negrita.

### 4. Validaciones del tipo de tarjeta
**Objective:** Como banquero, quiero que el tipo de tarjeta se registre de forma coherente, para mantener la consistencia del catálogo.

#### Acceptance Criteria
1. While se está creando una tarjeta, el sistema shall aceptar el tipo de tarjeta ignorando mayúsculas y minúsculas.
2. The sistema shall almacenar un tipo de tarjeta con el tamaño suficiente para los valores 'Débito' y 'Crédito'.
3. The sistema shall permitir que el tipo de tarjeta sea opcional al crear la tarjeta.