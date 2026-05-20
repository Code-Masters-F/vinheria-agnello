# 🍷 Vinheiro — Plataforma SaaS para Vinherias

> Para entender o produto, o problema que ele resolve, funcionalidades, stack e arquitetura, consulte a [Visão Geral do Produto](docs/PROJECT_OVERVIEW.md).

---

## 🚦 Como Executar

### Pré-requisitos
- Java 17 LTS
- Maven 3.8+
- Banco de Dados PostgreSQL (ou utilize o profile de testes com H2)
- Python 3.10+ (apenas para a camada de ML em `ml/`)

### Passos para Instalação

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/Code-Masters-F/vinheria-agnello.git
   cd vinheria-agnello
   ```

2. **Instale as dependências:**

   **Java (Maven):** Baixa as dependências do `pom.xml`, compila o projeto e executa os testes:
   ```bash
   mvn clean install
   ```

   Para pular os testes durante a build:
   ```bash
   mvn clean install -DskipTests
   ```

   **Python (camada de ML):** Necessário apenas se for treinar ou rodar os scripts em `ml/`. Recomenda-se usar um ambiente virtual:
   ```bash
   cd ml
   python -m venv .venv
   source .venv/bin/activate   # Linux/macOS
   # .venv\Scripts\activate    # Windows
   pip install -r requirements.txt
   cd ..
   ```

3. **Configure as variáveis de ambiente:**
   Crie um arquivo `.env` na raiz do projeto (ao lado do `pom.xml`) baseado no `.env.example`:
   ```env
   DB_URL=jdbc:postgresql://localhost:5432/vinheiro_db
   DB_USER=seu_usuario
   DB_PASSWORD=sua_senha
   DB_DRIVER=org.postgresql.Driver
   ```

4. **Crie o banco de dados e o schema:**

   ```bash
   # Crie o banco (pule se ja tiver um banco criado, ex: postgres)
   psql -U seu_usuario -c "CREATE DATABASE vinheiro_db;"
   ```

   A aplicacao usa o schema `vinheria_db` (configurado em `DatabaseConfig.java`). Conecte ao banco criado e execute:
   ```bash
   psql -U seu_usuario -d vinheiro_db -c "CREATE SCHEMA IF NOT EXISTS vinheria_db;"
   ```

   Em seguida, execute os scripts SQL com o search_path apontando para o schema:
   ```bash
   # Estrutura das tabelas
   psql -U seu_usuario -d vinheiro_db -c "SET search_path TO vinheria_db;" -f schema.sql

   # Dados de demonstracao (opcional)
   psql -U seu_usuario -d vinheiro_db -c "SET search_path TO vinheria_db;" -f data.sql
   ```

   > O nome do banco (`vinheiro_db`) deve corresponder ao que foi configurado na variavel `DB_URL` do passo anterior.

5. **Execute localmente via Jetty:**
   ```bash
   mvn jetty:run
   ```

6. **Acesse a aplicacao:**

   Abra o navegador em `http://localhost:8080/auth/login`.

   Se executou o `data.sql`, use as credenciais de teste:

   | Campo | Valor |
   |-------|-------|
   | Email | `admin@agnello.com` |
   | Senha | `admin123` |

### Rotas Disponiveis

| URL | Descricao |
|-----|-----------|
| `/auth/login` | Tela de login (ponto de entrada) |
| `/admin/dashboard` | Dashboard principal |
| `/admin/catalogo` | Gestao do catalogo de vinhos |
| `/admin/pedidos` | Gestao de pedidos |
| `/admin/relatorios` | Relatorios de vendas |
| `/admin/qrcode` | Gerador de QR Codes |

> Todas as rotas `/admin/*` exigem autenticacao. Sem login, voce sera redirecionado para `/auth/login`.

---

## 📄 Documentação Adicional

- [Guia de Arquitetura](docs/PROJECT_ARCHITECTURE.md)
- [Visão Geral do Produto](docs/PROJECT_OVERVIEW.md)

---

*Vinheria Agnello — Elevando a cultura do vinho com tecnologia.*
