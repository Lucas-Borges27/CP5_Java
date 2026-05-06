package br.com.fiap.produtosms.service;

import br.com.fiap.produtosms.entities.Produto;

import java.util.List;
import java.util.UUID;

public interface ProdutoService {

    Produto findById(UUID codigo);

    List<Produto> findAll();
    
    void saveOrUpdate(Produto produto);

    void deleteById(UUID codigo);
}
