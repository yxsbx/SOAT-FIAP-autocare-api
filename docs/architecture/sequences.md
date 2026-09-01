# Sequencias - AutoCare Hub Fase 3

## Autenticacao por CPF

```mermaid
sequenceDiagram
    autonumber
    actor Cliente
    participant Web as Web Vue
    participant Gateway as API Gateway
    participant Lambda as Auth Lambda
    participant Secrets as Secrets Manager
    participant DB as RDS PostgreSQL

    Cliente->>Web: Informa CPF
    Web->>Gateway: POST /auth/cpf { cpf }
    Gateway->>Lambda: Evento HTTP API
    Lambda->>Lambda: Normaliza e valida digitos do CPF
    Lambda->>Secrets: Busca credenciais DB e JWT_SECRET
    Secrets-->>Lambda: Secrets
    Lambda->>DB: SELECT cliente por document_value
    DB-->>Lambda: Cliente, document_type, active
    alt CPF valido e cliente ativo
        Lambda->>Lambda: Gera JWT role=CUSTOMER customerId document
        Lambda-->>Gateway: 200 { accessToken, tokenType, expiresIn }
        Gateway-->>Web: 200 JWT
        Web->>Web: Salva autocare.token
    else CPF invalido, inexistente ou inativo
        Lambda-->>Gateway: 401/403/404
        Gateway-->>Web: Erro padronizado
    end
```

## Abertura de Ordem de Servico

```mermaid
sequenceDiagram
    autonumber
    actor Usuario as Atendente/Admin
    participant Web as Web Vue
    participant Gateway as API Gateway
    participant API as API Spring Boot EKS
    participant Auth as JwtAuthenticationFilter
    participant UC as CreateServiceOrderUseCase
    participant Domain as Dominio ServiceOrder/Part
    participant DB as RDS PostgreSQL
    participant NR as New Relic

    Usuario->>Web: Preenche cliente, veiculo, diagnostico, servicos e pecas
    Web->>Gateway: POST /api/v1/service-orders Authorization Bearer JWT
    Gateway->>API: Encaminha request com X-Correlation-Id
    API->>Auth: Valida JWT interno ou JWT da Lambda
    Auth-->>API: Principal autenticado e autorizacao
    API->>UC: Executa command de criacao da OS
    UC->>DB: Busca/cria cliente e veiculo
    UC->>DB: Busca servicos e pecas
    UC->>Domain: Valida regras, status e valores
    Domain-->>UC: OS criada e itens calculados
    UC->>DB: Persiste OS e itens
    API->>NR: Incrementa autocare.service_orders.created
    API-->>Gateway: 201 ServiceOrderResponse
    Gateway-->>Web: 201 OS criada
```

## Correlacao e Logs

Todas as chamadas protegidas carregam `X-Correlation-Id`. Se o header nao chegar na API, o filtro cria um UUID, grava no MDC e devolve o mesmo valor no response. O log JSON emitido para stdout inclui esse `correlationId`, permitindo cruzar eventos do API Gateway, pods e APM no New Relic.
