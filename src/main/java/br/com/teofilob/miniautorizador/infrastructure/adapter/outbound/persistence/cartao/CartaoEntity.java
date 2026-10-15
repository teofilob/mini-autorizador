package br.com.teofilob.miniautorizador.infrastructure.adapter.outbound.persistence.cartao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "cartoes")
public class CartaoEntity {

    @Id
    @Column(name = "numero_cartao", length = 16, nullable = false)
    private String numeroCartao;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    protected CartaoEntity() {
    }

    public CartaoEntity(String numeroCartao, String senha, BigDecimal saldo) {
        this.numeroCartao = numeroCartao;
        this.senha = senha;
        this.saldo = saldo;
    }

    public String getNumeroCartao() {
        return numeroCartao;
    }

    public String getSenha() {
        return senha;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }
}
