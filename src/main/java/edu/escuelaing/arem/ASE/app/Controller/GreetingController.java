/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.escuelaing.arem.ASE.app.Controller;

import edu.escuelaing.arem.ASE.app.annotation.GetMapping;
import edu.escuelaing.arem.ASE.app.annotation.RequestParam;
import edu.escuelaing.arem.ASE.app.annotation.RestController;

/**
 *
 * @author jgamb
 */
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
