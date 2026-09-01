package br.com.autocarehub.interfaces.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class SwaggerUiController {

    @GetMapping("/swagger-ui/index.html")
    String swaggerUiIndex() {
        return "forward:/swagger-ui-static.html";
    }
}
