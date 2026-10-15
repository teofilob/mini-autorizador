package br.com.teofilob.miniautorizador.infrastructure.adapter.outbound.persistence.transacao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransacaoJpaRepository extends JpaRepository<TransacaoJpaEntity, UUID> {
}
