package br.com.teofilob.miniautorizador.application.service;

import br.com.teofilob.miniautorizador.application.dto.CriarCartaoCommand;
import br.com.teofilob.miniautorizador.application.dto.ResultadoCriarCartao;
import br.com.teofilob.miniautorizador.application.port.outbound.BuscarCartaoPort;
import br.com.teofilob.miniautorizador.application.port.outbound.CriarCartaoPort;
import br.com.teofilob.miniautorizador.domain.exception.CartaoDuplicadoException;
import br.com.teofilob.miniautorizador.domain.model.Cartao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarCartaoServiceTest {

    @Mock
    private BuscarCartaoPort buscarCartaoPort;

    @Mock
    private CriarCartaoPort criarCartaoPort;

    @InjectMocks
    private CriarCartaoService criarCartaoService;

    @Test
    void deveCriarCartaoComSaldoInicialDeQuinhentos() {
        var comando = new CriarCartaoCommand("1234567890123456", "1234");
        when(buscarCartaoPort.buscarPorNumero(comando.numeroCartao())).thenReturn(Optional.empty());
        when(criarCartaoPort.criarCartao(any(Cartao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResultadoCriarCartao resultado = criarCartaoService.criarCartao(comando);

        assertEquals(comando.numeroCartao(), resultado.numeroCartao());
        assertEquals(comando.senha(), resultado.senha());
        ArgumentCaptor<Cartao> cartaoCaptor = ArgumentCaptor.forClass(Cartao.class);
        verify(criarCartaoPort).criarCartao(cartaoCaptor.capture());
        assertEquals(new BigDecimal("500.00"), cartaoCaptor.getValue().saldo());
    }

    @Test
    void deveRetornarDadosDoCartaoExistenteQuandoNumeroJaEstaCadastrado() {
        var comando = new CriarCartaoCommand("1234567890123456", "9999");
        var cartaoExistente = new Cartao(comando.numeroCartao(), "1234", new BigDecimal("250.00"));
        when(buscarCartaoPort.buscarPorNumero(comando.numeroCartao()))
                .thenReturn(Optional.of(cartaoExistente));

        CartaoDuplicadoException excecao = assertThrows(
                CartaoDuplicadoException.class,
                () -> criarCartaoService.criarCartao(comando)
        );

        assertEquals(cartaoExistente.numeroCartao(), excecao.getNumeroCartao());
        assertEquals(cartaoExistente.senha(), excecao.getSenha());
    }

    @Test
    void deveConverterDataIntegrityViolationEmDuplicidadeQuandoOutroPedidoCriouCartao() {
        var comando = new CriarCartaoCommand("1234567890123456", "1234");
        var causa = new DataIntegrityViolationException("Número de cartão duplicado");
        var cartaoExistente = new Cartao(comando.numeroCartao(), "5678", new BigDecimal("300.00"));

        when(buscarCartaoPort.buscarPorNumero(comando.numeroCartao()))
                .thenReturn(Optional.empty(), Optional.of(cartaoExistente));
        doThrow(causa).when(criarCartaoPort).criarCartao(any(Cartao.class));

        CartaoDuplicadoException excecao = assertThrows(
                CartaoDuplicadoException.class,
                () -> criarCartaoService.criarCartao(comando)
        );

        assertEquals(comando.numeroCartao(), excecao.getNumeroCartao());
        assertEquals(cartaoExistente.senha(), excecao.getSenha());
        assertSame(causa, excecao.getCause());

        ArgumentCaptor<Cartao> cartaoCaptor = ArgumentCaptor.forClass(Cartao.class);
        verify(criarCartaoPort).criarCartao(cartaoCaptor.capture());
        assertEquals(new BigDecimal("500.00"), cartaoCaptor.getValue().saldo());
    }

    @Test
    void devePropagarDataIntegrityViolationQuandoCartaoNaoExisteAposFalha() {
        var comando = new CriarCartaoCommand("1234567890123456", "1234");
        var causa = new DataIntegrityViolationException("Outra falha de integridade");
        when(buscarCartaoPort.buscarPorNumero(comando.numeroCartao()))
                .thenReturn(Optional.empty(), Optional.empty());
        doThrow(causa).when(criarCartaoPort).criarCartao(any(Cartao.class));

        DataIntegrityViolationException excecao = assertThrows(
                DataIntegrityViolationException.class,
                () -> criarCartaoService.criarCartao(comando)
        );

        assertSame(causa, excecao);
    }
}
