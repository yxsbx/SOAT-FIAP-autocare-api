# RFC-003 - Autenticacao por CPF com JWT

## Status

Aprovada para a entrega da Fase 3.

## Contexto

O requisito pede rotas protegidas por autenticacao via CPF e uma Function Serverless que valide CPF, consulte existencia/status do cliente e gere um JWT valido para as APIs protegidas.

## Decisao Proposta

Criar uma Lambda dedicada para `POST /auth/cpf`, exposta pelo API Gateway. A Lambda valida CPF, consulta `customers` no RDS e emite JWT assinado com o mesmo segredo aceito pela API Spring Boot.

## Payload

```json
{
  "cpf": "12345678909"
}
```

## Token Emitido

Claims principais:

- `sub`: identificador do cliente.
- `customerId`: UUID do cliente.
- `document`: CPF normalizado.
- `role`: `CUSTOMER`.
- `scope`: `customer:self`.
- `iss`: valor de `JWT_ISSUER`.
- `exp`: expiracao configuravel.

## Justificativa

- Atende diretamente ao requisito serverless sem transformar toda a identidade em um IdP externo.
- Mantem a API protegida por Bearer Token, igual ao fluxo interno ja existente.
- Evita expor senha para cliente final no fluxo academico.
- Centraliza validacao CPF/status do cliente antes de consumir APIs protegidas.

## Keycloak

Keycloak poderia ser usado para login corporativo, OAuth2/OIDC e gestao completa de usuarios. Para este requisito, ele adicionaria complexidade e nao substituiria a Lambda exigida, porque a entrega pede explicitamente uma Function Serverless que consulte CPF e emita token. Portanto, fica como evolucao futura, nao como componente necessario da Fase 3.

## Riscos e Mitigacoes

| Risco | Mitigacao |
|---|---|
| CPF isolado e fator fraco de autenticacao | Registrar como limite academico e evoluir para OTP/SMS/e-mail ou senha em producao real. |
| Divergencia entre token Lambda e API | Usar mesmo `JWT_SECRET`, claims documentadas e teste manual via API Gateway. |
| Exposicao de segredo | Usar Secrets Manager/GitHub Secrets, nunca commit no repositorio. |
