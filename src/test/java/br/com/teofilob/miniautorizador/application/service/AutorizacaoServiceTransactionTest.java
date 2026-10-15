package br.com.teofilob.miniautorizador.application.service;

import br.com.teofilob.miniautorizador.application.dto.TransacaoCommand;
import br.com.teofilob.miniautorizador.application.port.inbound.AutorizacaoUseCase;
import br.com.teofilob.miniautorizador.application.port.outbound.TransacaoRepositoryPort;
import br.com.teofilob.miniautorizador.infrastructure.adapter.outbound.persistence.cartao.CartaoEntity;
import br.com.teofilob.miniautorizador.infrastructure.adapter.outbound.persistence.cartao.CartaoJpaRepository;
import br.com.teofilob.miniautorizador.infrastructure.adapter.outbound.persistence.cartao.JpaCartaoRepositoryAdapter;
import br.com.teofilob.miniautorizador.infrastructure.adapter.outbound.persistence.transacao.TransacaoJpaRepository;
import br.com.teofilob.miniautorizador.domain.exception.SaldoInsuficienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.IllegalTransactionStateException;

import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Autowired
    private JpaCartaoRepositoryAdapter cartaoRepositoryAdapter;

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

    @Test
    void deveMascararDadosSensiveisAoRepresentarComandoEmLog() {
        String representacao = new TransacaoCommand(
                "1234567890123456", "1234", new BigDecimal("50.00")).toString();

        assertTrue(representacao.contains("************3456"));
        assertTrue(representacao.contains("senha=[OCULTADA]"));
        assertFalse(representacao.contains("1234567890123456"));
        assertFalse(representacao.contains("1234"));
    }

    @Test
    void deveExigirTransacaoAtivaParaBuscarCartaoComLock() {
        assertThrows(IllegalTransactionStateException.class,
                () -> cartaoRepositoryAdapter.buscarPorNumeroComLock("1234567890123456"));
    }

    @Test
    void deveSerializarAutorizacoesConcorrentesParaEvitarDoubleSpending() throws Exception {
        var executor = Executors.newFixedThreadPool(2);
        var inicioSimultaneo = new CyclicBarrier(3);
        Callable<Boolean> autorizar = () -> {
            inicioSimultaneo.await();
            try {
                autorizacaoUseCase.autorizar(new TransacaoCommand(
                        "1234567890123456", "1234", new BigDecimal("300.00")));
                return true;
            } catch (SaldoInsuficienteException ex) {
                return false;
            }
        };

        try {
            var primeiraAutorizacao = executor.submit(autorizar);
            var segundaAutorizacao = executor.submit(autorizar);
            inicioSimultaneo.await(5, TimeUnit.SECONDS);

            int autorizacoesAprovadas = (primeiraAutorizacao.get(10, TimeUnit.SECONDS) ? 1 : 0)
                    + (segundaAutorizacao.get(10, TimeUnit.SECONDS) ? 1 : 0);

            assertEquals(1, autorizacoesAprovadas);
            assertEquals(new BigDecimal("200.00"), cartaoJpaRepository.findById("1234567890123456")
                    .orElseThrow()
                    .getSaldo());
        } finally {
            executor.shutdownNow();
        }
    }
}
