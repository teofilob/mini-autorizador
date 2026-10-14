package br.com.teofilob.miniautorizador.application.service;

import br.com.teofilob.miniautorizador.application.dto.ResultadoAutorizacao;
import br.com.teofilob.miniautorizador.application.dto.TransacaoCommand;
import br.com.teofilob.miniautorizador.application.port.inbound.AutorizacaoUseCase;
import br.com.teofilob.miniautorizador.application.port.outbound.BuscarCartaoPort;
import br.com.teofilob.miniautorizador.application.port.outbound.TransacaoRepositoryPort;
import br.com.teofilob.miniautorizador.domain.enums.StatusAutorizacao;
import br.com.teofilob.miniautorizador.domain.exception.CartaoInexistenteException;
import br.com.teofilob.miniautorizador.domain.exception.SaldoInsuficienteException;
import br.com.teofilob.miniautorizador.domain.exception.SenhaInvalidaException;
import br.com.teofilob.miniautorizador.domain.model.Cartao;
import br.com.teofilob.miniautorizador.domain.model.Transacao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AutorizacaoService implements AutorizacaoUseCase {

    private final BuscarCartaoPort buscarCartaoPort;
    private final TransacaoRepositoryPort transacaoRepositoryPort;

    public AutorizacaoService(
            BuscarCartaoPort buscarCartaoPort,
            TransacaoRepositoryPort transacaoRepositoryPort) {
        this.buscarCartaoPort = buscarCartaoPort;
        this.transacaoRepositoryPort = transacaoRepositoryPort;
    }

    @Override
    @Transactional
    public ResultadoAutorizacao autorizar(TransacaoCommand comando) {
        Cartao cartao = buscarCartaoPort.buscarPorNumeroComLock(comando.numeroCartao())
                .orElseThrow(() -> new CartaoInexistenteException("Cartão inexistente."));

        if (!cartao.senhaValida(comando.senha())) {
            throw new SenhaInvalidaException("Senha inválida.");
        }

        if (!cartao.possuiSaldoPara(comando.valor())) {
            throw new SaldoInsuficienteException("Saldo insuficiente.");
        }

        BigDecimal novoSaldo = cartao.saldo().subtract(comando.valor());
        buscarCartaoPort.atualizarSaldo(cartao.numeroCartao(), novoSaldo);
        transacaoRepositoryPort.salvar(new Transacao(
                cartao.numeroCartao(),
                comando.valor(),
                LocalDateTime.now()
        ));

        return new ResultadoAutorizacao(
                StatusAutorizacao.APROVADA,
                "Transação aprovada.",
                cartao.numeroCartao(),
                novoSaldo
        );
    }
}
