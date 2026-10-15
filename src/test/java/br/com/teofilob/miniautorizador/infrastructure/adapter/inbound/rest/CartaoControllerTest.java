package br.com.teofilob.miniautorizador.infrastructure.adapter.inbound.rest;

import br.com.teofilob.miniautorizador.infrastructure.adapter.outbound.persistence.cartao.CartaoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.security.username=username",
        "app.security.password=password"
})
@AutoConfigureMockMvc
class CartaoControllerTest {

    private static final String NUMERO_CARTAO = "6549873025634501";
    private static final String SENHA = "1234";
    private static final String PAYLOAD = """
            {
              "senha": "1234",
              "numeroCartao": "6549873025634501"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartaoJpaRepository cartaoJpaRepository;

    @BeforeEach
    void limparCartoes() {
        cartaoJpaRepository.deleteAll();
    }

    @Test
    void deveCriarCartaoEArmazenarNaBase() throws Exception {
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.senha").value(SENHA))
                .andExpect(jsonPath("$.numeroCartao").value(NUMERO_CARTAO));

        assertEquals(SENHA, cartaoJpaRepository.findById(NUMERO_CARTAO).orElseThrow().getSenha());
        assertEquals(new BigDecimal("1100.00"),
                cartaoJpaRepository.findById(NUMERO_CARTAO).orElseThrow().getSaldo());
    }

    @Test
    void deveRetornar422ComOsDadosEnviadosQuandoCartaoJaExiste() throws Exception {
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.senha").value(SENHA))
                .andExpect(jsonPath("$.numeroCartao").value(NUMERO_CARTAO));
    }

    @Test
    void deveRetornarSaldoDoCartaoComAutenticacaoBasica() throws Exception {
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "numeroCartao": "6549873025634501",
                                  "senhaCartao": "1234",
                                  "valor": 4.85
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/cartoes/{numeroCartao}", NUMERO_CARTAO)
                        .with(httpBasic("username", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("1095.15"));
    }

    @Test
    void deveRetornar404SemBodyQuandoCartaoNaoExiste() throws Exception {
        mockMvc.perform(get("/cartoes/{numeroCartao}", NUMERO_CARTAO)
                        .with(httpBasic("username", "password")))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    void deveRetornar401QuandoAutenticacaoBasicaEstaAusente() throws Exception {
        mockMvc.perform(get("/cartoes/{numeroCartao}", NUMERO_CARTAO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar401QuandoAsCredenciaisEstaoIncorretas() throws Exception {
        mockMvc.perform(get("/cartoes/{numeroCartao}", NUMERO_CARTAO)
                        .with(httpBasic("username", "senha-incorreta")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveExporHealthDoActuatorSemAutenticacao() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void deveExporInformacoesDaAplicacaoNoActuator() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("mini-autorizador"))
                .andExpect(jsonPath("$.app.description").exists());
    }
}
