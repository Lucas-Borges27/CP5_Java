package br.com.fiap.produtosms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController extends CommonController {

    @GetMapping
    public String index() {
        return "home";
    }

    @GetMapping("login")
    public String login() {
        return "login";
    }

    @GetMapping("cadastro")
    public String cadastro() {
        return "cadastro";
    }

    @GetMapping("403")
    public String error403() {
        return "403";
    }
}
