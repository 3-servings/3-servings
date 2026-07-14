package com.sparta.server.threeserving.global.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // 브라우저 테스트용 화면 (templates/index.html)
    @GetMapping("/")
    public String index() {
        return "index";
    }
}
