package br.com.fiap.produtosms.controller;

import br.com.fiap.produtosms.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    void deveCadastrarUsuarioLocalERedirecionarParaLogin() throws Exception {
        mockMvc.perform(post("/cadastro")
                        .param("login", "user.local")
                        .param("senha", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?cadastroSucesso"));

        verify(usuarioService).cadastrarLocal(argThat(dto ->
                "user.local".equals(dto.login()) && "123456".equals(dto.senha())));
    }

    @Test
    void deveRedirecionarComErroQuandoCadastroFalhar() throws Exception {
        doThrow(new IllegalArgumentException("Login ja cadastrado"))
                .when(usuarioService).cadastrarLocal(argThat(dto -> "user.local".equals(dto.login())));

        mockMvc.perform(post("/cadastro")
                        .param("login", "user.local")
                        .param("senha", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cadastro?erro"));
    }
}
