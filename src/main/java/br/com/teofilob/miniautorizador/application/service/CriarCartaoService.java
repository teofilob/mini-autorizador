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

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CriarCartaoService implements CriarCartaoUseCase {

    private static final BigDecimal SALDO_INICIAL = new BigDecimal("500.00");

    private final BuscarCartaoPort buscarCartaoPort;
    private final CriarCartaoPort criarCartaoPort;

    public CriarCartaoService(BuscarCartaoPort buscarCartaoPort, CriarCartaoPort criarCartaoPort) {
        this.buscarCartaoPort = buscarCartaoPort;
        this.criarCartaoPort = criarCartaoPort;
    }

    @Override
    public ResultadoCriarCartao criarCartao(CriarCartaoCommand comando) {
        Optional<Cartao> cartaoExistente = buscarCartaoPort.buscarPorNumero(comando.numeroCartao());
        if (cartaoExistente.isPresent()) {
            Cartao cartao = cartaoExistente.orElseThrow();
            throw new CartaoDuplicadoException(cartao.numeroCartao(), cartao.senha());
        }

        Cartao novoCartao = new Cartao(comando.numeroCartao(), comando.senha(), SALDO_INICIAL);
        try {
            Cartao cartaoSalvo = criarCartaoPort.criarCartao(novoCartao);
            return new ResultadoCriarCartao(cartaoSalvo.numeroCartao(), cartaoSalvo.senha());
        } catch (DataIntegrityViolationException ex) {
            Optional<Cartao> cartaoCriadoConcorrentemente =
                    buscarCartaoPort.buscarPorNumero(comando.numeroCartao());
            if (cartaoCriadoConcorrentemente.isPresent()) {
                Cartao cartao = cartaoCriadoConcorrentemente.orElseThrow();
                throw new CartaoDuplicadoException(cartao.numeroCartao(), cartao.senha(), ex);
            }
            throw ex;
        }
    }
}
