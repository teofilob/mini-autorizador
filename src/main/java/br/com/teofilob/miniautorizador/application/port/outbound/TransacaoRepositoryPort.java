package br.com.teofilob.miniautorizador.application.port.outbound;

import br.com.teofilob.miniautorizador.domain.model.Transacao;

public interface TransacaoRepositoryPort {
    void salvar(Transacao transacao);
}
