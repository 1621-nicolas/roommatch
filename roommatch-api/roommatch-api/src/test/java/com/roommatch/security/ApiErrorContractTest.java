package com.roommatch.security;

import com.roommatch.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ApiErrorContractTest {
    MockMvc mvc;
    @RestController static class FixtureController {
        @GetMapping("/missing") String missing() { throw new ResourceNotFoundException("No existe"); }
        @GetMapping("/forbidden") String forbidden() { throw new AccessDeniedException("Prohibido"); }
        @GetMapping("/conflict") String conflict() { throw new ConflictException("Estado incompatible"); }
        @GetMapping("/duplicate") String duplicate() { throw new DataIntegrityViolationException("private database payload"); }
        @PostMapping("/json") String json(@RequestBody java.util.Map<String, String> body) { return "ok"; }
    }
    @BeforeEach void setup() {
        mvc = MockMvcBuilders.standaloneSetup(new FixtureController())
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }
    @Test void mapsDomainErrorsAndHidesDatabaseDetails() throws Exception {
        mvc.perform(get("/missing")).andExpect(status().isNotFound()).andExpect(jsonPath("status").value("fail"));
        mvc.perform(get("/forbidden")).andExpect(status().isForbidden());
        mvc.perform(get("/conflict")).andExpect(status().isConflict());
        mvc.perform(get("/duplicate")).andExpect(status().isConflict())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("private database payload"))));
    }
    @Test void malformedJsonIs400RatherThan500() throws Exception {
        mvc.perform(post("/json").contentType("application/json").content("{broken"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("status").value("fail"));
    }
}
