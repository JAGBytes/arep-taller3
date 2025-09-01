# Framework Web en Java para Servicios REST con Anotaciones y Reflexión

Este proyecto implementa un **framework web completo en Java** que evoluciona desde un servidor web básico hacia una plataforma robusta para el desarrollo de aplicaciones web con servicios REST backend. El framework ahora incluye **sistema de anotaciones**, **carga automática de componentes mediante reflexión**, y **arquitectura MVC moderna**.

## 🆕 Nuevas Características Agregadas

### **Sistema de Anotaciones Personalizado**

- **`@RestController`**: Marca clases como controladores REST
- **`@GetMapping`**: Define endpoints GET con rutas personalizadas
- **`@RequestParam`**: Extrae parámetros de consulta automáticamente

### **Carga Automática de Componentes**

- **Reflection-based loading**: Carga automática de controladores usando la librería Reflections
- **Auto-registro de endpoints**: Los métodos anotados se registran automáticamente
- **Inyección de parámetros**: Procesamiento automático de query parameters

### **Arquitectura MVC Moderna**

- **Controladores separados**: `GreetingController` y `MathController`
- **Separación de responsabilidades**: Lógica de negocio en controladores dedicados
- **Métodos estáticos**: Fácil testing y acceso directo

### **Testing Avanzado**

- **JUnit 5**: Framework de testing moderno
- **Tests de reflexión**: Validación de carga automática de componentes
- **Tests de integración**: Verificación completa del sistema de anotaciones

### **Build System Mejorado**

- **Maven Shade Plugin**: Generación de JARs ejecutables
- **Dependencias optimizadas**: Reflections para scanning de clases
- **Configuración JUnit 5**: Testing framework actualizado

## Características Principales

### **Framework de Servicios REST con Anotaciones**

- **Anotaciones personalizadas**: Sistema completo de anotaciones tipo Spring
- **Reflection-based routing**: Enrutamiento automático basado en anotaciones
- **Auto-discovery**: Descubrimiento automático de controladores
- **Parameter injection**: Inyección automática de parámetros de consulta

### **Funcionalidades Implementadas**

1. **Sistema de Anotaciones**

   - `@RestController` para marcar controladores
   - `@GetMapping` para definir rutas GET
   - `@RequestParam` para extraer parámetros

2. **Controladores con Anotaciones**

   - `GreetingController`: Endpoints de saludo
   - `MathController`: Operaciones matemáticas

3. **Carga Automática de Componentes**

   - Scanning automático del classpath
   - Registro automático de endpoints
   - Inicialización automática del framework

4. **Testing Completo**
   - Tests de reflexión y anotaciones
   - Tests de integración del sistema completo
   - Validación de carga automática

---

## Requisitos Previos

- **Java 21** [Descargar Java](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
- **Apache Maven 3.8+** [Instalar Maven](https://maven.apache.org/install.html)
- **Git** [Instalar Git](https://git-scm.com/downloads)

---

## Instalación y Ejecución

### Pasos para ejecutar el proyecto:

1. **Clonar el repositorio:**

   ```bash
   git clone https://github.com/JAGBytes/arep-taller3.git
   cd arep-taller3
   ```

2. **Compilar el proyecto:**

   ```bash
   mvn clean package
   ```

3. **Ejecutar el servidor:**

   ```bash
   java -jar target/arep-taller3-1.0-SNAPSHOT.jar
   ```

4. **Acceder a la aplicación:**
   ```
   http://localhost:35000
   ```

---

## Arquitectura del Framework

### **Componentes Principales:**

#### **Sistema de Anotaciones**

```java
@RestController
public class GreetingController {

    @GetMapping("/greeting")
    public static String greeting(@RequestParam String name) {
        return "Hola Mundo!";
    }

    @GetMapping("/hello")
    public static String sayHello(@RequestParam("name") String name) {
        return "Hola, " + name + "!";
    }
}
```

#### **HttpServer con Reflexión**

- **Auto-discovery**: Escaneo automático de controladores
- **Reflection-based routing**: Enrutamiento basado en anotaciones
- **Parameter injection**: Inyección automática de parámetros
- **Error handling**: Manejo robusto de errores de reflexión

#### **Métodos del Framework**

1. **`loadComponents(String[] args)`**

   - Carga automática de controladores usando Reflections
   - Registro automático de endpoints anotados
   - Procesamiento de parámetros con `@RequestParam`

2. **`get(String path, Function<Request, Response> handler)`**

   - Define servicios REST GET con funciones lambda (legacy)
   - Compatible con el sistema anterior

3. **`post(String path, Function<Request, Response> handler)`**

   - Define servicios REST POST (legacy)
   - Procesamiento de cuerpos JSON

4. **`staticfiles(String directory)`**
   - Configura directorio de archivos estáticos
   - Búsqueda en `target/classes + directory`

#### **Clases de Soporte**

- **Request**: Acceso a parámetros, headers, body JSON
- **Response**: Constructor de respuestas HTTP con Builder Pattern
- **Annotations**: Sistema completo de anotaciones personalizadas

---

## Ejemplos de Uso

### **Controladores con Anotaciones:**

#### **GreetingController.java:**

```java
@RestController
public class GreetingController {

    @GetMapping("/greeting")
    public static String greeting(@RequestParam String name) {
        return "Hola Mundo!";
    }

    @GetMapping("/hello")
    public static String sayHello(@RequestParam("name") String name) {
        return "Hola, " + name + "!";
    }
}
```

#### **MathController.java:**

```java
@RestController
public class MathController {

    @GetMapping("/add")
    public static String add(@RequestParam("a") String a, @RequestParam("b") String b) {
        try {
            int numA = Integer.parseInt(a);
            int numB = Integer.parseInt(b);
            return "Result: " + (numA + numB);
        } catch (NumberFormatException e) {
            return "Error: Invalid numbers";
        }
    }
}
```

### **Aplicación Principal (App.java):**

```java
public class App {
    public static void main(String[] args) throws Exception {
        // Configurar archivos estáticos
        HttpServer.staticfiles("/");

        // Los controladores se cargan automáticamente mediante reflexión
        // No es necesario registrar manualmente los endpoints

        // Servicios legacy (compatibilidad)
        HttpServer.get("/pi", (req, res) -> {
            return new Response.Builder()
                .withContentType("text/plain")
                .withBody(String.valueOf(Math.PI))
                .build();
        });

        HttpServer.get("/e", (req, res) -> {
            return new Response.Builder()
                .withContentType("text/plain")
                .withBody(String.valueOf(Math.E))
                .build();
        });

        // Iniciar el servidor
        HttpServer.startServer(args);
    }
}
```

---

## Pruebas y Validación

### **Ejecutar pruebas:**

```bash
mvn test
```

### **Pruebas Implementadas:**

#### **Tests de Anotaciones y Reflexión (ControllerLoadingTest.java)**

```java
@Test
@DisplayName("Test endpoint /greeting")
void testHelloEndpoint() throws Exception {
    URI testUri = new URI("/greeting");
    byte[] response = HttpServer.handleGetRequest(testUri);

    String responseStr = new String(response);
    assertTrue(responseStr.contains("200 OK"));
    assertTrue(responseStr.contains("Hola Mundo!"));
}

@Test
@DisplayName("Test endpoint /hello con parámetro")
void testRequestParam() throws Exception {
    URI testUri = new URI("/hello?name=Jorge");
    byte[] response = HttpServer.handleGetRequest(testUri);

    String responseStr = new String(response);
    assertTrue(responseStr.contains("200 OK"));
    assertTrue(responseStr.contains("Hola, Jorge!"));
}

@Test
@DisplayName("Test MathController - /add suma")
void testMultipleControllers() throws Exception {
    URI testUri = new URI("/add?a=3&b=7");
    byte[] response = HttpServer.handleGetRequest(testUri);

    String responseStr = new String(response);
    assertTrue(responseStr.contains("200 OK"));
    assertTrue(responseStr.contains("Result: 10"));
}
```

#### **Servicios con Anotaciones:**

- `GET /greeting` → Saludo básico
- `GET /hello?name=X` → Saludo personalizado
- `GET /add?a=X&b=Y` → Suma de dos números

#### **Servicios Legacy (compatibilidad):**

- `GET /pi` → Constante matemática PI
- `GET /e` → Número de Euler
- `POST /app/hello` → Registro de usuarios

#### **Archivos Estáticos**

- `GET /` → `index.html`
- `GET /styles.css` → Archivos CSS
- `GET /scripts.js` → Archivos JavaScript

#### **Seguridad**

- Protección contra path traversal
- Validación de URL encoding
- Manejo de errores de reflexión

---

## Ejemplos de Peticiones

### **Endpoints con Anotaciones:**

```bash
# Saludo básico
curl "http://localhost:35000/greeting"
# Respuesta: Hola Mundo!

# Saludo personalizado
curl "http://localhost:35000/hello?name=Juan"
# Respuesta: Hola, Juan!

# Operación matemática
curl "http://localhost:35000/add?a=5&b=3"
# Respuesta: Result: 8

# Números inválidos
curl "http://localhost:35000/add?a=abc&b=5"
# Respuesta: Error: Invalid numbers
```

### **Servicios Legacy:**

```bash
# Constante PI
curl "http://localhost:35000/pi"
# Respuesta: 3.141592653589793

# Número de Euler
curl "http://localhost:35000/e"
# Respuesta: 2.718281828459045

# Registrar usuario
curl -X POST -H "Content-Type: application/json" \
     -d '{"name":"NuevoUsuario"}' \
     "http://localhost:35000/app/hello"
# Respuesta: {"message": "Hola NuevoUsuario fuiste registrado exitosamente!"}
```

---

## Estructura del Proyecto

```
arep-taller3/
│
├── src/main/
│   ├── java/edu/escuelaing/arem/ASE/app/
│   │   ├── App.java                    # Aplicación principal
│   │   ├── http/
│   │   │   ├── HttpServer.java         # Servidor con reflexión
│   │   │   ├── Request.java            # Manejo de peticiones
│   │   │   └── Response.java           # Constructor de respuestas
│   │   ├── annotation/                 # 🆕 Sistema de anotaciones
│   │   │   ├── GetMapping.java         # Anotación para GET
│   │   │   ├── RequestParam.java       # Anotación para parámetros
│   │   │   └── RestController.java     # Anotación para controladores
│   │   └── Controller/                 # 🆕 Controladores con anotaciones
│   │       ├── GreetingController.java # Controlador de saludos
│   │       └── MathController.java     # Controlador matemático
│   │
│   └── resources/                      # Archivos estáticos
│       ├── index.html
│       ├── styles.css
│       ├── scripts.js
│       └── servicio-web.jpg
│
├── src/test/java/edu/escuelaing/arem/ASE/app/
│   ├── HttpServerTest.java             # Tests del servidor HTTP
│   └── ControllerLoadingTest.java      # 🆕 Tests de anotaciones y reflexión
│
├── target/classes/                     # Archivos compilados
├── pom.xml                            # Configuración Maven con nuevas dependencias
├── README.md                          # Documentación actualizada
└── .gitignore
```

---

## Características Técnicas

### **Sistema de Reflexión Implementado:**

- **Reflections library**: Escaneo automático del classpath
- **Runtime annotation processing**: Procesamiento de anotaciones en tiempo de ejecución
- **Method invocation**: Invocación dinámica de métodos
- **Parameter extraction**: Extracción automática de parámetros

### **Protocolo HTTP Implementado:**

- Headers completos (Content-Type, Content-Length)
- Status codes apropiados (200, 400, 404, 500)
- Métodos GET y POST
- JSON parsing

### **Seguridad:**

- Path traversal protection
- Input validation
- Canonical path verification
- Content-Type validation
- Reflection security handling

### **Dependencias Nuevas:**

```xml
<!-- Reflections para scanning de clases -->
<dependency>
    <groupId>org.reflections</groupId>
    <artifactId>reflections</artifactId>
    <version>0.10.2</version>
</dependency>

<!-- JUnit 5 para testing -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.2</version>
    <scope>test</scope>
</dependency>

<!-- Maven Shade Plugin para JARs ejecutables -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.5.0</version>
</plugin>
```

---

## Autor

**Jorge Andrés Gamboa Sierra**
