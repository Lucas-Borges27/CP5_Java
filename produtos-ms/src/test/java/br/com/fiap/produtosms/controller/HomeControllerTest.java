package br.com.fiap.produtosms.controller;

import org.junit.jupiter.api.Test;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HomeController.class)
@AutoConfigureMockMvc(addFilters = false)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveRenderizarHomeEmInglesQuandoReceberParametroLang() throws Exception {
        mockMvc.perform(get("/").param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Welcome to the product system")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Go to products")));
    }

    @Test
    void deveRenderizarHomeEmPortuguesQuandoReceberParametroLangPtBr() throws Exception {
        mockMvc.perform(get("/").param("lang", "pt-BR"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Bem-vindo ao sistema de produtos")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Ir para produtos")));
    }

    @Test
    void deveSobrescreverCookieEmInglesQuandoReceberParametroLangPtBr() throws Exception {
        mockMvc.perform(get("/")
                        .cookie(new Cookie("produtos-ms-lang", "en"))
                        .param("lang", "pt-BR"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Bem-vindo ao sistema de produtos")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Welcome to the product system"))));
    }

    @Test
    void deveRenderizarTelaDeLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Entrar com GitHub")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Entrar com login e senha")));
    }

    @Test
    void deveRenderizarTelaDeCadastro() throws Exception {
        mockMvc.perform(get("/cadastro"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Cadastro de usuario")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ROLE_USER")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ROLE_PRODUTO")));
    }
}
