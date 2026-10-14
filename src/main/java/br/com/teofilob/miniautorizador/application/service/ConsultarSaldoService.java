package br.com.teofilob.miniautorizador.application.service;

import br.com.teofilob.miniautorizador.application.port.inbound.ConsultarSaldoUseCase;
import br.com.teofilob.miniautorizador.application.port.outbound.BuscarCartaoPort;
import br.com.teofilob.miniautorizador.domain.model.Cartao;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ConsultarSaldoService implements ConsultarSaldoUseCase {

    private final BuscarCartaoPort buscarCartaoPort;

    public ConsultarSaldoService(BuscarCartaoPort buscarCartaoPort) {
        this.buscarCartaoPort = buscarCartaoPort;
    }

    @Override
    public Optional<BigDecimal> consultarSaldo(String numeroCartao) {
        return buscarCartaoPort.buscarPorNumero(numeroCartao).map(Cartao::saldo);
    }
}
