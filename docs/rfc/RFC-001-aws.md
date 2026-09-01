# RFC-001 - Escolha da AWS como nuvem da Fase 3

## Status

Aprovada para a entrega da Fase 3.

## Contexto

A Fase 3 exige API Gateway, Function Serverless, banco gerenciado, Kubernetes escalavel, Terraform, CI/CD e observabilidade. A solucao precisa ser demonstravel, coerente entre repositorios e simples de operar por uma equipe pequena.

## Decisao Proposta

Usar AWS como nuvem principal da solucao, com:

- Amazon API Gateway para entrada publica e roteamento.
- AWS Lambda para autenticacao por CPF.
- Amazon RDS PostgreSQL para banco gerenciado.
- Amazon EKS para workloads Kubernetes.
- Amazon ECR para imagens Docker.
- AWS Secrets Manager para segredos compartilhados por Lambda/API.
- S3 + DynamoDB para backend remoto do Terraform.

## Alternativas Consideradas

| Alternativa | Pontos positivos | Motivos para nao escolher agora |
|---|---|---|
| Azure | AKS, Functions, API Management e SQL Database/RDS equivalentes. | A stack atual ficou mais direta com exemplos e modulo AWS. |
| GCP | Cloud Run/GKE/Cloud SQL oferecem boa experiencia serverless. | Menor familiaridade no contexto do projeto e menos aderente aos artefatos ja criados. |
| Kubernetes local | Baixo custo e simples para demo. | Nao atende banco gerenciado, API Gateway cloud, Lambda nem alta disponibilidade real. |

## Impactos

- Todos os repositorios usam GitHub Actions com credenciais AWS por environment.
- Terraform fica dividido em `infra-db` e `infra-k8s`.
- A Lambda e a API compartilham o mesmo segredo JWT via Secrets Manager/GitHub Secrets.
- A configuracao real de conta, VPC, subnets e chaves fica manual e sera feita fora do codigo.

## Riscos e Mitigacoes

| Risco | Mitigacao |
|---|---|
| Custo de EKS/RDS em conta real | Usar tamanhos pequenos em homolog, desligar recursos apos demo e revisar plano antes do apply. |
| Configuracao inicial de VPC/subnets | Documentar variaveis obrigatorias nos READMEs e pipelines. |
| Acoplamento ao provider | Manter regras de negocio fora do Terraform e usar padroes Kubernetes portaveis nos manifests. |
