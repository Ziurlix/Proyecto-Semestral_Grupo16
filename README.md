# Logística en Terreno — Ventas y Distribución (Grupo 16)

MVP académico en Android para la gestión de ventas, pedidos, rutas y entregas en terreno (caso DSY1105). Desarrollado con Jetpack Compose (Material 3), arquitectura MVVM, almacenamiento local Room (SQLite) con enfoque Offline-First, sincronización remota mediante Retrofit, integración con Google Sign-In / Credential Manager y módulo nativo C++ (JNI/NDK).

---

## Requisitos del Sistema

- Android Studio (Ladybug / Jellyfish o versión reciente con soporte Gradle Wrapper).
- JDK 17 (configurado mediante Gradle Toolchain).
- Android SDK:
  - `compileSdk`: 35
  - `targetSdk`: 35
  - `minSdk`: 24 (Android 7.0+)

---

## Instrucciones de Ejecución

1. Clonar o descargar el repositorio e importar la carpeta `Proyecto-Semestral_Grupo16-main` en Android Studio.
2. Esperar a que finalice la sincronización del Gradle Sync.
3. Seleccionar la configuración de ejecución `app`.
4. Ejecutar en un emulador o dispositivo Android físico (API 24 o superior).

---

## Cuentas de Prueba y Autenticación

Todas las cuentas predefinidas utilizan la contraseña `111111`:

| Perfil / Rol | Correo electrónico de prueba | Permisos principales |
|---|---|---|
| Administrador | `admin@guardian.test` / `admin@gmail.test` | Acceso global a Resumen, Pedidos, Rutas, Entregas y Perfil. |
| Supervisor | `supervisor@gmail.test` | Acceso a Resumen, Pedidos, Dashboard de Ventas, Rutas y Perfil. |
| Vendedor | `vendedor@gmail.test` | Resumen, Creación y gestión de Pedidos, Directorio (Clientes y Productos), Ventas y Perfil. |
| Repartidor | `operador@gmail.test` | Resumen, Rutas asignadas, Gestión de Entregas (cambio de estados) y Perfil. |

### Caracterizaciones de Autenticación
- Creación de Cuentas: Permite registrar nuevos usuarios definiendo correo, contraseña y el rol activo inicial.
- Inicio de Sesión con Google: Integración con `Credential Manager` y `Google Sign-In API`, incluyendo un selector directo para cuentas con autenticación Google previa.
- Seguridad: Sistema de bloqueo automático tras 3 intentos fallidos de contraseña.
- Persistencia: El rol y la sesión activa se almacenan localmente en `SharedPreferences` (`ventas_distribucion_store`).

---

## Funcionalidades Principales

### Vistas e Interfaz según Rol
- Resumen (Dashboard): Tarjetas e indicadores sintéticos de pedidos activos, entregas en proceso y métricas de ventas.
- Pedidos: Gestión de pedidos con filtros interactivos por estado (`Pendiente`, `En preparación`, `En despacho`, `Entregado`), mes y selector de fecha (`DatePicker`). Los vendedores pueden crear nuevos pedidos mediante un modal contextual (`CreateOrderDialog`).
- Directorio: Consulta rápida del catálogo de productos (categorías, precio, stock) y ficha de clientes (empresa, teléfono, dirección).
- Ventas: Gráficos e indicadores de ventas generadas. Filtra automáticamente las ventas asociadas al vendedor en sesión o muestra la métrica global para supervisores.
- Rutas: Visualización de zonas de entrega, chofer asignado y estado de avance de las rutas.
- Entregas: Control de despacho para el perfil Repartidor con actualización de estados en tiempo real (`Pendiente` → `En ruta` → `Entregado`).
- Perfil y Ajustes: Configuración de tema (Modo Oscuro / Modo Claro), consulta de historial por tipo de usuario y cierre de sesión seguro.

### Arquitectura Offline-First y Room DB
- Base de Datos Local (`AppDatabase` v3): Almacenamiento persistente SQLite gestionado con Room (`OperationsDao`), que mantiene sincronizados las entidades de `Order` y `Delivery`.
- Estado Reactivo: UI impulsada por `StateFlow` y `Flow` de Kotlin Coroutines para actualización inmediata de la interfaz ante cambios en la base de datos.
- Motor de Sincronización Backend:
  - Detección activa de red mediante `ConnectivityManager`.
  - Cuando existe conexión a Internet, la app sincroniza pedidos creados y cambios de estado pendientes (`isSyncPending`) de manera transparente hacia la API REST.
  - En caso de trabajar sin conexión, la app preserva los cambios de forma local y los envía automáticamente en la siguiente sincronización.

---

## Conexión con API REST (Retrofit)

La aplicación opera por defecto en modo resiliencia / demostración utilizando datos locales sintéticos (`MockDataSource`). Para vincular un backend real, se puede configurar la propiedad `API_BASE_URL` en `gradle.properties`:

```properties
API_BASE_URL=https://tu-servidor-api.com/
```

### Endpoints Expuestos (`ApiService.kt`):
- `GET /api/v1/products` — Consulta de catálogo de productos.
- `GET /api/v1/clients` — Lista de clientes registrados.
- `GET /api/v1/orders` — Consulta de lista de pedidos.
- `POST /api/v1/orders` — Registro de nuevo pedido (`CreateOrderRequest`).
- `GET /api/v1/deliveries` — Consulta de entregas.
- `GET /api/v1/sales` — Registro de ventas realizadas.
- `GET /api/v1/routes` — Hojas de ruta logísticas.
- `GET /api/v1/indicators` — Métricas e indicadores.
- `PUT /api/v1/deliveries/{id}/status` — Actualización del estado de entrega (`DeliveryStatusRequest`).

---

## Estructura del Proyecto

```text
com.example
├── data
│   ├── local          # Base de datos Room (AppDatabase, OperationsDao), LocalStore y MockData
│   ├── model          # Modelos de dominio (Order, Delivery, Sale, Client, Product, DeliveryRoute)
│   ├── remote         # Retrofit, ApiService, DTOs y client builder
│   └── repository     # OperationsRepository (Offline-First sync engine)
├── repository         # AuthRepository (Cuentas predefinidas, dinámicas y Google Sign-In)
├── ui
│   ├── components     # Componentes Compose (CreateOrderDialog, OrderCard, DeliveryCard, etc.)
│   ├── screens        # Pantallas (Orders, Deliveries, Sales, Directory, Profile, Routes, Summary)
│   ├── theme          # Paleta de colores, tipografía y tema Material 3 (AppTheme)
│   └── utils          # Formateadores, utilidades de color y WindowSize
├── viewmodel          # DashboardViewModel y LoginViewModel
└── MainActivity.kt    # Punto de entrada con NavHost y configuración Edge-to-Edge
```

### Módulo Nativo C++ (JNI / NDK)
El proyecto incluye un módulo nativo configurado mediante CMake (`CMakeLists.txt`) y código C++ (`appworkchat_grupo16.cpp`), demostrando interoperabilidad nativa en la arquitectura.

---

## Seguridad y Datos

Todos los registros (nombres, clientes, direcciones, correos y contraseñas) corresponden a datos ficticios de demostración académica. No se utilizan credenciales reales ni recursos productivos.
