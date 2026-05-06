package br.com.fiap.produtosms.controller;

import br.com.fiap.produtosms.entities.Produto;
import br.com.fiap.produtosms.service.ProdutoService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProdutoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProdutoService produtoService;

    @Test
    void deveSalvarProdutoComDadosDoFormulario() throws Exception {
        var codigo = UUID.randomUUID();

        mockMvc.perform(post("/produtos/salvar")
                        .param("codigo", codigo.toString())
                        .param("nome", "Notebook")
                        .param("descricao", "Notebook gamer")
                        .param("preco", "5999.90")
                        .param("categoria", "Eletronicos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos"));

        var captor = ArgumentCaptor.forClass(Produto.class);
        verify(produtoService).saveOrUpdate(captor.capture());

        var produto = captor.getValue();
        assertThat(produto.getCodigo()).isEqualTo(codigo);
        assertThat(produto.getNome()).isEqualTo("Notebook");
        assertThat(produto.getDescricao()).isEqualTo("Notebook gamer");
        assertThat(produto.getPreco()).isEqualByComparingTo(new BigDecimal("5999.90"));
        assertThat(produto.getCategoria()).isEqualTo("Eletronicos");
    }
}
