package br.com.teofilob.miniautorizador.infrastructure.adapter.inbound.rest;

import br.com.teofilob.miniautorizador.infrastructure.adapter.outbound.persistence.cartao.CartaoEntity;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartaoJpaRepository cartaoJpaRepository;

    @BeforeEach
    void prepararCartoes() {
        cartaoJpaRepository.deleteAll();
        cartaoJpaRepository.save(new CartaoEntity(
                "1234567890123456", "1234", new BigDecimal("500.00")));
        cartaoJpaRepository.save(new CartaoEntity(
                "2345678901234567", "4321", new BigDecimal("750.50")));
    }

    @Test
    void deveAutorizarTransacaoQuandoCartaoExisteEComSaldo() throws Exception {
        String payload = """
                {
                  "numeroCartao": "1234567890123456",
                  "senhaCartao": "1234",
                  "valor": 50.00
                }
                """;

        mockMvc.perform(post("/transacoes")
                        .with(httpBasic("username", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Correlation-ID", "teste-transacao-01")
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("OK"))
                .andExpect(header().string("X-Correlation-ID", "teste-transacao-01"));

        assertEquals(new BigDecimal("450.00"), cartaoJpaRepository.findById("1234567890123456")
                .orElseThrow()
                .getSaldo());
    }

    @Test
    void deveRetornar422QuandoSaldoInsuficiente() throws Exception {
        String payload = """
                {
                  "numeroCartao": "1234567890123456",
                  "senhaCartao": "1234",
                  "valor": 9999.99
                }
                """;

        mockMvc.perform(post("/transacoes")
                        .with(httpBasic("username", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SALDO_INSUFICIENTE"));

        assertSaldoNaoAlterado();
    }

    @Test
    void deveRetornar422QuandoSenhaInvalida() throws Exception {
        String payload = """
                {
                  "numeroCartao": "1234567890123456",
                  "senhaCartao": "9999",
                  "valor": 10.00
                }
                """;

        mockMvc.perform(post("/transacoes")
                        .with(httpBasic("username", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SENHA_INVALIDA"));

        assertSaldoNaoAlterado();
    }

    @Test
    void deveRetornar422QuandoCartaoNaoExiste() throws Exception {
        String payload = """
                {
                  "numeroCartao": "9999999999999999",
                  "senhaCartao": "1234",
                  "valor": 10.00
                }
                """;

        mockMvc.perform(post("/transacoes")
                        .with(httpBasic("username", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("CARTAO_INEXISTENTE"));
    }

    @Test
    void deveRetornar401AoAutorizarTransacaoSemAutenticacao() throws Exception {
        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "numeroCartao": "1234567890123456",
                                  "senhaCartao": "1234",
                                  "valor": 50.00
                                }
                                """))
                .andExpect(status().isUnauthorized());

        assertSaldoNaoAlterado();
    }

    @Test
    void deveRetornar401AoAutorizarTransacaoComCredenciaisInvalidas() throws Exception {
        mockMvc.perform(post("/transacoes")
                        .with(httpBasic("username", "senha-incorreta"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "numeroCartao": "1234567890123456",
                                  "senhaCartao": "1234",
                                  "valor": 50.00
                                }
                                """))
                .andExpect(status().isUnauthorized());

        assertSaldoNaoAlterado();
    }

    private void assertSaldoNaoAlterado() {
        assertEquals(new BigDecimal("500.00"), cartaoJpaRepository.findById("1234567890123456")
                .orElseThrow()
                .getSaldo());
    }
}
