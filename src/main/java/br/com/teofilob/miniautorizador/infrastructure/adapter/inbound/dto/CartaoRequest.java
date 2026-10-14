package br.com.teofilob.miniautorizador.infrastructure.adapter.inbound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CartaoRequest (
        @NotBlank(message = "Número do cartão é obrigatório")
        @Pattern(regexp = "\\d{16}", message = "Número do cartão deve conter 16 dígitos")
        String numeroCartao,

        @NotBlank(message = "Senha é obrigatória")
        @Pattern(regexp = "\\d{4}", message = "Senha deve conter 4 dígitos")
        String senha){
}