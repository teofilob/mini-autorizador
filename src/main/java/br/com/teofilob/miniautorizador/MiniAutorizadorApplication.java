package br.com.teofilob.miniautorizador;

import br.com.teofilob.miniautorizador.application.port.outbound.CriarCartaoPort;
import br.com.teofilob.miniautorizador.domain.model.Cartao;
import br.com.teofilob.miniautorizador.infrastructure.adapter.outbound.persistence.CartaoEntity;
import br.com.teofilob.miniautorizador.infrastructure.adapter.outbound.persistence.CartaoJpaRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
public class MiniAutorizadorApplication {
    public static void main(String[] args) {
        SpringApplication.run(MiniAutorizadorApplication.class, args);
    }


}
