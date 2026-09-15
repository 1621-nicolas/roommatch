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

@SpringBootTest(properties = {"springdoc.api-docs.enabled=true", "springdoc.swagger-ui.enabled=true"})
@ActiveProfiles("test")
@AutoConfigureMockMvc
class DocsSecurityTest {
    @Autowired MockMvc mvc;
    @Test void enablingSpringdocAloneDoesNotExposeDocsOutsideDevelopment() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isUnauthorized());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isUnauthorized());
    }
    @Test @WithMockUser(roles="ADMIN") void closedProductionStyleDocsAreNotOpenedByAnAdminToken() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isForbidden());
    }
}
