package edu.escuelaing.arem.ASE.app.http;

/**
 *
 * @author jgamb
 */
import edu.escuelaing.arem.ASE.app.annotation.GetMapping;
import edu.escuelaing.arem.ASE.app.annotation.RequestParam;
import edu.escuelaing.arem.ASE.app.annotation.RestController;
import java.net.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.reflections.Reflections;

/**
 * Servidor HTTP básico que maneja peticiones GET y POST.
 *
 * Este servidor implementa funcionalidades básicas de HTTP incluyendo: - Servir
 * archivos estáticos desde el directorio "resources" - API REST para manejo de
 * usuarios en el endpoint "/app/hello" - Soporte para peticiones GET y POST
 *
 * El servidor mantiene un registro de usuarios en memoria y proporciona
 * servicios de registro y saludo personalizado.
 *
 * @author jgamb
 * @version 1.0
 * @since 1.0
 */
public class HttpServer {

    static public int port = 35000;
    private static final HashMap<String, String> users = new HashMap<>();
    private static Map<String, BiFunction<Request, Response, Response>> getServices = new HashMap<>();
    private static Map<String, BiFunction<Request, Response, Response>> postServices = new HashMap<>();
    private static String staticFilesDirectory = "";
    private static int idCounter = 1;

    /**
     * Método principal que inicia el servidor HTTP.
     *
     * Carga los datos iniciales, crea un ServerSocket en el puerto especificado
     * y comienza a escuchar peticiones de clientes.
     *
     * @param args Argumentos de línea de comandos
     * @throws IOException Si ocurre un error de E/S al crear el socket
     * @throws Exception Si ocurre cualquier otro error inesperado
     */
    public static void startServer(String[] args) throws IOException, Exception {
        loadInitialData();
        loadComponents(args);
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Servidor escuchando en el puerto " + port);

            runServer(serverSocket);

        } catch (IOException e) {

            System.err.println("No se pudo iniciar el servidor en el puerto: " + port);
            System.exit(1);
        }
    }

    public static void loadComponents(String args[]) {
        try {
            Reflections reflections = new Reflections("edu.escuelaing.arem.ASE.app");

            // Buscar todas las clases anotadas con @RestController
            Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(RestController.class);
            
            for(Class<?> c : controllers){
                System.out.println("clase controller: " + c.getName());
                Method[] methods = c.getDeclaredMethods();
                for (Method m : methods) {
                    if (m.isAnnotationPresent(GetMapping.class)) {
                        String mapping = m.getAnnotation(GetMapping.class).value();
                        System.out.println("nombre de metodo registrado: " + m.getName());
                        get(mapping, (req, res) -> {
                            try {
                                System.out.println("retristrando metodo: " + mapping);
                                Parameter[] parameters = m.getParameters();
                                Object[] methodArgs = new Object[parameters.length];

                                // Procesar parámetros anotados con @RequestParam
                                for (int i = 0; i < parameters.length; i++) {
                                    Parameter param = parameters[i];

                                    if (param.isAnnotationPresent(RequestParam.class)) {
                                        String paramName = param.getAnnotation(RequestParam.class).value();
                                        String paramValue = req.getQueryParam(paramName);
                                        methodArgs[i] = paramValue; // Solo String, sin conversiones
                                    } else {
                                        return new Response.Builder()
                                                .withStatus(400)
                                                .withBody("Parámetro no soportado: " + param.getName())
                                                .build();
                                    }
                                }

                                Object result = m.invoke(null, methodArgs);
                                return new Response.Builder()
                                        .withStatus(200)
                                        .withBody(result != null ? result.toString() : "")
                                        .build();

                            } catch (IllegalAccessException | InvocationTargetException e) {
                                return new Response.Builder()
                                        .withStatus(500)
                                        .withBody("Error: " + e.getMessage())
                                        .build();
                            }
                        });
                    }
                }
            }
        } catch (SecurityException ex) {
            Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Carga datos iniciales de usuarios en el sistema.
     *
     * Este método se ejecuta al iniciar el servidor y registra tres usuarios
     * por defecto: Andres, Maria y Carlos. Este metodo es solo de prueba para
     * cargar usuarios antes de cargar el servidor.
     */
    public static void loadInitialData() {
        addUser("Andres");
        addUser("Maria");
        addUser("Carlos");
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * Genera automáticamente un ID único para el usuario y lo almacena en el
     * mapa de usuarios. Este metodo es solo de prueba para cargar usuarios
     * antes de cargar el servidor.
     *
     * @param name Nombre del usuario a registrar
     */
    public static void addUser(String name) {
        String id = String.valueOf(idCounter++);
        users.put(id, name);
    }

    /**
     * Ejecuta el bucle principal del servidor.
     *
     * Mantiene el servidor en ejecución, aceptando conexiones de clientes de
     * forma continua hasta que se detenga el servidor.
     *
     * @param serverSocket El socket del servidor que escucha las conexiones
     */
    public static void runServer(ServerSocket serverSocket) {

        Boolean running = true;
        while (running) {

            System.out.println("Listo para recibir ...");

            try (Socket clientSocket = serverSocket.accept()) {
                handleClient(clientSocket);

            } catch (IOException e) {
                System.err.println("Error al procesar el cliente: " + e.getMessage());
            }
        }
    }

    /**
     * Maneja la conexión de un cliente específico.
     *
     * Procesa la petición HTTP del cliente, determina el método HTTP (GET o
     * POST) y enruta la petición al manejador correspondiente. Finalmente envía
     * la respuesta al cliente.
     *
     * @param clientSocket Socket de conexión con el cliente
     */
    public static void handleClient(Socket clientSocket) {
        try (OutputStream out = clientSocket.getOutputStream(); BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            String inputLine;
            boolean isFirstLine = true;
            byte[] responseBytes = "HTTP/1.1 400 Bad Request\r\n\r\n".getBytes(StandardCharsets.UTF_8);

            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received: " + inputLine);

                if (isFirstLine) {
                    // Ejemplo de primera línea: "GET /index.html HTTP/1.1"
                    String[] header = inputLine.split(" ");

                    String method = header[0];
                    URI requestUri = new URI(header[1]);

                    // Seleccionar el manejador según el método HTTP
                    responseBytes = switch (method) {
                        case "GET" ->
                            handleGetRequest(requestUri);
                        case "POST" ->
                            handlePostRequest(requestUri, in);
                        default ->
                            new Response.Builder().withStatus(405).withBody("Method Not Allowed").build().toBytes();
                    };

                    System.out.println("Path: " + requestUri.getPath());
                    isFirstLine = false;
                }
                // Si no hay más datos en el request, salir del bucle
                if (!in.ready()) {
                    break;
                }
            }

            out.write(responseBytes);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error I/O con el cliente: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
        }
    }

    /**
     * Maneja las peticiones HTTP GET.
     *
     * Procesa las solicitudes GET de dos maneras: 1. Si la ruta coincide con un
     * servicio registrado en {@code getServices}, ejecuta su lógica. 2. Si no
     * coincide, intenta devolver un archivo estático desde el directorio
     * configurado. En caso de no encontrarlo, retorna un error 404. Si ocurre
     * un problema interno, retorna 500.
     *
     * @param uriReq URI de la petición que incluye la ruta solicitada y
     * posibles parámetros
     * @return Array de bytes con la respuesta HTTP completa (encabezados +
     * cuerpo)
     */
    public static byte[] handleGetRequest(URI uriReq) {

        String path = uriReq.getPath();

        if (getServices.containsKey(path)) {
            //plantilla de referencia para el lambda
            Response res = new Response.Builder().build();
            Request req = new Request.Builder().withUri(uriReq).build();
            Response response = getServices.get(path).apply(req, res);
            return response.toBytes();
        }
        try {
            File file = resolveStaticFile(path);

            if (file == null || !file.exists() || file.isDirectory()) {
                Response res = new Response.Builder()
                        .withStatus(404)
                        .withBody("{\"error\": \"Endpoint get not found\"}")
                        .build();

                return res.toBytes();
            }

            String contentType = Files.probeContentType(file.toPath());
            byte[] fileBytes = Files.readAllBytes(file.toPath());

            Response res = new Response.Builder()
                    .withContentType(contentType != null ? contentType : "application/octet-stream")
                    .withBodyBytes(fileBytes)
                    .build();

            return res.toBytes();

        } catch (IOException e) {
            Response res = new Response.Builder()
                    .withStatus(500)
                    .withBody("500 - Server Error: " + e.getMessage())
                    .build();

            return res.toBytes();
        }
    }

    /**
     * Maneja las peticiones HTTP POST.
     *
     * Procesa solicitudes POST de la siguiente manera: 1. Lee los encabezados
     * de la petición para obtener el valor de Content-Length. 2. Extrae y
     * construye el cuerpo de la petición a partir de dicho tamaño. 3. Construye
     * un objeto {@code Request} con la información obtenida. 4. Si la ruta
     * solicitada está registrada en {@code postServices}, ejecuta el servicio
     * asociado. 5. Si no existe un servicio para la ruta, devuelve un error
     * 404. En caso de error de E/S se devuelve 500, y si Content-Length no es
     * válido se devuelve 400.
     *
     * @param uriReq URI de la petición que incluye la ruta solicitada
     * @param in BufferedReader para leer los encabezados y el cuerpo de la
     * petición
     * @return Array de bytes con la respuesta HTTP completa (encabezados +
     * cuerpo)
     */
    public static byte[] handlePostRequest(URI uriReq, BufferedReader in) {

        try {
            Map<String, String> headers = new HashMap<>();
            String line;
            int contentLength = 0;
            // Leer el Content-Length del encabezado para saber cuántos caracteres esperar en el cuerpo
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    String headerName = parts[0].trim().toLowerCase();
                    String headerValue = parts[1].trim();
                    headers.put(headerName, headerValue);

                    if (headerName.equals("content-length")) {
                        contentLength = Integer.parseInt(headerValue);
                    }
                }
            }

            String body = "";
            if (contentLength > 0) {
                char[] bodyChars = new char[contentLength];
                in.read(bodyChars, 0, contentLength);
                body = new String(bodyChars);
            }

            Request req = new Request.Builder()
                    .withUri(uriReq)
                    .withBody(body)
                    .withHeaders(headers)
                    .build();

            String path = uriReq.getPath();

            // Verificar si existe un servicio POST registrado para esta ruta
            if (postServices.containsKey(path)) {
                Response res = new Response.Builder().build();
                Response response = postServices.get(path).apply(req, res);
                return response.toBytes();
            }

            return new Response.Builder()
                    .withStatus(404)
                    .withBody("{\"error\": \"Endpoint not found\"}")
                    .build().toBytes();

        } catch (IOException e) {
            Response res = new Response.Builder()
                    .withStatus(500)
                    .withBody("{\"error\": \"Server Error: " + e.getMessage() + "\"}")
                    .build();

            return res.toBytes();
        } catch (NumberFormatException e) {
            Response res = new Response.Builder()
                    .withStatus(400)
                    .withBody("{\"error\": \"Invalid Content-Length header\"}")
                    .build();

            return res.toBytes();
        }
    }

    /**
     * Obtiene el mapa de usuarios registrados.
     *
     * @return HashMap con los usuarios registrados (ID -> Nombre)
     */
    public static HashMap<String, String> getUsers() {
        return users;
    }

    public static void get(String path, BiFunction<Request, Response, Response> handler) {
        getServices.put(path, handler);
    }

    public static void post(String path, BiFunction<Request, Response, Response> handler) {
        postServices.put(path, handler);
    }

    /**
     * Configura la carpeta base donde buscar ficheros estáticos. Ej:
     * staticfiles("/webroot") -> buscará en target/classes/webroot
     *
     * @param dir directorio donde se ubican los archivos estáticos
     */
    public static void staticfiles(String dir) {
        if (dir == null || dir.isBlank()) {
            staticFilesDirectory = "";
            return;
        }
        String d = dir.startsWith("/") ? dir : "/" + dir;
        if (d.endsWith("/")) {
            d = d.substring(0, d.length() - 1);
        }
        staticFilesDirectory = d;
    }

    /**
     * Resuelve la ruta de un archivo estático solicitado.
     *
     * Decodifica la URI, sirve index.html si la ruta es raíz y previene ataques
     * de path traversal asegurando que el archivo esté dentro del directorio
     * base configurado.
     *
     * @param requestPath Ruta solicitada en la petición
     * @return Archivo solicitado o {@code null} si es acceso no permitido
     * @throws IOException Si falla la resolución de rutas
     */
    private static File resolveStaticFile(String requestPath) throws IOException {
        // decodifica %20 y similares
        String decoded = java.net.URLDecoder.decode(requestPath, StandardCharsets.UTF_8.name());

        // si la petición es raíz, servir index.html
        if (decoded.equals("/") || decoded.isEmpty()) {
            decoded = "/index.html";
        }

        // usar ClassLoader para buscar en target/classes (lo que Maven genera)
        String resourcePath = (staticFilesDirectory + decoded).replaceFirst("^/", "");
        java.net.URL resourceUrl = HttpServer.class.getClassLoader().getResource(resourcePath);

        if (resourceUrl == null) {
            return null; // recurso no encontrado
        }

        return new File(resourceUrl.getFile());
    }

    public Map<String, BiFunction<Request, Response, Response>> getGetServices() {
        return getServices;
    }

    public Map<String, BiFunction<Request, Response, Response>> getPostServices() {
        return postServices;
    }
}
