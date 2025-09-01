package edu.escuelaing.arem.ASE.app;

import edu.escuelaing.arem.ASE.app.http.HttpServer;
import org.junit.jupiter.api.*;
import java.net.URI;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests simplificados para controladores - solo usa métodos públicos estáticos
 */
class SimpleControllerTest {

    @BeforeAll
    static void setUpClass() {
        // Cargar datos iniciales
        HttpServer.loadInitialData();
        
        // Cargar componentes usando reflection
        HttpServer.loadComponents(new String[]{});
    }

    @BeforeEach
    void setUp() {
        HttpServer.getUsers().clear();
        HttpServer.loadInitialData();
    }

    // -------------------------
    // PRUEBAS ESENCIALES CORREGIDAS
    // -------------------------

    @Test
    @DisplayName("Test endpoint /greeting")
    void testHelloEndpoint() throws Exception {
        URI testUri = new URI("/greeting");
        byte[] response = HttpServer.handleGetRequest(testUri);

        String responseStr = new String(response);
        System.out.println(responseStr);
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
    @DisplayName("Test endpoint inexistente")
    void testNotFoundEndpoint() throws Exception {
        URI testUri = new URI("/doesnotexist");
        byte[] response = HttpServer.handleGetRequest(testUri);

        String responseStr = new String(response);
        assertTrue(responseStr.contains("404"));
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

    // ============ TESTS ADICIONALES PARA TUS CONTROLADORES ============


    @Test
    @DisplayName("Test MathController - números inválidos")
    void testMathInvalidNumbers() throws Exception {
        URI testUri = new URI("/add?a=abc&b=5");
        byte[] response = HttpServer.handleGetRequest(testUri);

        String responseStr = new String(response);
        assertTrue(responseStr.contains("200 OK"));
        assertTrue(responseStr.contains("Error: Invalid numbers"));
    }


    @Test
    @DisplayName("Test que loadComponents() funciona correctamente")
    void testLoadComponentsWorks() throws Exception {
        // Verificar que loadComponents() cargó los endpoints correctamente
        // probando que responden como se espera
        
        // Test 1: GreetingController funciona
        URI uri1 = new URI("/greeting");
        byte[] resp1 = HttpServer.handleGetRequest(uri1);
        assertTrue(new String(resp1).contains("Hola Mundo!"), 
                  "loadComponents() debe cargar GreetingController correctamente");
        
        // Test 2: MathController funciona
        URI uri2 = new URI("/add?a=1&b=1");
        byte[] resp2 = HttpServer.handleGetRequest(uri2);
        assertTrue(new String(resp2).contains("Result: 2"), 
                  "loadComponents() debe cargar MathController correctamente");
        
        // Test 3: Endpoints no existentes fallan correctamente
        URI uri3 = new URI("/inexistente");
        byte[] resp3 = HttpServer.handleGetRequest(uri3);
        assertTrue(new String(resp3).contains("404"), 
                  "Endpoints no registrados deben retornar 404");
    }

}