    package edu.escuelaing.arem.ASE.app.http;

    import java.nio.charset.StandardCharsets;
    import java.util.HashMap;
    import java.util.Map;

    /**
     * Representa una respuesta HTTP construida por el servidor.
     * 
     * Permite definir código de estado, tipo de contenido, cuerpo en texto o bytes,
     * y encabezados adicionales. Provee métodos para generar la respuesta en el 
     * formato requerido por el protocolo HTTP (headers + body).
     */
    public class Response {

        private final int status;
        private final String contentType;
        private byte[] bodyBytes;
        private boolean includeContentLength;
        private final Map<String, String> extraHeaders;

        private Response(Builder builder) {
            this.status = builder.status;
            this.contentType = builder.contentType;
            this.bodyBytes = builder.bodyBytes;
            this.includeContentLength = builder.includeContentLength;
            this.extraHeaders = builder.extraHeaders;
        }

         /**
         * Genera solo los headers HTTP en bytes.
         *
         * @return headers formateados como arreglo de bytes
         */
        public byte[] getHeaderBytes() {
            StringBuilder headers = new StringBuilder();
            headers.append("HTTP/1.1 ").append(status).append(" ").append(getStatusText()).append("\r\n");
            headers.append("Content-Type: ").append(contentType).append("\r\n");

            if (includeContentLength && bodyBytes != null && bodyBytes.length > 0) {
                headers.append("Content-Length: ").append(bodyBytes.length).append("\r\n");
            }

            // Agregar headers adicionales
            for (Map.Entry<String, String> entry : extraHeaders.entrySet()) {
                headers.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
            }

            headers.append("\r\n"); // Línea en blanco que separa headers y body
            return headers.toString().getBytes(StandardCharsets.UTF_8);
        }

         /**
         * Construye la respuesta completa (headers + body) en bytes.
         *
         * @return respuesta HTTP lista para enviar al cliente
         */
        public byte[] toBytes() {
            byte[] headerBytes = getHeaderBytes();
            int bodyLength = (bodyBytes != null && bodyBytes.length > 0) ? bodyBytes.length : 0;

            byte[] response = new byte[headerBytes.length + bodyLength];

            System.arraycopy(headerBytes, 0, response, 0, headerBytes.length);

            if (bodyLength > 0) {
                System.arraycopy(bodyBytes, 0, response, headerBytes.length, bodyLength);
            }

            return response;
        }

         /**
         * Traduce un código de estado a su texto correspondiente (ej: 200 -> OK).
         *
         * @return descripción textual del estado
         */
        private String getStatusText() {
            return switch (status) {
                case 200 ->
                    "OK";
                case 201 ->
                    "Created";
                case 204 ->
                    "No Content";
                case 400 ->
                    "Bad Request";
                case 401 ->
                    "Unauthorized";
                case 403 ->
                    "Forbidden";
                case 404 ->
                    "Not Found";
                case 500 ->
                    "Internal Server Error";
                default ->
                    "Unknown";
            };
        }

        public void setBody(String body) {
            this.includeContentLength = true;
            this.bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        }

        public void setBodyBytes(byte[] bodyBytes) {
            this.includeContentLength = true;
            this.bodyBytes = bodyBytes;

        }

         /**
         * Builder para crear instancias de Response de forma flexible.
         *
         * Métodos disponibles:
         * - withStatus(int): establece el código de estado
         * - withContentType(String): define el tipo de contenido
         * - withBody(String): cuerpo como texto
         * - withBodyBytes(byte[]): cuerpo como bytes
         * - addHeader(String, String): agrega encabezados personalizados
         * - build(): construye el objeto Response
         */
        public static class Builder {

            private int status = 200;
            private String contentType = "application/json";
            private byte[] bodyBytes = new byte[0];
            private boolean includeContentLength = false;
            private Map<String, String> extraHeaders = new HashMap<>();

            public Builder withStatus(int status) {
                this.status = status;
                return this;
            }

            public Builder withContentType(String contentType) {
                this.contentType = contentType;
                return this;
            }

            public Builder withBody(String body) {
                this.includeContentLength = true;
                this.bodyBytes = body.getBytes(StandardCharsets.UTF_8);
                return this;
            }

            public Builder withBodyBytes(byte[] bodyBytes) {
                this.includeContentLength = true;
                this.bodyBytes = bodyBytes;
                return this;
            }

            public Builder addHeader(String name, String value) {
                this.extraHeaders.put(name, value);
                return this;
            }

            public Response build() {
                return new Response(this);
            }
        }
    }
