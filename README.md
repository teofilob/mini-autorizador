# 📝 Mini Autorizador

API REST para cadastro de cartões, consulta de saldo e autorização de transações. O projeto usa Spring Boot, Java 25, Spring Data JPA e MySQL, organizado em camadas inspiradas em arquitetura hexagonal.

## 🛠 Ferramentas & Produtividade
O desenvolvimento deste projeto contou com o apoio de ferramentas de Inteligência Artificial Generativa (GitHub Copilot / LLMs) atuando como assistente de produtividade para geração de código boilerplate e auxílio na construção de cenários de testes automatizados, mantendo o controle total da arquitetura, regras de negócio e decisões de design sob responsabilidade técnica do desenvolvedor.

## 🏛 Decisões de Arquitetura & Escolha do Banco de Dados

Para este projeto, optou-se pela utilização do **MySQL** (banco de dados relacional) em detrimento de uma solução NoSQL como o MongoDB. As razões técnicas que motivaram essa escolha incluem:

### 1. Garantia Estrita de Propriedades ACID e Consistência Imediata (*Immediate Consistency*)
Em sistemas financeiros e autorizadores de pagamento, a consistência de dados precisa ser estrita e imediata. Soluções NoSQL frequentemente operam sob o modelo de consistência eventual (*eventual consistency*), o que pode gerar inconsistências em requisições de leitura e débito de saldo em curto intervalo de tempo. O MySQL garante atomicidade e consistência imediata a cada transação.

### 2. Prevenção de *Double Spending* via Pessimistic Locking (`FOR UPDATE`)
O principal desafio do mini-autorizador é garantir que requisições concorrentes disparadas simultaneamente para o mesmo cartão (em instâncias distintas da aplicação) não causem *double spending* (débito duplo indevido).

A escolha do motor **InnoDB do MySQL** permite a aplicação de **Pessimistic Write Locking** (`SELECT ... FOR UPDATE`) nativo no nível de linha (*Row-Level Locking*):
- No início do fluxo de autorização (`@Transactional`), a consulta obtém o bloqueio exclusivo da linha do cartão.
- Quaisquer requisições concorrentes paralelas para o mesmo cartão são forçadas pelo banco de dados a aguardar até a consolidação (`COMMIT`) ou reversão (`ROLLBACK`) da primeira transação.
- Isso garante que a segunda transação leia o saldo já atualizado e falhe na regra de negócio (`SALDO_INSUFICIENTE`), sem gerar a necessidade de mecanismos complexos de travas distribuídas.

### 3. Integridade de Dados no Nível da Base
Utilização de restrições de unicidade (`UNIQUE constraint`) no campo `numero_cartao`, prevenindo a criação duplicada de cartões em cenários de concorrência.


## Tecnologias

- Java 25 e Spring Boot 3.5
- Spring Web, Validation, Security e Data JPA
- MySQL para execução da aplicação e H2 para testes
- Maven e JaCoCo
- Logback com MDC para correlação de requisições

## Requisitos

- JDK 25
- Maven 3.9 ou superior
- Docker e Docker Compose, ou uma instância MySQL compatível




## Executar localmente

O arquivo `docker-compose.yml` inicia um MySQL na porta `3306` e cria o banco `miniautorizador`:

```bash
docker compose up -d mysql
```

Configure a conexão da aplicação usando variáveis de ambiente. O exemplo abaixo corresponde às configurações do serviço MySQL definido no Compose; altere os valores para seu ambiente:

```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=miniautorizador
export DB_USERNAME=root
export DB_PASSWORD="<senha-configurada-no-MySQL>"
```

No PowerShell, configure-as para a sessão atual assim:

```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "3306"
$env:DB_NAME = "miniautorizador"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "<senha-configurada-no-MySQL>"
```

Inicie a aplicação:

```bash
mvn spring-boot:run
```

A API fica disponível em `http://localhost:8080`. Para gerar o JAR:

```bash
mvn clean package
java -jar target/mini-autorizador-1.0.0.jar
```

O Hibernate está configurado com `ddl-auto=update` e atualiza o esquema ao iniciar. O perfil de testes usa H2 em memória e não requer uma instância MySQL.

## Endpoints

### Cadastrar cartão

`POST /cartoes`

O número deve conter 16 dígitos e a senha do cartão, 4 dígitos. Um cartão novo recebe saldo inicial de `1100.00`.

```bash
curl -i -X POST http://localhost:8080/cartoes \
  -H "Content-Type: application/json" \
  -d '{"numeroCartao":"6549873025634501","senha":"1234"}'
```

Sucesso (`201 Created`):

```json
{
  "senha": "1234",
  "numeroCartao": "6549873025634501"
}
```

Se o cartão já existir, a resposta é `422 Unprocessable Entity` com o mesmo formato de corpo.

### Consultar saldo

`GET /cartoes/{numeroCartao}`

Este endpoint exige autenticação HTTP Basic, configurada pelas variáveis `APP_SECURITY_USERNAME` e `APP_SECURITY_PASSWORD`.

```bash
curl -i -u "<usuario>:<senha-configurados-no-ambiente>" \
  http://localhost:8080/cartoes/6549873025634501
```

- `200 OK`: saldo como número JSON, por exemplo `495.15`.
- `404 Not Found`: cartão inexistente, sem corpo.
- `401 Unauthorized`: credenciais ausentes ou inválidas.

### Autorizar transação

`POST /transacoes`

```bash
curl -i -X POST http://localhost:8080/transacoes \
  -H "Content-Type: application/json" \
  -d '{"numeroCartao":"6549873025634501","senhaCartao":"1234","valor":4.85}'
```

- `201 Created`, corpo `OK`: transação aprovada.
- `422 Unprocessable Entity`, corpo texto `CARTAO_INEXISTENTE`, `SENHA_INVALIDA` ou `SALDO_INSUFICIENTE`: transação recusada.
- `400 Bad Request`: corpo inválido ou campos fora das validações.
- `503 Service Unavailable`, corpo texto `TRANSACAO_TEMPORARIAMENTE_INDISPONIVEL`: lock pessimista indisponível.

## Concorrência e persistência

A autorização é processada dentro de uma transação (`@Transactional`). O repositório busca o cartão com `PESSIMISTIC_WRITE`, que Hibernate traduz para o lock de escrita apropriado ao banco (como `FOR UPDATE` no MySQL). A senha e o saldo são validados após a aquisição do lock; o débito e a gravação do histórico da transação pertencem à mesma transação. O lock é mantido até commit ou rollback, serializando autorizações concorrentes para o mesmo cartão e evitando double spending.

## Segurança

- A consulta de saldo usa HTTP Basic. Configure `APP_SECURITY_USERNAME` e `APP_SECURITY_PASSWORD` no ambiente; não use credenciais de desenvolvimento em produção.
- A criação de cartões e o endpoint de transações são públicos na configuração atual.
- Não registre senhas nem números completos de cartão. O `toString()` do comando de transação oculta a senha e mascara o número do cartão.
- O endpoint de criação devolve os dados enviados, incluindo a senha do cartão, conforme o contrato da API. Proteja esse endpoint com controles adequados antes de expô-lo em ambientes não confiáveis.

## Observabilidade

O filtro de requisições aceita um `X-Correlation-ID` com até 64 caracteres alfanuméricos ou `._-`; se ausente ou inválido, gera um UUID. O identificador é devolvido no mesmo header, incluído nos logs via MDC e removido do contexto ao final da requisição.

Os endpoints do Spring Boot Actuator expostos são:

- `GET /actuator/health`: estado de saúde, sem detalhes internos.
- `GET /actuator/info`: nome e descrição da aplicação.

Os logs são enviados ao console. O nível raiz pode ser alterado pela variável `LOG_LEVEL_ROOT` (padrão `INFO`).

## Testes e cobertura

Execute os testes:

```bash
mvn test
```

Gere o relatório JaCoCo:

```bash
mvn clean verify
```

O relatório HTML é gerado em `target/site/jacoco/index.html`.

## Estrutura do projeto

- `domain`: modelos, regras e exceções de negócio.
- `application`: casos de uso, comandos, resultados e portas.
- `infrastructure/adapter/inbound`: endpoints REST, validação, tratamento de erros e filtro de correlação.
- `infrastructure/adapter/outbound`: persistência JPA para cartões e transações.
- `infrastructure/config`: configuração de segurança.
