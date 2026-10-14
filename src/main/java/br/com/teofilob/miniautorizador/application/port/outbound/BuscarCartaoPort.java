package br.com.teofilob.miniautorizador.application.port.outbound;

import br.com.teofilob.miniautorizador.domain.model.Cartao;

import java.math.BigDecimal;
import java.util.Optional;

public interface BuscarCartaoPort {
    Optional<Cartao> buscarPorNumero(String numeroCartao);
    Optional<Cartao> buscarPorNumeroComLock(String numeroCartao);
    void atualizarSaldo(String numeroCartao, BigDecimal novoSaldo);
}
