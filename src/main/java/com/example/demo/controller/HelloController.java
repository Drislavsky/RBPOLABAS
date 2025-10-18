package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String home() {
        return "PUKEKO NET";
    }

    @GetMapping("/info")
    public String info() {
        return "INFORMATION!!!";
    }
}
