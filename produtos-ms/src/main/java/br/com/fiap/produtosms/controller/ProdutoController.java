package br.com.fiap.produtosms.controller;

import br.com.fiap.produtosms.dto.ProdutoDto;
import br.com.fiap.produtosms.service.ProdutoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.NoSuchElementException;
import java.util.UUID;

@Controller
@RequestMapping("/produtos")
public class ProdutoController extends CommonController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("produtos", ProdutoDto.from(this.produtoService.findAll()));
        return "produtos";
    }

    @GetMapping("/novo")
    public String novo() {
        return "redirect:/produtos/detalhe/" + UUID.randomUUID();
    }

    @GetMapping("/detalhe/{codigo}")
    public String detalhe(@PathVariable UUID codigo, Model model) {
        ProdutoDto produto;
        try {
            produto = ProdutoDto.from(this.produtoService.findById(codigo));
        } catch (NoSuchElementException exception) {
            produto = ProdutoDto.empty(codigo);
        }

        model.addAttribute("produto", produto);
        return "detalhe-produto";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute ProdutoDto produtoDto) {
        this.produtoService.saveOrUpdate(produtoDto.toEntity());
        return "redirect:/produtos";
    }

    @PostMapping("/excluir")
    public String excluir(@RequestParam UUID codigo) {
        this.produtoService.deleteById(codigo);
        return "redirect:/produtos";
    }
}
