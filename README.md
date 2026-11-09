# NutriVi 

**NutriVi** es una aplicación móvil desarrollada para Android orientada a la salud, nutrición y bienestar personal. Permite a los usuarios llevar un control diario de su ingesta calórica, consumo de agua, conteo de pasos en tiempo real y compartir recetas saludables con la comunidad.

---

##  Características Principales

- **Autenticación Segura:**
  - Inicio de sesión y registro mediante correo electrónico y contraseña (Firebase Authentication).
  - Inicio de sesión rápido con Google utilizando la API moderna de **Android Credential Manager**.
- **Panel Principal (Dashboard):**
  - Indicador circular animado de progreso de calorías diarias.
  - Tarjeta interactiva de consumo de agua con control de vasos diarios.
  - Contador de pasos en segundo plano a través de un servicio dedicado (`StepCounterService`).
- **Comunidad de Recetas:**
  - Visualización de recetas saludables publicadas por otros usuarios mediante un `RecyclerView` optimizado.
- **Navegación Fluida:**
  - Barra de navegación inferior (*BottomNavigationView*) con diseño moderno de Material Design 3.

---

##  Tecnologías y Arquitectura

- **Lenguaje:** Java / Kotlin
- **Interfaz de Usuario:** XML Layouts, Material Design 3, CoordinatorLayout, Fragments.
- **Base de Datos y Backend:** Firebase Cloud Firestore & Firebase Auth.
- **Librerías Jetpack:**
  - `androidx.credentials:credential-manager` (Google Sign-In)
  - `androidx.recyclerview:recyclerview`
  - `com.google.android.material:material`

---

##  Configuración e Instalación local

Para clonar y poner en marcha este proyecto en tu entorno local:

1. **Clona el repositorio:**
   ```bash
   git clone https://github.com/pandekechu/nutrivi.git
   ```
2. **Configura Firebase:**
   - Debido a políticas de seguridad, el archivo `google-services.json` no se incluye en el repositorio público.
   - Coloca tu propio archivo `google-services.json` (generado desde tu consola de Firebase) dentro de la carpeta `app/`.
3. **Sincroniza y Compila:**
   - Abre el proyecto en **Android Studio**.
   - Haz clic en **Sync Project with Gradle Files**.
   - Ejecuta la aplicación en un emulador o dispositivo físico.

---

##  Próximas Mejoras (Roadmap)

- [Listo] **Escaneo de Alimentos por Cámara (`CameraActivity`):** Detección e integración fotográfica para registro rápido de comidas.
- [ ] **Perfil de Usuario Personalizable:** Pantalla de perfil para editar metas de calorías, agua y datos personales sincronizados con Firestore.
- [listo] **Notificaciones Push:** Recordatorios diarios para beber agua y registrar comidas.
- [ ] **Temas:** Soporte completo para temas claros y oscuros en toda la app.
- [ ] **Diseños:** Apartado donde se vean datos diario, semanal y mensual tanto de agua y pasos, también un apartado de la comunidad más sencilla.
- [ ] **Administrador:** revisión de recetas o publicaciones de los usuarios que reporten o contengan temas sensibles.
- [ ] **Automatización:** AI que filtra publicaciones para que no se comparta información sensible y contenido desnudos(partes intimas).
- [ ] **Base de comidas:** subir a la base de datos tanto comida y bebidas de todo tipo.
- [ ] **metodo de pago:** coach personalizado para cada usuario.

