package com.roommatch.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityHttpTest {
    @Autowired MockMvc mvc;
    @Autowired com.roommatch.service.ReporteService reports;

    @Test @WithMockUser(roles = "USUARIO") void serviceMethodCannotBeCalledWithAForgedAdminId() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> reports.sancionarUsuario(99,1,"Intento sin permiso"))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }
    @Test @WithMockUser(roles = "USUARIO") void reportHistoryIsNotPublicToAnAuthenticatedUser() throws Exception {
        mvc.perform(get("/api/reportes/admin/usuarios/1/historial")).andExpect(status().isForbidden());
        mvc.perform(put("/api/reportes/admin/usuarios/1/sancionar").contentType("application/json").content("{\"motivo\":\"sin permiso\"}"))
                .andExpect(status().isForbidden());
    }

    @Test void privateRouteIs401Json() throws Exception {
        mvc.perform(get("/api/habitaciones/mis")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("status").value("error"));
    }
    @Test void publicCatalogDoesNotRequireAuthentication() throws Exception {
        mvc.perform(get("/api/planes")).andExpect(status().isOk());
    }
    @Test @WithMockUser(roles = "USUARIO") void adminRequiresServerRole() throws Exception {
        mvc.perform(get("/api/admin/dashboard")).andExpect(status().isForbidden())
                .andExpect(jsonPath("status").value("error"));
    }
    @Test void invalidTokenOnPrivateRouteIs401() throws Exception {
        mvc.perform(get("/api/habitaciones/mis").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }
    @Test void docsAreClosedOutsideExplicitDevelopment() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isUnauthorized());
    }
}
