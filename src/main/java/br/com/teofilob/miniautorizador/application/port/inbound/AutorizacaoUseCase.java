package br.com.teofilob.miniautorizador.application.port.inbound;

import br.com.teofilob.miniautorizador.application.dto.ResultadoAutorizacao;
import br.com.teofilob.miniautorizador.application.dto.TransacaoCommand;

public interface AutorizacaoUseCase {
    ResultadoAutorizacao autorizar(TransacaoCommand comando);
}
