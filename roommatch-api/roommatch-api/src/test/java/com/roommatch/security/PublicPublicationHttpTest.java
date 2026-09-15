package com.roommatch.security;

import com.roommatch.controller.PublicacionRoomieController;
import com.roommatch.dto.PublicacionRoomieResponse;
import com.roommatch.service.PublicacionRoomieService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PublicPublicationHttpTest {
    @Test
    void publicDetailAcceptsVisitorWithoutPrincipal() throws Exception {
        var service = mock(PublicacionRoomieService.class);
        when(service.obtenerPublicacion(null, 7)).thenReturn(new PublicacionRoomieResponse());
        var mvc = MockMvcBuilders.standaloneSetup(new PublicacionRoomieController(service)).build();
        mvc.perform(get("/api/publicaciones-roomie/7")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("success"));
        verify(service).obtenerPublicacion(null, 7);
    }
}
