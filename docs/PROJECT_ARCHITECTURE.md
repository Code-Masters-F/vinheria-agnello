# 🍷 Vinheiro — Arquitetura Técnica e Plano de Implementação

> Documento técnico de referência para desenvolvimento — Sprint 1 · 2026
> Stack: Java + JSP (Backend) · Kotlin (Mobile B2C)  AWS (Banco de Dados)

---

## 📋 Índice

1. [Visão Geral da Arquitetura](#1-visão-geral-da-arquitetura)
2. [Stack Tecnológica](#2-stack-tecnológica)
3. [Estrutura de Pastas](#3-estrutura-de-pastas)
4. [Banco de Dados — Modelo Relacional](#4-banco-de-dados--modelo-relacional)
5. [Camadas da Aplicação](#5-camadas-da-aplicação)
6. [Módulos e Responsabilidades](#6-módulos-e-responsabilidades)
7. [API REST — Endpoints](#7-api-rest--endpoints)
8. [Fluxos Principais](#8-fluxos-principais)
9. [Motor de Recomendação por IA](#9-motor-de-recomendação-por-ia)
10. [Multi-tenancy — Isolamento por Vinheria](#10-multi-tenancy--isolamento-por-vinheria)
11. [Plano de Implementação por Sprints](#11-plano-de-implementação-por-sprints)
12. [Decisões Arquiteturais](#12-decisões-arquiteturais)

---

## 1. Visão Geral da Arquitetura

O Vinheiro é uma aplicação **multi-tenant SaaS** com dois contextos de uso distintos:

- **Painel B2B**: usado pela vinheria para gerenciar catálogo, pedidos, campanhas e clientes
- **Loja B2C**: usada pelo consumidor final para navegar, receber recomendações e comprar

### Diagrama de Contexto

```
┌─────────────────────────────────────────────────────────────────┐
│                        VINHEIRO PLATFORM                        │
│                                                                 │
│  ┌──────────────────────┐      ┌───────────────────────────┐   │
│  │   PAINEL B2B (JSP)   │      │     LOJA B2C (Kotlin)  │   │
│  │  /admin/**           │      │  /{slug}/                 │   │
│  │  Gestão da vinheria  │      │  Loja customizada         │   │
│  └──────────┬───────────┘      └──────────────┬────────────┘   │
│             │                                 │                │
│  ┌──────────▼─────────────────────────────────▼────────────┐   │
│  │              SERVLET CONTROLLER LAYER (Java)            │   │
│  │         Roteamento · Autenticação · Autorização         │   │
│  └──────────────────────────┬────────────────────────────  ┘   │
│                             │                                   │
│  ┌──────────────────────────▼────────────────────────────  ┐   │
│  │                  SERVICE LAYER (Java)                    │   │
│  │    Regras de negócio · Recomendação IA · Pagamentos     │   │
│  └──────────────────────────┬────────────────────────────  ┘   │
│                             │                                   │
│  ┌──────────────────────────▼────────────────────────────  ┐   │
│  │                  DAO / REPOSITORY LAYER                  │   │
│  │              JDBC · Connection Pool · SQL                │   │
│  └──────────────────────────┬────────────────────────────  ┘   │
│                             │                                   │
│              ┌──────────────▼────────────────┐                 │
│              │     MySQL / PostgreSQL         │                 │
│              └───────────────────────────────┘                 │
└─────────────────────────────────────────────────────────────────┘
```

### Padrão Arquitetural

A aplicação segue o padrão **MVC clássico com camada de serviço**:

```
Request → Filter (Auth/Tenant) → Servlet (Controller) → Service → DAO → DB
                                       ↓
                            JSP (View B2B) ou JSON (API para Kotlin B2C)
```

O **Painel B2B** é renderizado server-side via JSP. O app da **Loja B2C** é nativo em **Kotlin (Android)** e consome a API REST exposta pelos Servlets Java via chamadas HTTP (Retrofit/Ktor).

---

## 2. Stack Tecnológica

### Backend

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 17 LTS | Linguagem principal |
| Jakarta EE (Servlet API) | 5.0 | Roteamento HTTP, Filtros |
| JSP + JSTL | 3.0 | Views do painel B2B |
| Apache Tomcat | 10.1 | Servidor de aplicação |
| HikariCP | 5.x | Connection Pool JDBC |
| AWS RDS (SQL) |  | Banco SQL (Oracle/MySQL) p/ transacional |
| AWS NoSQL |  | MongoDB/DynamoDB p/ catálogo e avaliações |
| Gson / Jackson | 2.x | Serialização JSON (API REST) |
| BCrypt (jBCrypt) | 0.4 | Hash de senhas |
| ZXing | 3.5 | Geração de QR Code |
| JavaMail / SMTP | 2.0 | Envio de e-mails |
| Maven | 3.9 | Build e dependências |

### Mobile (App B2C) & Admin View

| Tecnologia | Versão | Uso |
|---|---|---|
| Kotlin | 1.9+ | Linguagem principal do App Android |
| Retrofit / Ktor |  | Cliente HTTP no Kotlin |
| HTML5 / CSS3 / JS|  | Templates JSP (Admin) |

### Integrações Externas (futuro/planejadas)

| Serviço | Finalidade |
|---|---|
| Pagar.me / Stripe | Processamento de pagamentos |
| Twilio / Z-API | Envio de mensagens WhatsApp |
| Claude API (Anthropic) | Motor de recomendação por IA |
| AWS S3 / Cloudflare R2 | Armazenamento de imagens de produtos |

---

## 3. Estrutura de Pastas

```
vinheiro/
├── pom.xml
├── README.md
│
├── src/
│   └── main/
│       ├── java/
│       │   └── br/com/vinheiro/
│       │       │
│       │       ├── config/
│       │       │   ├── DatabaseConfig.java          # HikariCP pool setup
│       │       │   └── AppConfig.java               # Constantes e propriedades
│       │       │
│       │       ├── filter/
│       │       │   ├── AuthFilter.java              # Verifica sessão autenticada
│       │       │   ├── TenantFilter.java            # Resolve vinheria pelo subdomínio/slug
│       │       │   └── CorsFilter.java              # Habilita CORS para API Kotlin
│       │       │
│       │       ├── model/
│       │       │   ├── enums/                       # Enumerações do domínio
│       │       │   │   ├── TipoVinho.java
│       │       │   │   ├── Ocasiao.java
│       │       │   │   ├── StatusPedido.java
│       │       │   │   ├── TipoEntrega.java
│       │       │   │   ├── StatusPagamento.java
│       │       │   │   ├── MetodoPagamento.java
│       │       │   │   ├── StatusCampanha.java
│       │       │   │   ├── CanalCampanha.java
│       │       │   │   └── TipoCadastro.java
│       │       │   ├── Vinheria.java
│       │       │   ├── UsuarioAdmin.java             # Usuário do painel B2B
│       │       │   ├── Vinho.java
│       │       │   ├── VinhoOcasiao.java            # Relação M:N vinho-ocasião
│       │       │   ├── Cliente.java
│       │       │   ├── Pedido.java
│       │       │   ├── ItemPedido.java
│       │       │   ├── Pagamento.java
│       │       │   ├── AvaliacaoVinho.java
│       │       │   ├── Campanha.java
│       │       │   ├── ConfigFidelidade.java        # Config do programa de fidelidade
│       │       │   ├── HistoricoPontos.java         # Histórico de pontos
│       │       │   ├── ScanQRCode.java              # Análise de QR Codes
│       │       │   └── Recomendacao.java
│       │       │
│       │       ├── dao/
│       │       │   ├── VinheriaDAO.java
│       │       │   ├── UsuarioAdminDAO.java
│       │       │   ├── VinhoDAO.java
│       │       │   ├── ClienteDAO.java
│       │       │   ├── PedidoDAO.java
│       │       │   ├── ItemPedidoDAO.java
│       │       │   ├── PagamentoDAO.java
│       │       │   ├── AvaliacaoVinhoDAO.java
│       │       │   ├── CampanhaDAO.java
│       │       │   ├── ConfigFidelidadeDAO.java
│       │       │   ├── HistoricoPontosDAO.java
│       │       │   └── ScanQRCodeDAO.java
│       │       │
│       │       ├── service/
│       │       │   ├── VinheriaService.java
│       │       │   ├── UsuarioAdminService.java
│       │       │   ├── VinhoService.java
│       │       │   ├── ClienteService.java
│       │       │   ├── PedidoService.java
│       │       │   ├── RecomendacaoService.java     # Motor de IA
│       │       │   ├── FidelidadeService.java
│       │       │   ├── CampanhaService.java
│       │       │   ├── QrCodeService.java
│       │       │   └── PagamentoService.java
│       │       │
│       │       ├── servlet/
│       │       │   │
│       │       │   ├── admin/                       # Painel B2B — renderiza JSPs
│       │       │   │   ├── AuthServlet.java         # Login/logout admin B2B
│       │       │   │   ├── DashboardServlet.java
│       │       │   │   ├── CatalogoServlet.java
│       │       │   │   ├── PedidosServlet.java
│       │       │   │   ├── ClientesServlet.java
│       │       │   │   ├── CampanhasServlet.java
│       │       │   │   ├── FidelidadeServlet.java
│       │       │   │   ├── RelatoriosServlet.java
│       │       │   │   └── ConfiguracaoServlet.java
│       │       │   │
│       │       │   └── api/                         # API REST — retorna JSON para Kotlin
│       │       │       ├── TenantApiServlet.java
│       │       │       ├── VinhosApiServlet.java
│       │       │       ├── RecomendacaoApiServlet.java
│       │       │       ├── PedidoApiServlet.java
│       │       │       ├── ClienteApiServlet.java
│       │       │       ├── AuthApiServlet.java
│       │       │       ├── FidelidadeApiServlet.java
│       │       │       └── PagamentoApiServlet.java
│       │       │
│       │       └── util/
│       │           ├── JsonUtil.java                # Helpers para serialização
│       │           ├── PasswordUtil.java            # BCrypt wrapper
│       │           ├── SessionUtil.java
│       │           └── SlugUtil.java                # Geração de slug para URL
│       │
│       └── webapp/
│           ├── WEB-INF/
│           │   ├── web.xml                          # Mapeamento de Servlets e Filtros
│       │   └── views/
│       │       ├── error/
│       │       │   ├── 404.jsp                    # Página não encontrada
│       │       │   └── 500.jsp                    # Erro interno
│       │       ├── admin/                        # JSPs do painel B2B
│       │       │   ├── layout/
│       │       │   │   ├── header.jsp
│       │       │   │   ├── sidebar.jsp
│       │       │   │   └── footer.jsp
│       │       │   ├── dashboard.jsp
│       │       │   ├── catalogo.jsp
│       │       │   ├── pedidos.jsp
│       │       │   ├── clientes.jsp
│       │       │   ├── campanhas.jsp
│       │       │   ├── fidelidade.jsp
│       │       │   ├── relatorios.jsp
│       │       │   └── configuracao.jsp
│       │       └── auth/
│       │           ├── login.jsp
│       │           └── registro.jsp
│           │
│           ├── static/
│           │   ├── css/
│           │   │   └── admin.css                    # Estilos do painel JSP
│           │   ├── js/
│           │   │   └── admin.js                     # Scripts auxiliares do painel
│           │   └── img/
│           │
│           └── loja/                                # Build do Kotlin (gerado pelo Vite)
│               ├── index.html
│               ├── assets/
│               └── ...
│
└── app-mobile/                                      # Código-fonte Kotlin (Mobile App)
     build.gradle.kts
     src/
         main/
             AndroidManifest.xml
             java/br/com/vinheiro/
                 view/             # Telas nativas
                 api/              # Retrofit endpoints
                 model/            # Data classes
```

---

## 4. Banco de Dados — Modelo Relacional

### Tabelas Principais

```sql
-- Tenant central
CREATE TABLE vinheria (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome            VARCHAR(100) NOT NULL,
    slug            VARCHAR(60) UNIQUE NOT NULL,   -- ex: "alcantara-vinhos"
    logo_url        VARCHAR(255),
    cor_primaria    VARCHAR(7),                    -- hex: #8B1A1A
    cor_secundaria  VARCHAR(7),
    ativo           BOOLEAN DEFAULT TRUE,
    criado_em       DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Usuário da vinheria (acesso ao painel B2B)
CREATE TABLE usuario_admin (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    vinheria_id     BIGINT NOT NULL REFERENCES vinheria(id),
    nome            VARCHAR(100) NOT NULL,
    email           VARCHAR(150) UNIQUE NOT NULL,
    senha_hash      VARCHAR(255) NOT NULL,
    criado_em       DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Catálogo de vinhos por vinheria
CREATE TABLE vinho (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    vinheria_id     BIGINT NOT NULL REFERENCES vinheria(id),
    nome            VARCHAR(150) NOT NULL,
    tipo            ENUM('tinto','branco','rose','espumante','sobremesa') NOT NULL,
    uva             VARCHAR(100),
    pais            VARCHAR(80),
    regiao          VARCHAR(100),
    safra           YEAR,
    preco           DECIMAL(10,2) NOT NULL,
    descricao       TEXT,
    foto_url        VARCHAR(255),
    estoque         INT DEFAULT 0,
    estoque_minimo  INT DEFAULT 3,
    ativo           BOOLEAN DEFAULT TRUE,
    criado_em       DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Ocasiões e harmonizações (tags M:N)
CREATE TABLE vinho_ocasiao (
    vinho_id   BIGINT REFERENCES vinho(id),
    ocasiao    ENUM('churrasco','jantar_casa','presente','petisco','comemoracao','casual'),
    PRIMARY KEY (vinho_id, ocasiao)
);

-- Cliente consumidor (B2C)
CREATE TABLE cliente (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    vinheria_id     BIGINT NOT NULL REFERENCES vinheria(id),
    nome            VARCHAR(100) NOT NULL,
    email           VARCHAR(150),
    whatsapp        VARCHAR(20),
    cpf             VARCHAR(14),
    endereco        TEXT,
    senha_hash      VARCHAR(255),                   -- null = cadastro presencial
    tipo_cadastro   ENUM('online','presencial') NOT NULL,
    pontos          INT DEFAULT 0,
    criado_em       DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (vinheria_id, email),
    UNIQUE (vinheria_id, whatsapp)
);

-- Pedidos
CREATE TABLE pedido (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    vinheria_id     BIGINT NOT NULL REFERENCES vinheria(id),
    cliente_id      BIGINT REFERENCES cliente(id),   -- null = pedido anônimo
    status          ENUM('aguardando_pagamento','pago','em_separacao',
                         'pronto','entregue','cancelado') DEFAULT 'aguardando_pagamento',
    tipo_entrega    ENUM('retirada','delivery') NOT NULL,
    subtotal        DECIMAL(10,2) NOT NULL,
    total           DECIMAL(10,2) NOT NULL,
    endereco_entrega TEXT,
    criado_em       DATETIME DEFAULT CURRENT_TIMESTAMP,
    atualizado_em   DATETIME ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE item_pedido (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    pedido_id   BIGINT NOT NULL REFERENCES pedido(id),
    vinho_id    BIGINT NOT NULL REFERENCES vinho(id),
    quantidade  INT NOT NULL,
    preco_unit  DECIMAL(10,2) NOT NULL
);

-- Pagamentos
CREATE TABLE pagamento (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    pedido_id       BIGINT NOT NULL REFERENCES pedido(id),
    metodo          ENUM('pix','cartao_credito','cartao_debito'),
    status          ENUM('pendente','aprovado','recusado','estornado'),
    valor           DECIMAL(10,2) NOT NULL,
    gateway_id      VARCHAR(100),                    -- ID externo (Pagar.me etc.)
    criado_em       DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Avaliações de vinho pelo cliente
CREATE TABLE avaliacao_vinho (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    cliente_id  BIGINT NOT NULL REFERENCES cliente(id),
    vinho_id    BIGINT NOT NULL REFERENCES vinho(id),
    nota        TINYINT CHECK (nota BETWEEN 1 AND 5),
    ocasiao     VARCHAR(60),
    criado_em   DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (cliente_id, vinho_id)
);

-- Campanhas segmentadas
CREATE TABLE campanha (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    vinheria_id     BIGINT NOT NULL REFERENCES vinheria(id),
    nome            VARCHAR(150) NOT NULL,
    mensagem        TEXT NOT NULL,
    filtro_tipo     VARCHAR(60),    -- ex: "comprou_tinto_60d", "inativo_30d"
    canal           ENUM('whatsapp','email','ambos'),
    status          ENUM('rascunho','enviada','agendada'),
    enviada_em      DATETIME,
    criado_em       DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Programa de fidelidade — configuração por vinheria
CREATE TABLE config_fidelidade (
    vinheria_id         BIGINT PRIMARY KEY REFERENCES vinheria(id),
    pontos_por_real     DECIMAL(5,2) DEFAULT 1.0,
    validade_dias       INT DEFAULT 365,
    recompensas         JSON                         -- array de {pontos, descricao}
);

-- Histórico de pontos
CREATE TABLE historico_pontos (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    cliente_id  BIGINT NOT NULL REFERENCES cliente(id),
    pedido_id   BIGINT REFERENCES pedido(id),
    pontos      INT NOT NULL,                        -- positivo = ganho, negativo = resgate
    descricao   VARCHAR(200),
    criado_em   DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Análise de QR Code (dados anônimos)
CREATE TABLE scan_qrcode (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    vinheria_id     BIGINT NOT NULL REFERENCES vinheria(id),
    ocasiao         VARCHAR(60),
    faixa_preco     VARCHAR(30),
    converteu       BOOLEAN DEFAULT FALSE,
    criado_em       DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

---

## 5. Camadas da Aplicação

### 5.1 Filter Layer

**`TenantFilter`** — executado em toda requisição `/loja/*` e `/api/*`

```
Requisição chega → extrai slug da URL ou subdomínio
→ busca Vinheria no DB → injeta como atributo na request
→ passa para o próximo filtro/servlet
```

**`AuthFilter`** — executado em `/admin/*`

```
Verifica HttpSession → se não autenticado → redirect para /login
→ verifica se admin pertence à vinheria do tenant → autoriza ou bloqueia
```

**`CorsFilter`** — executado em `/api/*`

```
Adiciona headers CORS para permitir chamadas do Kotlin (Vite dev server)
```

### 5.2 Controller Layer (Servlets)

Dois grupos de servlets com responsabilidades distintas:

**Servlets Admin (JSP)** — processam form POST e redirecionam (`PRG pattern`)

```java
// Exemplo: CatalogoServlet
@WebServlet("/admin/catalogo/*")
public class CatalogoServlet extends HttpServlet {
    protected void doGet(...)  // lista vinhos → forward para catalogo.jsp
    protected void doPost(...) // salva/edita/exclui → redirect com mensagem
}
```

**Servlets API (JSON)** — leem/escrevem JSON para o Kotlin

```java
// Exemplo: VinhosApiServlet
@WebServlet("/api/vinhos/*")
public class VinhosApiServlet extends HttpServlet {
    protected void doGet(...)  // retorna JSON com lista filtrada
    protected void doPost(...) // recebe JSON, valida, persiste
}
```

### 5.3 Service Layer

Concentra toda a lógica de negócio. Nenhum Servlet acessa o DAO diretamente.

```java
public class VinhoService {
    private final VinhoDAO dao;

    public List<Vinho> listarDisponiveisPorVinheria(Long vinheriaId) { ... }
    public void cadastrar(Vinho vinho) throws VinhoJaExisteException { ... }
    public void atualizarEstoque(Long vinhoId, int quantidade) throws EstoqueInsuficienteException { ... }
}
```

### 5.4 DAO Layer

Acesso direto ao banco via JDBC. Cada DAO recebe uma `Connection` do HikariCP.

```java
public class VinhoDAO {
    public List<Vinho> findByVinheriaId(Long vinheriaId, Connection conn) { ... }
    public Optional<Vinho> findById(Long id, Connection conn) { ... }
    public void save(Vinho vinho, Connection conn) { ... }
    public void updateEstoque(Long vinhoId, int novoEstoque, Connection conn) { ... }
}
```

---

## 6. Módulos e Responsabilidades

### Módulo 1 — Autenticação e Multi-tenancy

**Responsabilidade**: login do admin B2B, resolução do tenant por slug, sessão do cliente B2C.

Componentes:
- `AuthFilter.java` + `TenantFilter.java`
- `AuthApiServlet.java` → endpoints `/api/auth/login`, `/api/auth/logout`, `/api/auth/verify-otp`
- `login.jsp`, `registro.jsp`
- `SessionUtil.java`, `PasswordUtil.java`

Fluxo do OTP (cadastro presencial):
```
Cliente digita WhatsApp → sistema gera código 6 dígitos →
envia via WhatsApp API → cliente insere código →
sistema valida → cria sessão
```

### Módulo 2 — Catálogo e Estoque

**Responsabilidade**: CRUD de vinhos, controle de estoque, upload de fotos.

Componentes:
- `CatalogoServlet.java` (admin JSP) + `VinhosApiServlet.java` (API Kotlin)
- `VinhoService.java`, `VinhoDAO.java`
- `catalogo.jsp`

Regra de negócio: ao atingir `estoque = 0`, o vinho é automaticamente marcado como indisponível. Ao atingir `estoque <= estoque_minimo`, o admin recebe alerta no dashboard.

### Módulo 3 — Loja Digital (Kotlin SPA)

**Responsabilidade**: experiência do consumidor final.

Componentes Kotlin:
- `VinheriaContext` → carrega dados do tenant (logo, cores, nome) via `/api/tenant`
- `EntradaLoja.jsx` → split screen "Já sei" vs "Me ajude"
- `Catalogo.jsx` → listagem com filtros em tempo real via `/api/vinhos`
- `DetalheVinho.jsx` → página do produto
- `Recomendacao.jsx` → questionário 3 etapas + resultado IA
- `Checkout.jsx` → carrinho + pagamento

### Módulo 4 — Pedidos

**Responsabilidade**: criação, acompanhamento e gestão de pedidos.

Componentes:
- `PedidoApiServlet.java` + `PedidosServlet.java` (admin)
- `PedidoService.java`, `PedidoDAO.java`
- `pedidos.jsp`

Fluxo de criação:
```
Carrinho no Kotlin → POST /api/pedidos → valida estoque →
cria Pedido + ItensPedido → inicia pagamento → decrementar estoque
→ notifica admin → retorna ID do pedido
```

### Módulo 5 — Motor de Recomendação por IA

Detalhado na Seção 9.

### Módulo 6 — Programa de Fidelidade

**Responsabilidade**: acúmulo e resgate de pontos.

Componentes:
- `FidelidadeService.java` → calcula pontos, aplica validade, registra resgate
- `FidelidadeApiServlet.java` → `/api/fidelidade/saldo`, `/api/fidelidade/resgatar`
- `FidelidadeServlet.java` (admin) + `fidelidade.jsp`

### Módulo 7 — Campanhas Segmentadas

**Responsabilidade**: criação e disparo de mensagens segmentadas.

Componentes:
- `CampanhaService.java` → resolve filtro, monta lista de destinatários, dispara
- `CampanhasServlet.java` + `campanhas.jsp`

Filtros suportados no MVP:
- `comprou_tinto_60d` — comprou vinho tinto nos últimos 60 dias
- `inativo_30d` — não compra há mais de 30 dias
- `nunca_espumante` — nunca comprou espumante
- `ticket_alto` — ticket médio acima de R$ 100

### Módulo 8 — QR Code e Relatórios

**Responsabilidade**: gerar QR Code por vinheria, coletar dados anônimos de escaneamento.

Componentes:
- `QrCodeService.java` → usa ZXing para gerar imagem PNG
- `RelatoriosServlet.java` + `relatorios.jsp`

---

## 7. API REST — Endpoints

Todos os endpoints retornam `Content-Type: application/json`. Autenticação via cookie de sessão (B2C) ou header `X-Admin-Token` (B2B).

### Tenant

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/tenant` | Dados públicos da vinheria (logo, cores, nome) |

### Vinhos

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/vinhos` | Lista vinhos disponíveis (filtros: tipo, preço, ocasião) |
| GET | `/api/vinhos/{id}` | Detalhe de um vinho |
| POST | `/api/vinhos` | Cadastra vinho (admin) |
| PUT | `/api/vinhos/{id}` | Atualiza vinho (admin) |
| DELETE | `/api/vinhos/{id}` | Desativa vinho (admin) |

### Recomendação

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/recomendacao` | Recebe respostas do questionário, retorna 3 sugestões |

```json
// Request
{
  "ocasiao": "churrasco",
  "estilo": "tinto",
  "faixa_preco": "50-100",
  "cliente_id": 42          // opcional
}

// Response
{
  "sugestoes": [
    { "id": 12, "nome": "Malbec Reserva", "preco": 79.90, "descricao": "...", "foto_url": "..." },
    { "id": 7,  "nome": "Carménère Gran", "preco": 89.00, "descricao": "...", "foto_url": "..." },
    { "id": 23, "nome": "Cabernet Chileno","preco": 59.90, "descricao": "...", "foto_url": "..." }
  ]
}
```

### Pedidos

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/pedidos` | Cria novo pedido |
| GET | `/api/pedidos/{id}` | Status do pedido |
| GET | `/api/pedidos/meus` | Histórico do cliente autenticado |

### Autenticação (B2C)

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/auth/login` | Login com e-mail + senha |
| POST | `/api/auth/otp/solicitar` | Solicita OTP por WhatsApp |
| POST | `/api/auth/otp/verificar` | Valida código OTP |
| POST | `/api/auth/logout` | Encerra sessão |

### Fidelidade

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/fidelidade/saldo` | Saldo de pontos do cliente |
| POST | `/api/fidelidade/resgatar` | Resgata recompensa |

---

## 8. Fluxos Principais

### Fluxo 1 — Recomendação Anônima (QR Code)

```
1. Cliente escaneia QR Code na mesa
2. Abre /{slug}/  no navegador (sem app)
3. VinheriaContext carrega logo/cores via GET /api/tenant
4. Tela inicial: "Já sei o que quero" | "Me ajude a escolher"
5. Cliente escolhe "Me ajude" → abre Recomendacao.jsx
6. 3 cards visuais: Ocasião → Estilo → Faixa de preço
7. POST /api/recomendacao com respostas
8. RecomendacaoService busca vinhos em estoque + filtra por critérios
9. Retorna 3 sugestões → exibidas como cards
10. Cliente clica em "Adicionar ao carrinho" → segue para Checkout
```

### Fluxo 2 — Recomendação com Histórico (Cliente Cadastrado)

```
1-6. Igual ao Fluxo 1
7. Antes do POST, verifica sessão ativa
   → Se não logado: exibe "Entrar para sugestões personalizadas" (opcional)
   → Se logado: inclui cliente_id no body
8. RecomendacaoService cruza respostas + histórico de compras + avaliações
9. Ranqueia vinhos por afinidade histórica
10. Retorna 3 sugestões personalizadas
```

### Fluxo 3 — Criação de Pedido com Pagamento

```
1. Cliente adiciona vinhos ao carrinho (CarrinhoContext)
2. Clica em "Finalizar pedido"
3. Checkout.jsx exibe resumo + form de entrega + método de pagamento
4. POST /api/pedidos → PedidoService valida estoque em transação DB
5. Pedido criado com status "aguardando_pagamento"
6. Integração com gateway: gera Pix ou tokeniza cartão
7. Webhook do gateway → POST /api/pagamentos/callback
8. PedidoService atualiza status → "pago" → decrementa estoque
9. Notificação push para painel do admin (WebSocket ou polling)
10. Se cliente cadastrado: adiciona pontos de fidelidade
```

### Fluxo 4 — Admin Cria Campanha Segmentada

```
1. Admin acessa /admin/campanhas
2. Define filtro (ex: "comprou tinto nos últimos 60 dias")
3. Sistema exibe preview: "42 clientes encontrados"
4. Admin escreve mensagem e escolhe canal (WhatsApp / e-mail)
5. Clica em "Enviar" ou "Agendar"
6. CampanhaService busca clientes pelo filtro via CampanhaDAO
7. Para cada cliente: dispara via API do WhatsApp ou SMTP
8. Registra status de envio por destinatário
```

---

## 9. Motor de Recomendação por IA

### Estratégia em duas camadas

O motor usa uma abordagem híbrida para MVP — sem custo de IA para usuários anônimos e com IA contextual para cadastrados:

**Camada 1 — Filtragem baseada em regras (todos os usuários)**

```java
public class RecomendacaoService {

    public List<Vinho> recomendar(RecomendacaoRequest req, Long vinheriaId) {
        // 1. Busca vinhos em estoque filtrados por ocasião e faixa de preço
        List<Vinho> candidatos = vinhoDAO.findByOcasiaoEPreco(
                vinheriaId, req.getOcasiao(), req.getPrecoMin(), req.getPrecoMax()
        );

        // 2. Aplica filtro de estilo (tinto, branco, etc.)
        candidatos = candidatos.stream()
                .filter(v -> v.getTipo().equals(req.getEstilo()))
                .collect(toList());

        // 3. Se cliente autenticado: enriquece com IA
        if (req.getClienteId() != null) {
            return recomendarComHistorico(candidatos, req.getClienteId());
        }

        // 4. Anônimo: retorna top 3 por popularidade da vinheria
        return candidatos.stream()
                .sorted(Comparator.comparingInt(Vinho::getVendasTotal).reversed())
                .limit(3)
                .collect(toList());
    }
}
```

**Camada 2 — Ranking com IA (usuários cadastrados)**

Chama a API da Claude com o histórico compactado do cliente:

```java
private List<Vinho> recomendarComHistorico(List<Vinho> candidatos, Long clienteId) {
    List<AvaliacaoVinho> historico = avaliacaoDAO.findByCliente(clienteId);
    String prompt = construirPrompt(candidatos, historico);

    // Chama Claude API → retorna IDs ranqueados
    String resposta = claudeApiClient.completar(prompt);
    List<Long> idsPriorizados = parseIds(resposta);

    return idsPriorizados.stream()
            .map(id -> candidatos.stream().filter(v -> v.getId().equals(id)).findFirst())
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toList());
}
```

**Prompt template:**

```
Você é um sommelier digital. Com base no histórico de compras abaixo,
ranqueie os vinhos candidatos do mais ao menos adequado.
Responda APENAS com um array JSON de IDs na ordem recomendada.

Histórico do cliente:
{historico_compras_json}

Ocasião atual: {ocasiao}
Candidatos disponíveis:
{candidatos_json}
```

### Aprendizado contínuo

A cada pedido finalizado, `PedidoService` chama `RecomendacaoService.registrarConversao()` que:
1. Cria/atualiza registro em `avaliacao_vinho` com nota implícita (compra = 3/5)
2. Se o cliente avaliar explicitamente → atualiza nota real
3. Dados alimentam o histórico nas próximas recomendações

---

## 10. Multi-tenancy — Isolamento por Vinheria

### Resolução do tenant

```
URL: /alcantara-vinhos/          → TenantFilter extrai slug "alcantara-vinhos"
URL: /api/vinhos (subdomínio)    → TenantFilter lê Host header
```

```java
@WebFilter("/*")
public class TenantFilter implements Filter {
    public void doFilter(ServletRequest req, ...) {
        String slug = extrairSlug((HttpServletRequest) req);
        Vinheria vinheria = vinheriaService.findBySlug(slug);
        req.setAttribute("vinheria", vinheria);      // disponível em toda a requisição
        req.setAttribute("vinheriaId", vinheria.getId());
        chain.doFilter(req, resp);
    }
}
```

### Isolamento de dados

Toda query ao banco inclui `vinheria_id` como parâmetro obrigatório:

```java
// DAO nunca busca dados sem o tenant
public List<Vinho> findByVinheriaId(Long vinheriaId, Connection conn) {
    String sql = "SELECT * FROM vinho WHERE vinheria_id = ? AND ativo = 1";
    // ...
}
```

### Customização visual (CSS injection)

O JSP de entrada da loja injeta as cores do tenant como variáveis CSS:

```jsp
<%-- loja/index.jsp — serve o Kotlin SPA com tokens do tenant ---%>
<style>
  :root {
    --cor-primaria: ${vinheria.corPrimaria};
    --cor-secundaria: ${vinheria.corSecundaria};
  }
</style>
<script>
  window.TENANT = {
    slug: "${vinheria.slug}",
    nome: "${vinheria.nome}",
    logoUrl: "${vinheria.logoUrl}"
  };
</script>
<div id="root"></div>
<script src="/loja/assets/main.js"></script>
```

O Kotlin lê `window.TENANT` via `VinheriaContext` e aplica a identidade visual dinamicamente.

---

## 11. Plano de Implementação por Sprints

### Sprint 0 — Fundação (1 semana)

**Objetivo**: projeto rodando localmente, banco criado, estrutura de pastas definida.

- [ ] Setup Maven + Tomcat + MySQL
- [ ] Criação do schema completo do banco
- [ ] `DatabaseConfig.java` com HikariCP
- [ ] `web.xml` com mapeamentos base
- [ ] `TenantFilter` e `AuthFilter` (esqueleto)
- [ ] Build Kotlin com Vite integrado ao Maven (`frontend-maven-plugin`)
- [ ] JSP de login funcional

**Entregável**: aplicação sobe sem erros. Login redireciona para dashboard vazio.

---

### Sprint 1 — Painel Admin MVP (2 semanas)

**Objetivo**: vinheria consegue cadastrar vinhos e ver pedidos.

- [ ] CRUD completo de vinhos via JSP (`catalogo.jsp`)
- [ ] Upload de foto (salvo em disco, servido como static)
- [ ] Dashboard com contador de pedidos e alertas de estoque
- [ ] Gestão básica de pedidos (`pedidos.jsp`)
- [ ] Módulo de configuração da loja (logo, cores, slug)
- [ ] Geração de QR Code com ZXing

**Entregável**: Fernanda consegue cadastrar seus vinhos e ver a loja no ar.

---

### Sprint 2 — Loja Digital B2C (2 semanas)

**Objetivo**: consumidor acessa a loja pelo QR Code e navega pelo catálogo.

- [ ] `GET /api/tenant` — retorna dados da vinheria
- [ ] `GET /api/vinhos` — lista com filtros
- [ ] Kotlin: `VinheriaContext`, `EntradaLoja`, `Catalogo`, `DetalheVinho`
- [ ] Carrinho de compras (`CarrinhoContext`)
- [ ] CSS dinâmico com cores do tenant
- [ ] Responsividade mobile-first

**Entregável**: Bruno escaneia QR Code, vê os vinhos com a marca da vinheria e adiciona ao carrinho.

---

### Sprint 3 — Motor de Recomendação (1.5 semanas)

**Objetivo**: questionário visual funcional com sugestões.

- [ ] `POST /api/recomendacao` com filtragem por regras
- [ ] Kotlin: `Recomendacao.jsx` com 3 cards visuais animados
- [ ] Tabela `vinho_ocasiao` populada
- [ ] Registro de scans em `scan_qrcode`
- [ ] Integração Claude API (Camada 2 — usuários cadastrados)

**Entregável**: Rafael escaneia QR, responde 3 perguntas e recebe 3 sugestões relevantes.

---

### Sprint 4 — Checkout e Pagamentos (2 semanas)

**Objetivo**: pedido completo, do carrinho ao pagamento.

- [ ] `POST /api/pedidos` com validação de estoque em transação
- [ ] Kotlin: `Checkout.jsx` com form de entrega
- [ ] Integração Pix (Pagar.me sandbox)
- [ ] Webhook de confirmação de pagamento
- [ ] Atualização de status no painel admin
- [ ] Notificação por e-mail ao cliente (JavaMail)

**Entregável**: pedido flui do início ao fim e aparece no painel da vinheria.

---

### Sprint 5 — Cadastro, Histórico e Fidelidade (1.5 semanas)

**Objetivo**: cliente cria conta, acumula pontos, vê histórico.

- [ ] Cadastro online (checkout) e presencial (WhatsApp OTP)
- [ ] Login B2C com sessão
- [ ] `GET /api/pedidos/meus` — histórico
- [ ] Kotlin: `Perfil.jsx` com histórico e pontos
- [ ] `FidelidadeService` — acúmulo automático pós-pedido
- [ ] `fidelidade.jsp` — config no admin

**Entregável**: Bruno cria conta, vê seus pedidos anteriores e saldo de pontos.

---

### Sprint 6 — Campanhas e Relatórios (1.5 semanas)

**Objetivo**: vinheria envia campanhas e acessa dados de uso.

- [ ] `CampanhaService` com filtros pré-definidos
- [ ] `campanhas.jsp` com preview de destinatários
- [ ] Integração WhatsApp API (Z-API sandbox)
- [ ] `relatorios.jsp` — gráficos de comportamento QR Code e clientes
- [ ] `clientes.jsp` — tabela com histórico individual

**Entregável**: Fernanda envia campanha para clientes inativos e vê taxa de conversão do QR Code.

---

### Sprint 7 — Polimento e Deploy (1 semana)

- [ ] Testes de carga básicos (JMeter)
- [ ] Validação de formulários (client + server-side)
- [ ] Tratamento global de erros (página 404/500 customizadas)
- [ ] HTTPS + configuração de subdomínios
- [ ] Deploy em VPS (ex: DigitalOcean, AWS EC2) com Tomcat + Nginx como proxy reverso
- [ ] Documentação de API (Postman Collection)

---

## 12. Decisões Arquiteturais

### Por que JSP para o painel admin e Kotlin para a loja?

O painel admin tem fluxos simples de CRUD que se beneficiam de renderização server-side (menos JavaScript, mais seguro, sem necessidade de SPA). A loja B2C precisa de UX fluida no celular, animações, estado de carrinho e experiência de app — Kotlin é a escolha natural.

### Por que não usar Spring ou Hibernate?

Por tratar-se de um projeto acadêmico (FIAP), a escolha por Servlets + JDBC puro garante que os fundamentos de Java EE sejam exercitados sem a camada de abstração dos frameworks. A arquitetura em camadas (Filter → Servlet → Service → DAO) é idêntica ao que Spring faz por baixo.

### Por que multi-tenancy via `vinheria_id` na mesma base?

Para o volume esperado no MVP (dezenas a centenas de vinherias), um banco compartilhado com isolamento por `vinheria_id` é mais simples de operar, migrar e fazer backup do que múltiplos schemas. A transição para schema-per-tenant pode ser feita no futuro se necessário.

### Segurança

- Senhas com BCrypt (custo 12)
- Prepared Statements em todo o DAO (prevenção de SQL Injection)
- CSRF token em todos os formulários JSP
- Rate limiting no endpoint de OTP (máx 3 tentativas / 10 min)
- Dados sensíveis (CPF, endereço) nunca logados

---

*Arquitetura v1.0 — Vinheiro · Sprint 1 · 2026*
*Equipe de desenvolvimento — FIAP*