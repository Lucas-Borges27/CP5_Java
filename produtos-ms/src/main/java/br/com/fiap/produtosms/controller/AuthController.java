package br.com.fiap.produtosms.controller;

import br.com.fiap.produtosms.dto.CadastroUsuarioDto;
import br.com.fiap.produtosms.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController extends CommonController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/cadastro")
    public String cadastrar(@ModelAttribute CadastroUsuarioDto cadastroUsuarioDto) {
        try {
            usuarioService.cadastrarLocal(cadastroUsuarioDto);
            return "redirect:/login?cadastroSucesso";
        } catch (IllegalArgumentException ex) {
            return "redirect:/cadastro?erro";
        }
    }
}
