package br.com.teofilob.miniautorizador.application.port.inbound;

import java.math.BigDecimal;
import java.util.Optional;

public interface ConsultarSaldoUseCase {
    Optional<BigDecimal> consultarSaldo(String numeroCartao);
}
