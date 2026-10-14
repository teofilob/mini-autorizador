package br.com.teofilob.miniautorizador.application.service;

import br.com.teofilob.miniautorizador.application.dto.TransacaoCommand;
import br.com.teofilob.miniautorizador.application.port.inbound.AutorizacaoUseCase;
import br.com.teofilob.miniautorizador.application.port.outbound.TransacaoRepositoryPort;
import br.com.teofilob.miniautorizador.infrastructure.adapter.outbound.persistence.CartaoEntity;
import br.com.teofilob.miniautorizador.infrastructure.adapter.outbound.persistence.CartaoJpaRepository;
import br.com.teofilob.miniautorizador.infrastructure.adapter.outbound.persistence.TransacaoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class AutorizacaoServiceTransactionTest {

    @Autowired
    private AutorizacaoUseCase autorizacaoUseCase;

    @Autowired
    private CartaoJpaRepository cartaoJpaRepository;

    @Autowired
    private TransacaoJpaRepository transacaoJpaRepository;

    @MockitoBean
    private TransacaoRepositoryPort transacaoRepositoryPort;

    @BeforeEach
    void prepararCartao() {
        transacaoJpaRepository.deleteAll();
        cartaoJpaRepository.deleteAll();
        cartaoJpaRepository.save(new CartaoEntity(
                "1234567890123456", "1234", new BigDecimal("500.00")));
    }

    @Test
    void deveReverterDebitoQuandoFalhaAoPersistirTransacao() {
        doThrow(new IllegalStateException("Falha simulada ao persistir transação"))
                .when(transacaoRepositoryPort)
                .salvar(any());

        assertThrows(IllegalStateException.class, () -> autorizacaoUseCase.autorizar(
                new TransacaoCommand("1234567890123456", "1234", new BigDecimal("50.00"))
        ));

        assertEquals(new BigDecimal("500.00"), cartaoJpaRepository.findById("1234567890123456")
                .orElseThrow()
                .getSaldo());
        assertEquals(0, transacaoJpaRepository.count());
    }
}
