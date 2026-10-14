package br.com.teofilob.miniautorizador.application.port.outbound;

import br.com.teofilob.miniautorizador.domain.model.Cartao;

public interface CriarCartaoPort {
    Cartao criarCartao(Cartao cartao);
}
