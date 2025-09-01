/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.escuelaing.arem.ASE.app.Controller;

import edu.escuelaing.arem.ASE.app.annotation.GetMapping;
import edu.escuelaing.arem.ASE.app.annotation.RequestParam;
import edu.escuelaing.arem.ASE.app.annotation.RestController;

/**
 * Controlador que maneja endpoints de saludo y bienvenida.
 * 
 * Este controlador proporciona endpoints básicos para saludar usuarios.
 * Utiliza el sistema de anotaciones del framework para definir rutas
 * y procesar parámetros de consulta automáticamente.
 * 
 * @author jgamb
 * @version 1.0
 * @since 1.0
 */
@RestController
public class GreetingController {

    /**
     * Endpoint que devuelve un saludo básico.
     * 
     * @param name Nombre del usuario (parámetro de consulta)
     * @return Mensaje de saludo
     */
    @GetMapping("/greeting")
    public static String greeting(@RequestParam String name) {
        return "Hola Mundo!";
    }

    /**
     * Endpoint que devuelve un saludo personalizado.
     * 
     * @param name Nombre del usuario (parámetro de consulta)
     * @return Mensaje de saludo personalizado
     */

    @GetMapping("/hello")
    public static String sayHello(@RequestParam("name") String name) {
        return "Hola, " + name + "!";
    }

}
