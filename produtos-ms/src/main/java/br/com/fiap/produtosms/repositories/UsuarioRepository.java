package br.com.fiap.produtosms.repositories;

import br.com.fiap.produtosms.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
}
