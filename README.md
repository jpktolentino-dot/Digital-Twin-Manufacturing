# JERMAM — Digital Twin Manufacturing

Projeto acadêmico com frontend Next.js e backend Java/Spring Boot, baseado nos documentos de requisitos e diagramas disponibilizados pela equipe. O foco é demonstrar cadastro, acompanhamento operacional, ordens de produção, histórico, eventos, indicadores simples e simulação local — sem sensores físicos ou infraestrutura distribuída.

## Escopo implementado

- Login com token JWT, senha armazenada com BCrypt e autorização básica por perfil.
- CRUD de setores, máquinas, usuários e produtos.
- Uma única máquina associada a cada ordem de produção para manter o modelo simples nesta versão.
- Criação, início, pausa, conclusão e cancelamento de OPs.
- Apontamentos com quantidade produzida, quantidade refugadas e energia consumida.
- Mudanças do estado da máquina registradas no histórico.
- Registro e resolução de eventos de falha, interrupção e manutenção.
- Dashboard com status da planta, produção diária, energia, disponibilidade, performance, qualidade e OEE estimado.
- Simulação opcional que gera produção e eventos fictícios.
- Dados de demonstração são criados automaticamente na primeira inicialização de um banco vazio.

## Tecnologias

- Java 21 e Spring Boot 3.5.x
- Spring Web, Validation, Data JPA e Spring Security
- PostgreSQL
- JWT (JJWT) e BCrypt
- Next.js App Router, React, TypeScript e Tailwind CSS
- Docker Compose opcional

## Estrutura

```text
JERMAM/
├── backend/       API Spring Boot, persistência, autenticação, KPIs e simulação
├── frontend/      interface Next.js integrada à API
├── documentacao/  documentos de requisitos e diagramas enviados pela equipe
├── docker-compose.yml
└── README.md
```

## Opção A — executar com Docker

Requer Docker Desktop ou Docker Engine com o comando `docker compose`.

Na pasta raiz:

```bash
docker compose up --build
```

Endereços:

- Frontend: http://localhost:3000
- API: http://localhost:8080/api
- Verificação da API: http://localhost:8080/api/health

Para parar os serviços, use `Ctrl+C`; para removê-los, execute `docker compose down`. Os dados do PostgreSQL ficam no volume `jermam_postgres_data`. Para apagar também o banco e reiniciar a demonstração do zero, execute `docker compose down -v`.

## Opção B — executar localmente

### 1. Banco de dados

Instale o PostgreSQL e crie um banco/usuário compatíveis com os padrões do `backend/src/main/resources/application.yml`:

- Banco: `jermam`
- Usuário: `jermam`
- Senha: `jermam`
- Porta: `5432`

É possível sobrescrever essas configurações por variáveis de ambiente `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`.

### 2. Backend

Requisitos: JDK 21 e Maven 3.9+.

```bash
cd backend
mvn spring-boot:run
```

A API inicia em `http://localhost:8080`. As tabelas são atualizadas pelo Hibernate durante o desenvolvimento (`ddl-auto: update`). Para um projeto de produção, isso deveria ser substituído por migrações versionadas e uma configuração de banco específica por ambiente.

### 3. Frontend

Requisitos: Node.js 22 e pnpm conforme a versão definida no `package.json`.

```bash
cd frontend
corepack enable
pnpm install
pnpm dev
```

Abra `http://localhost:3000`. Por padrão, o frontend utiliza `http://localhost:8080/api`. Para outro endereço, crie `frontend/.env.local` com:

```dotenv
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

## Contas de demonstração

| Perfil | E-mail | Senha |
|---|---|---|
| Administrador | `admin@jermam.com` | `admin123` |
| Operador | `operador@jermam.com` | `demo123` |
| Manutenção | `manutencao@jermam.com` | `demo123` |
| Gestor | `gestor@jermam.com` | `demo123` |

Altere ou remova as contas de demonstração antes de qualquer implantação pública. O segredo JWT do arquivo de configuração é apenas para desenvolvimento; use um segredo aleatório via `JWT_SECRET` se for expor o backend.

## Endpoints principais

Todos os endpoints abaixo, exceto login e health, exigem `Authorization: Bearer <token>`.

| Recurso | Endpoints |
|---|---|
| Autenticação | `POST /api/auth/login`, `GET /api/auth/me` |
| Health | `GET /api/health` |
| Dashboard | `GET /api/dashboard/resumo` |
| Setores | `GET/POST /api/setores`, `GET/PUT/DELETE /api/setores/{id}` |
| Usuários | `GET/POST /api/usuarios`, `GET/PUT/DELETE /api/usuarios/{id}`, `GET /api/usuarios/operadores` |
| Produtos | `GET/POST /api/produtos`, `GET/PUT/DELETE /api/produtos/{id}` |
| Máquinas | `GET/POST /api/maquinas`, `GET/PUT/DELETE /api/maquinas/{id}`, `PATCH /api/maquinas/{id}/estado`, `GET /api/maquinas/{id}/historico` |
| Ordens | `GET/POST /api/ordens-producao`, `GET/PUT/DELETE /api/ordens-producao/{id}`, ações `/iniciar`, `/pausar`, `/concluir`, `/cancelar` |
| Produção | `POST /api/ordens-producao/{id}/registros`, `GET /api/ordens-producao/{id}/registros`, `GET /api/registros-producao` |
| Eventos | `GET/POST /api/eventos`, `PATCH /api/eventos/{id}/resolver`, `DELETE /api/eventos/{id}` |
| Simulação | `GET /api/simulacao`, `POST /api/simulacao/iniciar`, `POST /api/simulacao/parar`, `PUT /api/simulacao/configuracao` |

O contrato dos DTOs está representado nos records em `backend/src/main/java/com/jermam/dto`. Os erros de validação retornam HTTP 400, os itens inexistentes 404 e conflitos de integridade 409.

## Como os indicadores são aproximados

- Disponibilidade: tempo registrado como `OPERANDO` dividido pelo tempo coberto pelo histórico no dia.
- Performance: quantidade produzida multiplicada pelo ciclo ideal do produto, dividida pelo tempo registrado em operação; limitada a 100% para facilitar a leitura acadêmica.
- Qualidade: quantidade boa dividida pela quantidade total produzida.
- OEE: disponibilidade × performance × qualidade.

Esses indicadores têm objetivo didático. Como a simulação é simplificada, não substituem medição industrial ou análise de produção real. Sem apontamentos, os componentes que dependem de produção ficam em zero.

## Testes

No backend:

```bash
cd backend
mvn test
```

Os testes de integração usam H2 em memória e cobrem o login, a proteção de endpoints e a consulta do resumo do dashboard. O banco padrão para executar a aplicação continua sendo PostgreSQL.

## Limitações conscientes desta versão

- Uma OP referencia apenas uma máquina; não existe roteamento de uma OP por uma sequência de máquinas.
- Simulação de estado/energia/produção é aleatória e serve apenas para demonstração.
- O estado de execução e os parâmetros atuais da simulação ficam em memória; ao reiniciar a API, a simulação volta pausada com parâmetros padrão.
- Autorização por perfil é básica e pode ser refinada com regras de propriedade/escopo por máquina e auditoria de usuário.
- Não há integração física com IoT, notificações push, relatórios PDF nem manutenção preditiva.
