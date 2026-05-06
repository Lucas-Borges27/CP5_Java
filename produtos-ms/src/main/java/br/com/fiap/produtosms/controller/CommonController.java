package br.com.fiap.produtosms.controller;

import br.com.fiap.produtosms.utils.GitHubUserUtils;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

public abstract class CommonController {

    @ModelAttribute
    public void preProcessor(Model model, Authentication authentication) {
        model.addAttribute("username", GitHubUserUtils.getUsername(authentication));
        model.addAttribute("urlAvatar", GitHubUserUtils.getAvatar(authentication));
    }
}
