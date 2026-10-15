package br.com.teofilob.miniautorizador.application.service;

import br.com.teofilob.miniautorizador.application.dto.CriarCartaoCommand;
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
    void deveConverterDataIntegrityViolationEmCartaoDuplicado() {
        var comando = new CriarCartaoCommand("1234567890123456", "1234");
        var causa = new DataIntegrityViolationException("Número de cartão duplicado");

        when(buscarCartaoPort.buscarPorNumero(comando.numeroCartao())).thenReturn(Optional.empty());
        doThrow(causa).when(criarCartaoPort).criarCartao(any(Cartao.class));

        CartaoDuplicadoException excecao = assertThrows(
                CartaoDuplicadoException.class,
                () -> criarCartaoService.criarCartao(comando)
        );

        assertEquals(comando.numeroCartao(), excecao.getNumeroCartao());
        assertEquals(comando.senha(), excecao.getSenha());
        assertSame(causa, excecao.getCause());

        ArgumentCaptor<Cartao> cartaoCaptor = ArgumentCaptor.forClass(Cartao.class);
        verify(criarCartaoPort).criarCartao(cartaoCaptor.capture());
        assertEquals(new BigDecimal("1100.00"), cartaoCaptor.getValue().saldo());
    }
}
