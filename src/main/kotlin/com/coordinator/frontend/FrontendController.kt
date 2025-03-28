package com.coordinator.frontend

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class FrontendController {
    @GetMapping("/", "/{path:^(?!api).*}$")
    fun forward(): String {
        return "forward:/index.html"
    }
}
