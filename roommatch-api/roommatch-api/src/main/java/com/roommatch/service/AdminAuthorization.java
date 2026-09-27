package com.roommatch.service;

import com.roommatch.model.Usuario;
import com.roommatch.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class AdminAuthorization {
    private final UsuarioRepository users;
    public AdminAuthorization(UsuarioRepository users) { this.users = users; }
    public Usuario require(Integer id) {
        Usuario user = id == null ? null : users.findById(id).orElse(null);
        if (user == null || !"activo".equals(user.getEstado()) || user.getRol() == null || !"ADMIN".equals(user.getRol().getNombreRol()))
            throw new AccessDeniedException("No tienes permisos de administrador");
        return user;
    }
}
