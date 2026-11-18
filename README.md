# Ventas y Distribución — Grupo 16

MVP académico Android para el caso DSY1105. Incluye autenticación por perfiles, panel de indicadores, clientes, productos, pedidos, rutas/entregas, persistencia local y capa Retrofit para conexión con API REST.

## Requisitos

- Android Studio compatible con el Gradle Wrapper incluido.
- JDK 17.
- Android SDK API 37 instalado (o ajustar `compileSdk` y `targetSdk` en `app/build.gradle.kts` a una versión instalada).

## Ejecutar

1. Abrir la carpeta `Proyecto-Semestral_Grupo16-main` desde Android Studio.
2. Esperar la sincronización de Gradle.
3. Ejecutar la configuración `app` en un emulador o dispositivo Android.

## Cuentas ficticias

Todas las cuentas usan la contraseña `123456`:

| Perfil inicial | Correo |
|---|---|
| Administrador | `admin@guardian.test` |
| Supervisor | `supervisor@guardian.test` |
| Vendedor | `vendedor@guardian.test` |
| Repartidor | `operador@guardian.test` |

El administrador puede cambiar el rol de las otras cuentas desde la pestaña **Usuarios**. El perfil Supervisor no tiene acceso a la administración de usuarios. La selección se guarda en el dispositivo y se aplica en el siguiente inicio de sesión.

## Funcionalidades implementadas

- Login local de prueba con bloqueo tras tres contraseñas incorrectas.
- Panel con indicadores sintéticos de pedidos, entregas y ventas.
- Consulta de clientes y catálogo de productos.
- Registro de pedidos por el perfil vendedor, con guardado local y envío `POST` cuando la API está disponible.
- Consulta de entregas y transición de estado `Pendiente` → `En ruta` → `Entregado` por el perfil repartidor.
- Persistencia local de pedidos, estados de entrega y roles mediante `SharedPreferences`.
- MVVM con `ViewModel`, `viewModelScope` y estado observable por Compose.
- Retrofit con operaciones REST para productos, clientes, pedidos, ventas, rutas, entregas e indicadores; incluye creación de pedidos y actualización de estados.

## Conexión con API REST

El caso entregado no especifica la URL del servidor ni credenciales. Por eso la app abre en **modo demostración**, con datos sintéticos, y realiza el intento de sincronización Retrofit con una URL de ejemplo. Para conectar el backend real, configurar la propiedad `API_BASE_URL` en `gradle.properties` o al ejecutar Gradle:

```properties
API_BASE_URL=https://tu-servidor-de-prueba/
```

La API debe exponer estas rutas y responder JSON compatible con los DTO de `app/src/main/java/com/example/data/remote/ApiService.kt`:

- `GET /api/v1/products`
- `GET /api/v1/clients`
- `GET /api/v1/orders`
- `POST /api/v1/orders` con los campos `id`, `clientName`, `productName`, `quantity`, `priority`, `status` y `date`.
- `GET /api/v1/deliveries`
- `GET /api/v1/sales`
- `GET /api/v1/routes`
- `GET /api/v1/indicators`
- `PUT /api/v1/deliveries/{id}/status` con cuerpo `{ "status": "En ruta" }` o `{ "status": "Entregado" }`

Si el servidor no está disponible o las rutas no coinciden, la aplicación conserva los datos sintéticos y los cambios locales. Esto permite probar la navegación y los flujos sin usar información real. La conexión real debe verificarse con la URL entregada por el docente o el backend del equipo.

## Datos y seguridad

Todos los nombres, pedidos, direcciones, montos y usuarios son ficticios. Las credenciales están incluidas únicamente para demostración académica; no deben reutilizarse en producción. La app no utiliza credenciales, tokens ni registros productivos.

## Arquitectura

```text
com.example
├── data
│   ├── local          # Persistencia local
│   ├── model          # Modelos de dominio
│   ├── remote         # Retrofit, DTO y endpoints
│   └── repository     # Coordinación de datos local/remoto
├── repository         # Autenticación de prueba
├── ui                 # Login y panel Compose
└── viewmodel          # Estado y lógica de presentación
```

## Alcance pendiente de validar

- Reemplazar la URL de ejemplo por la URL real de la API REST.
- Confirmar que las rutas y nombres de campos coinciden con el backend acordado.
- Ejecutar pruebas en el emulador/dispositivo y documentar capturas de la demostración.
