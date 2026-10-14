package br.com.teofilob.miniautorizador.infrastructure.adapter.inbound.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record TransacaoRequest(
        @NotBlank(message = "Número do cartão é obrigatório")
        @Pattern(regexp = "\\d{16}", message = "Número do cartão deve conter 16 dígitos")
        String numeroCartao,

        @NotBlank(message = "Senha é obrigatória")
        @Pattern(regexp = "\\d{4}", message = "Senha deve conter 4 dígitos")
        String senhaCartao,

        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.01", inclusive = true, message = "Valor da transação deve ser maior que zero")
        BigDecimal valor
) {
}
