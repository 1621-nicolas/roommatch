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
