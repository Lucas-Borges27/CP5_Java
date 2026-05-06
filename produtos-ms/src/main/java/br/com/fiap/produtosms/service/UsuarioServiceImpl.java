package br.com.fiap.produtosms.service;

import br.com.fiap.produtosms.dto.CadastroUsuarioDto;
import br.com.fiap.produtosms.entities.Usuario;
import br.com.fiap.produtosms.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void cadastrarLocal(CadastroUsuarioDto cadastroUsuarioDto) {
        String login = cadastroUsuarioDto.login() == null ? "" : cadastroUsuarioDto.login().trim();

        if (login.isBlank()
                || cadastroUsuarioDto.senha() == null || cadastroUsuarioDto.senha().isBlank()) {
            throw new IllegalArgumentException("Login e senha sao obrigatorios");
        }

        if (usuarioRepository.existsById(login)) {
            throw new IllegalArgumentException("Login ja cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setLogin(login);
        usuario.setSenha(passwordEncoder.encode(cadastroUsuarioDto.senha()));
        usuario.addRole("ROLE_USER");

        usuarioRepository.save(usuario);
    }
}
