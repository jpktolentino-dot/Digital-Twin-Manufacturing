package com.jermam.repository;

import com.jermam.entity.Usuario;
import com.jermam.enums.TipoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByLoginEmailIgnoreCase(String loginEmail);
    boolean existsByLoginEmailIgnoreCase(String loginEmail);
    boolean existsByLoginEmailIgnoreCaseAndIdNot(String loginEmail, Long id);
    List<Usuario> findByTipo(TipoUsuario tipo);
}
