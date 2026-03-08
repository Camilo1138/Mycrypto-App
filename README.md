# ProyectoMD – Aplicación de Mensajería Segura

Proyecto de aplicación de mensajería desarrollado en **Java/Android** que implementa **cifrado híbrido utilizando RSA y AES** para proteger los mensajes enviados entre usuarios.

La aplicación permite enviar y recibir mensajes cifrados que se almacenan en **Firebase**, asegurando que el contenido de los mensajes esté protegido antes de ser guardado en la base de datos.

---

## Características principales

* 📩 Envío de mensajes entre usuarios
* 🔐 Cifrado de mensajes utilizando **AES**
* 🔑 Intercambio seguro de claves usando **RSA**
* ☁️ Almacenamiento de mensajes en **Firebase**
* 📱 Aplicación móvil desarrollada para **Android**

---

## Arquitectura de seguridad

La aplicación utiliza un **sistema de cifrado híbrido**, combinando RSA y AES para mejorar la seguridad y eficiencia.

### Proceso de cifrado

1. El mensaje del usuario se cifra utilizando **AES** (cifrado simétrico).
2. La clave AES utilizada para cifrar el mensaje se cifra utilizando **RSA** (clave pública del receptor).
3. El mensaje cifrado y la clave AES cifrada se envían y almacenan en **Firebase**.

### Proceso de descifrado

1. El receptor obtiene el mensaje desde Firebase.
2. La clave AES es descifrada utilizando su **clave privada RSA**.
3. Con la clave AES recuperada se descifra el mensaje original.

Este enfoque permite:

* Mayor **seguridad**
* Mejor **rendimiento** que usar solo RSA
* Protección de datos incluso si la base de datos es comprometida

---

## Tecnologías utilizadas

* **Java**
* **Android SDK**
* **Firebase Realtime Database / Firestore**
* **RSA Encryption**
* **AES Encryption**

---

## Estructura general del proyecto

El proyecto está organizado en diferentes módulos que manejan:

* Interfaz de usuario
* Lógica de mensajería
* Procesos de cifrado y descifrado
* Conexión con Firebase

```
app
 ├── activities
 ├── encryption
 │    ├── RSA
 │    └── AES
 ├── firebase
 └── models
```

---

## Instalación

1. Clonar el repositorio

```
git clone https://github.com/Camilo1138/ProyectoMD
```

2. Abrir el proyecto en **Android Studio**

3. Configurar Firebase:

   * Crear un proyecto en Firebase
   * Descargar el archivo `google-services.json`
   * Colocarlo en la carpeta `app/`

4. Ejecutar la aplicación en un emulador o dispositivo Android.

---

## Uso de la aplicación

1. Crear o iniciar sesión en una cuenta.
2. Seleccionar un usuario para enviar mensajes.
3. Escribir el mensaje.
4. El mensaje se cifra automáticamente antes de enviarse.
5. El receptor lo descifra al recibirlo.

---

## Objetivo del proyecto

Este proyecto fue desarrollado con fines académicos para explorar:

* **Criptografía aplicada**
* **Comunicación segura**
* **Desarrollo de aplicaciones Android**
* **Uso de Firebase como backend**

---

## Autor

Camilo Arboleda
