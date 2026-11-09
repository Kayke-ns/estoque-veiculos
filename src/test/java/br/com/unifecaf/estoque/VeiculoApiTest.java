package br.com.unifecaf.estoque;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integracao da API (rodam com H2 em memoria, perfil "dev").
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class VeiculoApiTest {

    @Autowired
    MockMvc mvc;

    @Test
    void listaVeiculosDaCargaInicial() throws Exception {
        mvc.perform(get("/api/veiculos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(10))));
    }

    @Test
    void filtraPorStatusEPreco() throws Exception {
        mvc.perform(get("/api/veiculos").param("status", "DISPONIVEL").param("precoMax", "100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].status", everyItem(is("DISPONIVEL"))))
                .andExpect(jsonPath("$[*].preco", everyItem(lessThanOrEqualTo(100000.0))));
    }

    @Test
    void cicloCompletoCrud() throws Exception {
        String json = """
                {"modeloId":1,"anoFabricacao":2024,"cor":"Verde","preco":155000,"quilometragem":0,"placa":"TST0A00"}
                """;
        String resposta = mvc.perform(post("/api/veiculos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DISPONIVEL"))
                .andReturn().getResponse().getContentAsString();
        String id = resposta.replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(patch("/api/veiculos/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preco\":149000,\"quilometragem\":150}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preco").value(149000))
                .andExpect(jsonPath("$.quilometragem").value(150));

        mvc.perform(post("/api/veiculos/" + id + "/vender"))
                .andExpect(jsonPath("$.status").value("VENDIDO"));

        mvc.perform(delete("/api/veiculos/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/veiculos/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void rejeitaQuilometragemMenorQueAtual() throws Exception {
        mvc.perform(patch("/api/veiculos/1").contentType(MediaType.APPLICATION_JSON).content("{\"quilometragem\":10}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaDadosInvalidos() throws Exception {
        mvc.perform(post("/api/veiculos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modeloId\":1,\"anoFabricacao\":2024,\"cor\":\"\",\"preco\":-5,\"quilometragem\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    void naoRemoveMarcaComModelos() throws Exception {
        mvc.perform(delete("/api/marcas/1")).andExpect(status().isConflict());
    }
}
