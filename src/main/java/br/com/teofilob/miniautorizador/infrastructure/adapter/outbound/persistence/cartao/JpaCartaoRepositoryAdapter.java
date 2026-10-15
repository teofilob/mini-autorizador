package br.com.teofilob.miniautorizador.infrastructure.adapter.outbound.persistence.cartao;

import br.com.teofilob.miniautorizador.application.port.outbound.BuscarCartaoPort;
import br.com.teofilob.miniautorizador.domain.model.Cartao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public class JpaCartaoRepositoryAdapter implements BuscarCartaoPort {

    private final CartaoJpaRepository cartaoJpaRepository;

    public JpaCartaoRepositoryAdapter(CartaoJpaRepository cartaoJpaRepository) {
        this.cartaoJpaRepository = cartaoJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cartao> buscarPorNumero(String numeroCartao) {
        return cartaoJpaRepository.findById(numeroCartao)
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public Optional<Cartao> buscarPorNumeroComLock(String numeroCartao) {
        return cartaoJpaRepository.findByNumeroCartaoComLock(numeroCartao)
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public void atualizarSaldo(String numeroCartao, BigDecimal novoSaldo) {
        cartaoJpaRepository.findById(numeroCartao)
                .ifPresent(cartao -> {
                    cartao.setSaldo(novoSaldo);
                    cartaoJpaRepository.save(cartao);
                });
    }

    private Cartao toDomain(CartaoEntity entity) {
        return new Cartao(entity.getNumeroCartao(), entity.getSenha(), entity.getSaldo());
    }


}
