# My CryptoApp

Aplicación Android de mensajería cifrada desarrollada en Java. Permite registrar e iniciar sesión con Firebase Authentication, consultar usuarios, mantener conversaciones y enviar mensajes protegidos mediante cifrado híbrido con RSA y AES.

> Proyecto académico: la implementación sirve para estudiar criptografía aplicada y comunicación segura. Antes de utilizarlo en producción se deben revisar las reglas de seguridad, la gestión de claves, la autenticación y la distribución de claves privadas.

## Funcionalidades

- Registro e inicio de sesión con correo y contraseña.
- Lista de usuarios y conversaciones registradas.
- Envío y recepción de mensajes en tiempo real mediante Cloud Firestore.
- Cifrado híbrido de cada mensaje:
  - AES-GCM para el contenido del mensaje.
  - RSA para cifrar la clave AES.
- Almacenamiento seguro local de la clave privada mediante Android Keystore.
- Recuperación de respaldo de la clave privada desde Firestore.
- Tokens de Firebase Cloud Messaging asociados a cada usuario.
- Notificaciones locales al recibir mensajes remotos.
- Registro de fallos con Firebase Crashlytics.

## Seguridad implementada

1. Al registrar un usuario se generan componentes RSA personalizados y se construye un par de claves pública y privada.
2. La clave pública se publica en el documento del usuario para permitir que otros usuarios cifren mensajes dirigidos a él.
3. La clave privada se guarda localmente con Android Keystore y también se conserva cifrada como respaldo en Firestore.
4. Para cada mensaje se genera una clave AES diferente.
5. El texto se cifra con `AES/GCM/NoPadding`.
6. La clave AES se cifra con la clave pública RSA del receptor.
7. Firestore almacena el contenido cifrado, la clave AES cifrada y los metadatos necesarios.
8. El receptor descifra la clave AES con su clave privada RSA y después descifra el mensaje.

## Tecnologías

- Java 11
- Android SDK 35
- Android Gradle Plugin 8.7.2
- AndroidX AppCompat, Activity y ConstraintLayout
- Material Components
- Firebase Authentication
- Cloud Firestore
- Firebase Cloud Messaging
- Firebase Functions
- Firebase Crashlytics
- Android Security Crypto
- Gradle Wrapper

## Requisitos

- Android Studio con SDK de Android 35.
- JDK compatible con Java 11.
- Un proyecto de Firebase.
- Android SDK API 28 o superior en el emulador o dispositivo.
- Conexión a Internet.

## Configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/Camilo1138/Mycrypto-App.git
cd Mycrypto-App
```

### 2. Abrir Android Studio

Selecciona **Open** y abre la carpeta raíz del proyecto.

Android Studio descargará las dependencias de Gradle durante la primera sincronización.

### 3. Configurar Firebase

1. Crea un proyecto en Firebase.
2. Registra una aplicación Android con el identificador:

   ```text
   com.example.myapplication
   ```

3. Descarga el archivo `google-services.json`.
4. Colócalo en la carpeta `app/`.
5. Habilita en Firebase Console:
   - Authentication con el proveedor **Email/Password**.
   - Cloud Firestore.
   - Cloud Messaging.
   - Crashlytics, si deseas recopilar informes de fallos.

El proyecto ya contiene la configuración de ejemplo de Firebase en `app/google-services.json`. Debes sustituirla por la configuración de tu propio proyecto antes de ejecutar la aplicación.

### 4. Configurar las colecciones de Firestore

La aplicación utiliza las siguientes referencias principales:

- `usuarios`: perfil de usuario, clave pública y privada cifrada, correo y token FCM.
- `chats`: identificadores de los participantes y datos generales de la conversación.
- `chats/{chatId}/mensajes`: contenido cifrado, clave cifrada, remitente y fecha.

Los nombres exactos y las reglas de acceso dependen de la estructura almacenada por la aplicación. Para un entorno real, configura reglas de Firestore que impidan que usuarios no autorizados puedan leer o modificar documentos ajenos.

### 5. Configurar la función de notificaciones

La aplicación invoca una función HTTPS de Firebase llamada `sendPushNotification`. Esta función no está incluida en este repositorio y debe implementarse y desplegarse en tu proyecto de Firebase antes de usar las notificaciones remotas.

### 6. Ejecutar

Desde Android Studio, selecciona un dispositivo físico o un emulador con API 28 o superior y ejecuta la configuración `app`.

También puedes compilar desde la raíz del proyecto:

**Windows**

```bat
gradlew.bat assembleDebug
```

**Linux/macOS**

```bash
./gradlew assembleDebug
```

El APK de prueba se genera en `app/build/outputs/apk/debug/app-debug.apk`.

## Uso

1. Crea una cuenta desde la pantalla de registro.
2. Inicia sesión con el correo y la contraseña.
3. Selecciona un usuario de la lista.
4. Escribe un mensaje.
5. El remitente cifra el mensaje antes de guardarlo en Firestore.
6. El receptor descifra el contenido con su clave privada.

## Estructura del proyecto

```text
app/
├── src/main/java/com/example/myapplication/
│   ├── activities/   Pantallas y navegación
│   ├── adapters/     Adaptadores de usuarios, chats y mensajes
│   ├── models/       Modelos usados por la interfaz y Firestore
│   └── utils/        RSA, AES, almacenamiento seguro y FCM
└── src/main/res/     Layouts, recursos gráficos y configuración visual
```

```text
build.gradle.kts
settings.gradle.kts
gradle/libs.versions.toml
gradlew / gradlew.bat
app/google-services.json
```

## Pruebas

El proyecto incluye plantillas de pruebas unitarias e instrumentadas:

```bash
./gradlew test
./gradlew connectedAndroidTest
```

`connectedAndroidTest` requiere un dispositivo o emulador conectado.
