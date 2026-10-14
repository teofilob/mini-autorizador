package br.com.teofilob.miniautorizador.infrastructure.adapter.outbound.persistence;

import br.com.teofilob.miniautorizador.application.port.outbound.CriarCartaoPort;
import br.com.teofilob.miniautorizador.domain.model.Cartao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaCriarCartaoRepositoryAdapter implements CriarCartaoPort {

    private final CartaoJpaRepository cartaoJpaRepository;

    public JpaCriarCartaoRepositoryAdapter(CartaoJpaRepository cartaoJpaRepository) {
        this.cartaoJpaRepository = cartaoJpaRepository;
    }

    @Override
    @Transactional
    public Cartao criarCartao(Cartao cartao) {
        CartaoEntity entity = new CartaoEntity(cartao.numeroCartao(), cartao.senha(), cartao.saldo());
        return toDomain(cartaoJpaRepository.saveAndFlush(entity));
    }

    private Cartao toDomain(CartaoEntity entity) {
        return new Cartao(entity.getNumeroCartao(), entity.getSenha(), entity.getSaldo());
    }
}