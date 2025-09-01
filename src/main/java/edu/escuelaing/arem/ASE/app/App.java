package edu.escuelaing.arem.ASE.app;

import edu.escuelaing.arem.ASE.app.http.Response;
import edu.escuelaing.arem.ASE.app.http.HttpServer;

/**
 * Clase principal de la aplicación que configura e inicia el servidor HTTP.
 * 
 * Esta clase configura los endpoints del servidor, incluyendo tanto endpoints
 * legacy (definidos manualmente) como endpoints automáticos (cargados mediante
 * reflexión desde controladores anotados). También configura el directorio
 * de archivos estáticos y maneja el registro de usuarios.
 * 
 * @author jgamb
 * @version 1.0
 * @since 1.0
 */
public class App {

    /**
     * Método principal que inicia la aplicación.
     * 
     * Configura los endpoints del servidor, incluyendo endpoints legacy y
     * automáticos, y luego inicia el servidor HTTP en el puerto configurado.
     * 
     * @param args Argumentos de línea de comandos (opcional)
     * @throws Exception Si ocurre un error al iniciar el servidor
     */
    public static void main(String[] args) throws Exception {

        // Configurar directorio de archivos estáticos
        HttpServer.staticfiles("/");

        // Endpoint GET /app/hello - Saluda al usuario si está registrado, de lo
        // contrario indica que no lo está
        HttpServer.get("/app/hello", (req, res) -> {
            System.out.println("registrando o ejecutando app hello");
            String name = req.getQueryParam("name");

            if (name != null && !name.isEmpty()) {
                boolean userExists = HttpServer.getUsers().containsValue(name);
                String message = userExists
                        ? "Hola " + name
                        : "No estás registrado en el sistema.";

                return new Response.Builder() // Crear nueva instancia del Builder
                        .withStatus(200)
                        .withBody("{\"message\": \"" + message + "\"}")
                        .build();
            } else {
                return new Response.Builder() // Crear nueva instancia del Builder
                        .withStatus(400)
                        .withBody("{\"message\": \"Parámetro inválido en la petición.\"}")
                        .build();
            }
        });

        // Endpoint GET /pi - Devuelve el valor de la constante matemática PI
        HttpServer.get("/pi", (req, res) -> {
            System.out.println("registrando o ejecutando pi");
            return new Response.Builder()
                    .withContentType("text/plain")
                    .withBody(String.valueOf(Math.PI))
                    .build();
        });

        // Endpoint GET /e - Devuelve el valor de la constante matemática e (número de
        // Euler)
        HttpServer.get("/e", (req, res) -> {
            return new Response.Builder()
                    .withContentType("text/plain")
                    .withBody(String.valueOf(Math.E))
                    .build();
        });

        // Endpoint POST /app/hello - Registra un usuario si se envía un JSON válido con
        // el campo "name"
        HttpServer.post("/app/hello", (req, res) -> {
            if (!req.hasBody()) {
                return new Response.Builder()
                        .withStatus(400)
                        .withBody("{\"error\": \"Cuerpo de la petición requerido\"}")
                        .build();
            }

            if (req.isJson()) {
                String name = req.getJsonValue("name");

                if (name != null && !name.isEmpty()) {
                    HttpServer.addUser(name);

                    return new Response.Builder()
                            .withStatus(200)
                            .withBody("{\"message\": \"Hola " + name + " fuiste registrado exitosamente!\"}")
                            .build();
                } else {
                    return new Response.Builder()
                            .withStatus(400)
                            .withBody("{\"error\": \"Nombre de usuario requerido en el campo 'name'\"}")
                            .build();
                }
            } else {
                return new Response.Builder()
                        .withStatus(400)
                        .withBody("{\"error\": \"Content-Type debe ser application/json\"}")
                        .build();
            }
        });
        try {
            HttpServer.startServer(args);
        } catch (Exception e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }
}
