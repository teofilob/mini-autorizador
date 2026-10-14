package br.com.teofilob.miniautorizador.application.service;

import br.com.teofilob.miniautorizador.application.dto.CriarCartaoCommand;
import br.com.teofilob.miniautorizador.application.dto.ResultadoCriarCartao;
import br.com.teofilob.miniautorizador.application.port.inbound.CriarCartaoUseCase;
import br.com.teofilob.miniautorizador.application.port.outbound.BuscarCartaoPort;
import br.com.teofilob.miniautorizador.application.port.outbound.CriarCartaoPort;
import br.com.teofilob.miniautorizador.domain.exception.CartaoDuplicadoException;
import br.com.teofilob.miniautorizador.domain.model.Cartao;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CriarCartaoService implements CriarCartaoUseCase {

    private static final BigDecimal SALDO_INICIAL = new BigDecimal("1100.00");

    private final BuscarCartaoPort buscarCartaoPort;
    private final CriarCartaoPort criarCartaoPort;

    public CriarCartaoService(BuscarCartaoPort buscarCartaoPort, CriarCartaoPort criarCartaoPort) {
        this.buscarCartaoPort = buscarCartaoPort;
        this.criarCartaoPort = criarCartaoPort;
    }

    @Override
    @Transactional
    public ResultadoCriarCartao criarCartao(CriarCartaoCommand comando) {
        if (buscarCartaoPort.buscarPorNumero(comando.numeroCartao()).isPresent()) {
            throw new CartaoDuplicadoException(comando.numeroCartao(), comando.senha());
        }

        Cartao novoCartao = new Cartao(comando.numeroCartao(), comando.senha(), SALDO_INICIAL);
        try {
            Cartao cartaoSalvo = criarCartaoPort.criarCartao(novoCartao);
            return new ResultadoCriarCartao(cartaoSalvo.numeroCartao(), cartaoSalvo.senha());
        } catch (DataIntegrityViolationException ex) {
            throw new CartaoDuplicadoException(comando.numeroCartao(), comando.senha(), ex);
        }
    }
}
