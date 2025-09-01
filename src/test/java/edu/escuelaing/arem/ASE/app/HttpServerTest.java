package edu.escuelaing.arem.ASE.app;

import edu.escuelaing.arem.ASE.app.http.Response;
import edu.escuelaing.arem.ASE.app.http.HttpServer;
import org.junit.jupiter.api.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas para HttpServer con servicios básicos y funcionalidades principales
 */
class HttpServerTest {

    private static final int TEST_PORT = 35001;
    private static ServerSocket testServerSocket;
    private static volatile boolean serverRunning = false;

    @BeforeAll
    static void setUpClass() throws IOException {
        // Configurar puerto de prueba
        HttpServer.port = TEST_PORT;

        // Crear directorio de archivos estáticos de prueba
        createTestStaticFiles();
        HttpServer.staticfiles("/test-static");

        // Limpiar servicios anteriores
        clearServices();

        // Registrar servicios de prueba
        registerTestServices();
    }

    @BeforeEach
    void setUp() {
        // Limpiar usuarios antes de cada prueba
        HttpServer.getUsers().clear();
        HttpServer.loadInitialData();
    }

    @AfterAll
    static void tearDownClass() throws IOException {
        if (testServerSocket != null && !testServerSocket.isClosed()) {
            testServerSocket.close();
        }
        cleanupTestFiles();
    }

    /**
     * Crea archivos estáticos de prueba
     */
    private static void createTestStaticFiles() throws IOException {
        Path testDir = Paths.get("target/classes/test-static");
        Files.createDirectories(testDir);

        Files.write(testDir.resolve("index.html"),
                "<html><body><h1>Test Index</h1></body></html>".getBytes());
        Files.write(testDir.resolve("style.css"),
                "body { background-color: #f0f0f0; }".getBytes());
        Files.write(testDir.resolve("data.json"),
                "{\"message\": \"Hello World\", \"status\": \"ok\"}".getBytes());

        Path subDir = testDir.resolve("subdir");
        Files.createDirectories(subDir);
        Files.write(subDir.resolve("nested.txt"),
                "This is a nested file".getBytes());
    }

    /**
     * Limpia servicios registrados
     */
    private static void clearServices() {
        try {
            HttpServer httpServer = new HttpServer();
            httpServer.getGetServices().clear();
            httpServer.getPostServices().clear();
        } catch (Exception e) {
            System.err.println("Error limpiando servicios: " + e.getMessage());
        }
    }

    /**
     * Registra servicios de prueba: 2 GET y 1 POST
     */
    private static void registerTestServices() {

        // GET 1: Servicio de saludo con parámetros
        HttpServer.get("/api/hello", (req, res) -> {
            String name = req.getQueryParam("name");
            String message = name != null ? "Hello " + name + "!" : "Hello World!";

            return new Response.Builder()
                    .withBody("{\"message\": \"" + message + "\"}")
                    .build();
        });

        // GET 2: Servicio para obtener lista de usuarios
        HttpServer.get("/api/users", (req, res) -> {
            StringBuilder json = new StringBuilder("{\"users\": [");
            Map<String, String> users = HttpServer.getUsers();

            String[] ids = users.keySet().toArray(new String[0]);
            for (int i = 0; i < ids.length; i++) {
                String id = ids[i];
                json.append("{\"id\": \"").append(id)
                        .append("\", \"name\": \"").append(users.get(id)).append("\"}");
                if (i < ids.length - 1) {
                    json.append(",");
                }
            }
            json.append("]}");

            return new Response.Builder()
                    .withBody(json.toString())
                    .build();
        });

        // POST 1: Servicio para crear usuarios
        HttpServer.post("/api/users", (req, res) -> {
            try {
                if (!req.hasBody()) {
                    return new Response.Builder()
                            .withStatus(400)
                            .withBody("{\"error\": \"Request body is required\"}")
                            .build();
                }

                String name = req.getJsonValue("name");
                if (name == null || name.trim().isEmpty()) {
                    return new Response.Builder()
                            .withStatus(400)
                            .withBody("{\"error\": \"Name is required\"}")
                            .build();
                }

                HttpServer.addUser(name);

                return new Response.Builder()
                        .withStatus(201)
                        .withContentType("application/json")
                        .withBody("{\"message\": \"User created\", \"name\": \"" + name + "\"}")
                        .build();

            } catch (Exception e) {
                return new Response.Builder()
                        .withStatus(500)
                        .withContentType("application/json")
                        .withBody("{\"error\": \"Internal server error\"}")
                        .build();
            }
        });
    }

    // ============ PRUEBAS BÁSICAS ============
    @Test
    @DisplayName("Test carga inicial de datos")
    void testLoadInitialData() {
        Map<String, String> users = HttpServer.getUsers();

        assertEquals(3, users.size(), "Debe cargar 3 usuarios iniciales");
        assertTrue(users.containsValue("Andres"));
        assertTrue(users.containsValue("Maria"));
        assertTrue(users.containsValue("Carlos"));
    }

    @Test
    @DisplayName("Test agregar usuario")
    void testAddUser() {
        int initialSize = HttpServer.getUsers().size();
        HttpServer.addUser("TestUser");

        assertEquals(initialSize + 1, HttpServer.getUsers().size());
        assertTrue(HttpServer.getUsers().containsValue("TestUser"));
    }

    // ============ PRUEBAS DE SERVICIOS REGISTRADOS ============
    @Test
    @DisplayName("Test servicio GET /api/hello con parámetros")
    void testGetHelloWithParams() throws Exception {
        URI testUri = new URI("/api/hello?name=Juan");
        byte[] response = HttpServer.handleGetRequest(testUri);

        String responseStr = new String(response);
        assertTrue(responseStr.contains("200 OK"));
        assertTrue(responseStr.contains("Hello Juan!"));
    }

    @Test
    @DisplayName("Test servicio GET /api/users")
    void testGetUsers() throws Exception {
        URI testUri = new URI("/api/users");
        byte[] response = HttpServer.handleGetRequest(testUri);

        String responseStr = new String(response);
        assertTrue(responseStr.contains("200 OK"));
        assertTrue(responseStr.contains("users"));
        assertTrue(responseStr.contains("Andres"));
        assertTrue(responseStr.contains("Maria"));
        assertTrue(responseStr.contains("Carlos"));
    }

    @Test
    @DisplayName("Test servicio POST /api/users")
    void testPostCreateUser() throws Exception {
        URI testUri = new URI("/api/users");
        String jsonBody = "{\"name\": \"NewUser\"}";

        StringReader stringReader = new StringReader(
                "Content-Type: application/json\r\n"
                + "Content-Length: " + jsonBody.length() + "\r\n"
                + "\r\n" + jsonBody
        );
        BufferedReader reader = new BufferedReader(stringReader);

        byte[] response = HttpServer.handlePostRequest(testUri, reader);
        String responseStr = new String(response);

        assertTrue(responseStr.contains("201"));
        assertTrue(responseStr.contains("User created"));
        assertTrue(responseStr.contains("NewUser"));

        // Verificar que el usuario fue agregado
        assertTrue(HttpServer.getUsers().containsValue("NewUser"));
    }

    // ============ PRUEBAS DE ARCHIVOS ESTÁTICOS ============
    @Test
    @DisplayName("Test archivo estático - index.html")
    void testStaticFileIndex() throws Exception {
        URI testUri = new URI("/");
        byte[] response = HttpServer.handleGetRequest(testUri);

        String responseStr = new String(response);
        assertTrue(responseStr.contains("200 OK"));
        assertTrue(responseStr.contains("Test Index"));
        assertTrue(responseStr.contains("text/html"));
    }

    @Test
    @DisplayName("Test archivo estático - CSS")
    void testStaticFileCss() throws Exception {
        URI testUri = new URI("/style.css");
        byte[] response = HttpServer.handleGetRequest(testUri);

        String responseStr = new String(response);
        assertTrue(responseStr.contains("200 OK"));
        assertTrue(responseStr.contains("background-color"));
        assertTrue(responseStr.contains("text/css"));
    }

    // ============ PRUEBAS DE SEGURIDAD ============
    @Test
    @DisplayName("Test seguridad - Path traversal bloqueado")
    void testPathTraversalSecurity() throws Exception {
        URI testUri = new URI("/../../../etc/passwd");
        byte[] response = HttpServer.handleGetRequest(testUri);

        String responseStr = new String(response);
        assertTrue(responseStr.contains("404"));
    }

    @Test
    @DisplayName("Test seguridad - Path traversal con URL encoding")
    void testPathTraversalWithEncoding() throws Exception {
        URI testUri = new URI("/%2E%2E%2F%2E%2E%2F%2E%2E%2Fetc%2Fpasswd");
        byte[] response = HttpServer.handleGetRequest(testUri);

        String responseStr = new String(response);
        assertTrue(responseStr.contains("404"));
    }

    @Test
    @DisplayName("Test seguridad - Acceso a directorio bloqueado")
    void testDirectoryAccessBlocked() throws Exception {
        URI testUri = new URI("/subdir/");
        byte[] response = HttpServer.handleGetRequest(testUri);

        String responseStr = new String(response);
        assertTrue(responseStr.contains("404"));
    }

    // ============ PRUEBAS DE INTEGRACIÓN ============
    @Test
    @DisplayName("Integración - Servidor completo")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testServerIntegration() throws Exception {
        // Iniciar servidor en hilo separado
        CompletableFuture<Void> serverFuture = CompletableFuture.runAsync(() -> {
            try {
                testServerSocket = new ServerSocket(TEST_PORT);
                serverRunning = true;

                // Procesar algunas conexiones para la prueba
                for (int i = 0; i < 3; i++) {
                    try (Socket clientSocket = testServerSocket.accept()) {
                        HttpServer.handleClient(clientSocket);
                    }
                }
            } catch (IOException e) {
                if (!testServerSocket.isClosed()) {
                    e.printStackTrace();
                }
            }
        });

        // Esperar a que el servidor esté listo
        Thread.sleep(100);
        assertTrue(serverRunning, "Servidor debe estar ejecutándose");

        // Test 1: GET servicio
        String response1 = sendHttpRequest("GET", "/api/hello?name=Integration", "");
        assertTrue(response1.contains("200 OK"));
        assertTrue(response1.contains("Hello Integration!"));

        // Test 2: POST servicio
        String postData = "{\"name\": \"IntegrationUser\"}";
        String response2 = sendHttpRequest("POST", "/api/users", postData);
        assertTrue(response2.contains("201"));
        assertTrue(response2.contains("User created"));

        // Test 3: GET archivo estático
        String response3 = sendHttpRequest("GET", "/data.json", "");
        assertTrue(response3.contains("200 OK"));
        assertTrue(response3.contains("Hello World"));

        // Cerrar servidor
        testServerSocket.close();
        serverFuture.cancel(true);
    }

    // ============ MÉTODOS AUXILIARES ============
    /**
     * Envía una petición HTTP al servidor de prueba
     */
    private String sendHttpRequest(String method, String path, String body) throws IOException {
        try (Socket socket = new Socket("localhost", TEST_PORT); PrintWriter out = new PrintWriter(socket.getOutputStream(), true); BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Enviar petición
            out.println(method + " " + path + " HTTP/1.1");
            out.println("Host: localhost:" + TEST_PORT);

            if (!body.isEmpty()) {
                out.println("Content-Type: application/json");
                out.println("Content-Length: " + body.length());
                out.println();
                out.print(body);
            } else {
                out.println();
            }
            out.flush();

            // Leer respuesta
            StringBuilder response = new StringBuilder();
            String line;
            boolean inBody = false;
            int contentLength = 0;

            while ((line = in.readLine()) != null) {
                response.append(line).append("\n");

                if (line.toLowerCase().startsWith("content-length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }

                if (line.isEmpty()) {
                    inBody = true;
                    if (contentLength > 0) {
                        char[] bodyChars = new char[contentLength];
                        in.read(bodyChars, 0, contentLength);
                        response.append(new String(bodyChars));
                    }
                    break;
                }

                if (!in.ready() && !inBody) {
                    break;
                }
            }

            return response.toString();
        }
    }

    /**
     * Limpia archivos de prueba
     */
    private static void cleanupTestFiles() {
        try {
            Path testDir = Paths.get("target/classes/test-static");
            if (Files.exists(testDir)) {
                Files.walk(testDir)
                        .sorted(java.util.Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        } catch (IOException e) {
            System.err.println("Error limpiando archivos de prueba: " + e.getMessage());
        }
    }

}
