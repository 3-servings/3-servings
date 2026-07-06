package com.sparta.server.threeserving.review.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewController {

    @GetMapping("/hello")
    public String hello(){
        return "Hello Logging TEST!!";
    }
}
