package edu.escuelaing.arem.ASE.app.http;

/**
 *
 * @author jgamb
 */
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Clase que representa una petición HTTP.
 *
 * Encapsula toda la información de una petición HTTP incluyendo: - URI y
 * parámetros de query - Cuerpo de la petición (body) - Headers HTTP - Métodos
 * de utilidad para parsing de datos
 */
public class Request {

    private final URI uri;
    private final String body;
    private final Map<String, String> headers;

    private Request(Builder builder) {
        this.uri = builder.uri;
        this.body = builder.body != null ? builder.body : "";
        this.headers = builder.headers != null ? new HashMap<>(builder.headers) : new HashMap<>();
    }

    /**
     * Obtiene la ruta de la URI sin parámetros de query.
     *
     * @return La ruta de la petición
     */
    public String getPath() {
        return uri.getPath();
    }

    /**
     * Obtiene un parámetro específico de la query string.
     *
     * @param key El nombre del parámetro
     * @return El valor del parámetro o null si no existe
     */
    public String getQueryParam(String key) {
        if (uri.getQuery() == null) {
            return null;
        }

        for (String param : uri.getQuery().split("&")) {
            String[] kv = param.split("=", 2);
            if (kv[0].equals(key)) {
                try {
                    return kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
                } catch (Exception e) {
                    return kv.length > 1 ? kv[1] : "";
                }
            }
        }
        return null;
    }

    /**
     * Obtiene todos los parámetros de query como un mapa.
     *
     * @return Mapa con todos los parámetros de query
     */
    public Map<String, String> getQueryParams() {
        Map<String, String> params = new HashMap<>();
        if (uri.getQuery() == null) {
            return params;
        }

        for (String param : uri.getQuery().split("&")) {
            String[] kv = param.split("=", 2);
            try {
                String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
                params.put(key, value);
            } catch (Exception e) {
                // Si falla el decode, usar valores sin decodificar
                params.put(kv[0], kv.length > 1 ? kv[1] : "");
            }
        }
        return params;
    }

    /**
     * Obtiene el cuerpo completo de la petición.
     *
     * @return El cuerpo de la petición como string
     */
    public String getBody() {
        return body;
    }

    /**
     * Verifica si la petición tiene cuerpo.
     *
     * @return true si tiene cuerpo, false en caso contrario
     */
    public boolean hasBody() {
        return body != null && !body.trim().isEmpty();
    }

    /**
     * Obtiene el valor de un header específico.
     *
     * @param name El nombre del header (case-insensitive)
     * @return El valor del header o null si no existe
     */
    public String getHeader(String name) {
        return headers.get(name.toLowerCase());
    }

    /**
     * Obtiene todos los headers como un mapa.
     *
     * @return Mapa con todos los headers
     */
    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }

    /**
     * Verifica si existe un header específico.
     *
     * @param name El nombre del header (case-insensitive)
     * @return true si existe, false en caso contrario
     */
    public boolean hasHeader(String name) {
        return headers.containsKey(name.toLowerCase());
    }

    /**
     * Obtiene el Content-Type de la petición.
     *
     * @return El Content-Type o null si no está presente
     */
    public String getContentType() {
        return getHeader("content-type");
    }

    /**
     * Obtiene la longitud del contenido.
     *
     * @return La longitud del contenido o 0 si no está presente
     */
    public int getContentLength() {
        String lengthStr = getHeader("content-length");
        if (lengthStr != null) {
            try {
                return Integer.parseInt(lengthStr);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Parsea el cuerpo como datos de formulario
     * (application/x-www-form-urlencoded).
     *
     * @return Mapa con los datos del formulario
     */
    public Map<String, String> getFormData() {
        Map<String, String> formData = new HashMap<>();
        if (!hasBody()) {
            return formData;
        }

        String contentType = getContentType();
        if (contentType != null && contentType.toLowerCase().contains("application/x-www-form-urlencoded")) {
            for (String param : body.split("&")) {
                String[] kv = param.split("=", 2);
                try {
                    String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                    String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
                    formData.put(key, value);
                } catch (Exception e) {
                    // Si falla el decode, usar valores sin decodificar
                    formData.put(kv[0], kv.length > 1 ? kv[1] : "");
                }
            }
        }
        return formData;
    }

    /**
     * Verifica si el contenido es JSON.
     *
     * @return true si el Content-Type indica JSON
     */
    public boolean isJson() {
        String contentType = getContentType();
        return contentType != null && contentType.toLowerCase().contains("application/json");
    }

    /**
     * Verifica si el contenido es de formulario.
     *
     * @return true si el Content-Type indica form data
     */
    public boolean isFormData() {
        String contentType = getContentType();
        return contentType != null && contentType.toLowerCase().contains("application/x-www-form-urlencoded");
    }

    /**
     * Extrae un valor específico de un JSON simple en el cuerpo.
     *
     * @param key La clave a buscar
     * @return El valor encontrado o null
     */
    public String getJsonValue(String key) {
        if (!hasBody() || !isJson()) {
            return null;
        }

        try {
            // Parser muy básico para JSON simple: {"key": "value"}
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher matcher = regex.matcher(body);

            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            // Si falla el parsing, retornar null
        }

        return null;
    }

    /**
     * Obtiene la URI completa de la petición.
     *
     * @return La URI de la petición
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Representación en string de la petición para debugging.
     *
     * @return String descriptivo de la petición
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Request{");
        sb.append("path='").append(getPath()).append('\'');
        if (uri.getQuery() != null) {
            sb.append(", query='").append(uri.getQuery()).append('\'');
        }
        if (hasBody()) {
            sb.append(", bodyLength=").append(body.length());
        }
        sb.append(", headers=").append(headers.size());
        sb.append('}');
        return sb.toString();
    }

    /**
     * Builder para crear respuestas HTTP. Métodos: withStatus, withContentType,
     * withBody, withBodyBytes, addHeader, build.
     */
    public static class Builder {

        private URI uri;
        private String body;
        private Map<String, String> headers;

        public Builder withUri(URI uri) {
            this.uri = uri;
            return this;
        }

        public Builder withBody(String body) {
            this.body = body;
            return this;
        }

        public Builder withHeaders(Map<String, String> headers) {
            this.headers = new HashMap<>();
            // Convertir todos los nombres de headers a minúsculas para consistencia
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                this.headers.put(entry.getKey().toLowerCase(), entry.getValue());
            }
            return this;
        }

        public Builder withHeader(String name, String value) {
            if (this.headers == null) {
                this.headers = new HashMap<>();
            }
            this.headers.put(name.toLowerCase(), value);
            return this;
        }

        public Request build() {
            if (uri == null) {
                throw new IllegalStateException("URI is required");
            }
            return new Request(this);
        }
    }
}
