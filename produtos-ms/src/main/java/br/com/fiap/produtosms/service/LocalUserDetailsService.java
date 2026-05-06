package br.com.fiap.produtosms.service;

import br.com.fiap.produtosms.entities.Usuario;
import br.com.fiap.produtosms.repositories.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class LocalUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public LocalUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findById(username)
                .filter(item -> item.getSenha() != null && !item.getSenha().isBlank())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));

        return new User(
                usuario.getLogin(),
                usuario.getSenha(),
                usuario.getRoles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet())
        );
    }
}
