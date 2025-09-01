/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.escuelaing.arem.ASE.app.Controller;

import edu.escuelaing.arem.ASE.app.annotation.GetMapping;
import edu.escuelaing.arem.ASE.app.annotation.RequestParam;
import edu.escuelaing.arem.ASE.app.annotation.RestController;

/**
 * Controlador que maneja operaciones matemáticas básicas.
 * 
 * Este controlador proporciona endpoints para realizar cálculos matemáticos
 * simples como sumas. Utiliza el sistema de anotaciones del framework
 * para procesar parámetros numéricos de consulta.
 * 
 * @author jgamb
 * @version 1.0
 * @since 1.0
 */
@RestController
public class MathController {
    /**
     * Endpoint con múltiples parámetros GET /add?a=5&b=3 -> "Result: 8"
     * 
     * @param a
     * @param b
     * @return result of the sum operation
     */
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
