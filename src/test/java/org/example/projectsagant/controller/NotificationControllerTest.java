package org.example.projectsagant.controller;

import org.example.projectsagant.dto.CreateNotificationRequest;
import org.example.projectsagant.model.Channel;
import org.example.projectsagant.model.Priority;
import org.example.projectsagant.repository.NotificationRepository;
import org.example.projectsagant.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private NotificationRepository notificationRepository;

    private String bearerToken;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        bearerToken = "Bearer " + jwtService.generateToken("admin");
    }

    @Test
    void create_conDatosValidosYToken_deberiaRetornar201() throws Exception {
        CreateNotificationRequest request = new CreateNotificationRequest(
                "cliente@example.com", Channel.LOG, "Bienvenida", "Gracias por registrarte",
                Priority.MEDIUM, Map.of("origen", "test"));

        mockMvc.perform(post("/api/notifications")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.recipient").value("cliente@example.com"));
    }

    @Test
    void create_sinToken_deberiaRetornar401() throws Exception {
        CreateNotificationRequest request = new CreateNotificationRequest(
                "cliente@example.com", Channel.LOG, "s", "b", Priority.LOW, Map.of());

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_conCamposFaltantes_deberiaRetornar400ConMensajesDescriptivos() throws Exception {
        String invalidJson = "{\"channel\":\"LOG\"}";

        mockMvc.perform(post("/api/notifications")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.recipient").exists());
    }

    @Test
    void getById_cuandoNoExiste_deberiaRetornar404() throws Exception {
        mockMvc.perform(get("/api/notifications/999999")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isNotFound());
    }
}