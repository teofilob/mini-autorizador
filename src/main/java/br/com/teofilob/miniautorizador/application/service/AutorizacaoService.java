package br.com.teofilob.miniautorizador.application.service;

import br.com.teofilob.miniautorizador.application.dto.ResultadoAutorizacao;
import br.com.teofilob.miniautorizador.application.dto.TransacaoCommand;
import br.com.teofilob.miniautorizador.application.port.inbound.AutorizacaoUseCase;
import br.com.teofilob.miniautorizador.application.port.outbound.BuscarCartaoPort;
import br.com.teofilob.miniautorizador.domain.enums.StatusAutorizacao;
import br.com.teofilob.miniautorizador.domain.exception.CartaoInexistenteException;
import br.com.teofilob.miniautorizador.domain.exception.SenhaInvalidaException;
import br.com.teofilob.miniautorizador.domain.model.Cartao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AutorizacaoService implements AutorizacaoUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutorizacaoService.class);

    private final BuscarCartaoPort buscarCartaoPort;

    public AutorizacaoService(BuscarCartaoPort buscarCartaoPort) {
        this.buscarCartaoPort = buscarCartaoPort;
    }

    @Override
    @Transactional
    public ResultadoAutorizacao autorizar(TransacaoCommand comando) {
        Cartao cartao = buscarCartaoPort.buscarPorNumeroComLock(comando.numeroCartao())
                .orElseThrow(() -> new CartaoInexistenteException("Cartão inexistente."));

        cartao.validarSenha(comando.senha());

        Cartao cartaoAtualizado = cartao.debitar(comando.valor());
        BigDecimal novoSaldo = cartaoAtualizado.saldo();
        buscarCartaoPort.atualizarSaldo(cartaoAtualizado.numeroCartao(), novoSaldo);
        LOGGER.info("transaction.authorization.approved comando={}", comando);

        return new ResultadoAutorizacao(
                StatusAutorizacao.APROVADA,
                "Transação aprovada.",
                cartaoAtualizado.numeroCartao(),
                novoSaldo
        );
    }
}
