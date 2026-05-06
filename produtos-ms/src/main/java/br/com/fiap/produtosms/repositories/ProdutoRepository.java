package br.com.fiap.produtosms.repositories;

import br.com.fiap.produtosms.entities.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProdutoRepository extends JpaRepository<Produto, UUID> {
}
