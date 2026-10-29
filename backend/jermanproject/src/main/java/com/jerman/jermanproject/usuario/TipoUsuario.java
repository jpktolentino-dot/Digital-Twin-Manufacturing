
package com.jerman.jermanproject.usuario;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        enumAsRef = true,
        description = """
                Perfis de usuário:
                ADMINISTRADOR: usuário com permissões administrativas.
                OPERADOR: usuário destinado à operação industrial.
                """
)
public enum TipoUsuario {
    ADMINISTRADOR,
    OPERADOR
}
