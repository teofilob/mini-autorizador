package br.com.teofilob.miniautorizador.application.port.inbound;

import br.com.teofilob.miniautorizador.application.dto.CriarCartaoCommand;
import br.com.teofilob.miniautorizador.application.dto.ResultadoCriarCartao;

public interface CriarCartaoUseCase {
    ResultadoCriarCartao criarCartao(CriarCartaoCommand comando);
}
