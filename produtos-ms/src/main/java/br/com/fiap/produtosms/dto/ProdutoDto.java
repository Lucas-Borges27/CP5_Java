package br.com.fiap.produtosms.dto;

import br.com.fiap.produtosms.entities.Produto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProdutoDto(
        UUID codigo,
        String nome,
        String descricao,
        BigDecimal preco,
        String categoria
) implements Serializable {

    public static ProdutoDto from(Produto produto) {
        return new ProdutoDto(
                produto.getCodigo(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getCategoria()
        );
    }

    public static List<ProdutoDto> from(List<Produto> produtos) {
        return produtos.stream().map(ProdutoDto::from).toList();
    }

    public Produto toEntity() {
        return new Produto(codigo, nome, descricao, preco, categoria);
    }

    public static ProdutoDto empty(UUID codigo) {
        return new ProdutoDto(codigo, null, null, null, null);
    }
}
