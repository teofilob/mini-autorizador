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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AutorizacaoService implements AutorizacaoUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutorizacaoService.class);

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

        // Valida a senha (lança SenhaInvalidaException se falhar)
        cartao.validarSenha(comando.senha());

        // Debita o saldo (lança SaldoInsuficienteException se falhar) e retorna a nova instância de Cartao'
        cartao.debitar(comando.valor());

        BigDecimal novoSaldo = cartao.saldo().subtract(comando.valor());
        buscarCartaoPort.atualizarSaldo(cartao.numeroCartao(), novoSaldo);
        transacaoRepositoryPort.salvar(new Transacao(
                cartao.numeroCartao(),
                comando.valor(),
                LocalDateTime.now()
        ));
        LOGGER.info("transaction.authorization.approved comando={}", comando);

        return new ResultadoAutorizacao(
                StatusAutorizacao.APROVADA,
                "Transação aprovada.",
                cartao.numeroCartao(),
                novoSaldo
        );
    }



}
