package com.example.phuongstore.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Handles Angular SPA routing.
 * Uses regex [\\w\\-]+ to ONLY match paths without dots.
 * This ensures static files (.html, .js, .css, .png...) are NOT intercepted
 * and are served directly by Spring Boot's static resource handler.
 * API endpoints (@RestController) take priority over these patterns automatically.
 */
@Controller
public class SpaController {

    @RequestMapping(value = {
        "/",
        "/{a:[\\w\\-]+}",
        "/{a:[\\w\\-]+}/{b:[\\w\\-]+}",
        "/{a:[\\w\\-]+}/{b:[\\w\\-]+}/{c:[\\w\\-]+}",
        "/{a:[\\w\\-]+}/{b:[\\w\\-]+}/{c:[\\w\\-]+}/{d:[\\w\\-]+}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}



