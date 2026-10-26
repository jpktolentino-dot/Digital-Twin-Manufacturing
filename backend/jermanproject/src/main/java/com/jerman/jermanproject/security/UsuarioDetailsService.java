package com.jerman.jermanproject.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.jerman.jermanproject.usuario.UsuarioEntity;
import com.jerman.jermanproject.usuario.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        UsuarioEntity usuario = usuarioRepository
                .findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado."
                ));

        return User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .roles(usuario.getTipo().name())
                .disabled(!usuario.isAtivo())
                .build();
    }
}