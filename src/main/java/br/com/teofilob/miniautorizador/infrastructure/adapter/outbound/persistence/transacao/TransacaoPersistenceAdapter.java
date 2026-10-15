package br.com.teofilob.miniautorizador.infrastructure.adapter.outbound.persistence.transacao;

import br.com.teofilob.miniautorizador.application.port.outbound.TransacaoRepositoryPort;
import br.com.teofilob.miniautorizador.domain.model.Transacao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class TransacaoPersistenceAdapter implements TransacaoRepositoryPort {

    private final TransacaoJpaRepository transacaoJpaRepository;

    public TransacaoPersistenceAdapter(TransacaoJpaRepository transacaoJpaRepository) {
        this.transacaoJpaRepository = transacaoJpaRepository;
    }

    @Override
    @Transactional
    public void salvar(Transacao transacao) {
        transacaoJpaRepository.save(new TransacaoJpaEntity(
                transacao.numeroCartao(),
                transacao.valor(),
                transacao.dataHora()
        ));
    }
}
