package br.com.teofilob.miniautorizador.infrastructure.adapter.outbound.persistence.cartao;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartaoJpaRepository extends JpaRepository<CartaoEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select cartao from CartaoEntity cartao where cartao.numeroCartao = :numeroCartao")
    Optional<CartaoEntity> findByNumeroCartaoComLock(@Param("numeroCartao") String numeroCartao);
}
