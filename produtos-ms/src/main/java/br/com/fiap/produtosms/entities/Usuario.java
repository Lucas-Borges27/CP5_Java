package br.com.fiap.produtosms.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Usuario {

    @Id
    private String login;

    private String senha;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuarios_roles", joinColumns = @JoinColumn(name = "login"))
    @Column(name = "role")
    private Set<String> roles;

    public Usuario() {
        this.roles = new HashSet<>();
    }

    public Usuario(String login) {
        this.login = login;
        this.roles = new HashSet<>();
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void addRole(String role) {
        this.roles.add(role);
    }
}
