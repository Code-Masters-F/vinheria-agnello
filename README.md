# 🍷 Vinheiro — Plataforma SaaS para Vinherias

> **"O Vinheiro é o Shopify dos vinhos: você traz o vinho, nós trazemos a tecnologia."**

O **Vinheiro** é uma plataforma SaaS (Software as a Service) desenvolvida para transformar a operação de vinherias, adegas e restaurantes. Fornecemos toda a infraestrutura digital necessária para que estabelecimentos de qualquer porte possam vender online, fidelizar clientes e oferecer uma experiência de sommelier digital através de Inteligência Artificial.

---

## 🚀 Sobre o Projeto

Diferente de um marketplace comum, o Vinheiro é uma solução "white-label" invisível para o consumidor final. Cada vinheria parceira possui sua própria loja digital com identidade visual customizada, enquanto o Vinheiro gerencia toda a complexidade técnica por trás (pagamentos, recomendações por IA, gestão de catálogo e fidelização).

### 🎯 Público-Alvo
- **B2B (Vinherias):** Proprietários de adegas e restaurantes que buscam digitalizar seu catálogo e automatizar vendas.
- **B2C (Consumidores):** Amantes de vinho que desejam recomendações personalizadas e uma jornada de compra fluida, seja online ou via QR Code na loja física.

---

## ✨ Funcionalidades Principais

### 🏛️ Painel Administrativo (B2B)
- **Gestão de Catálogo:** Cadastro completo de rótulos (tipo, uva, país, safra) com controle automático de estoque e alertas de estoque mínimo.
- **Dashboard de Vendas:** Visão geral de pedidos, ticket médio e comportamento do cliente em tempo real.
- **Programas de Fidelidade:** Configuração de regras de pontuação (pontos por real gasto) e resgate de recompensas.
- **Campanhas Segmentadas:** Disparo de mensagens personalizadas via WhatsApp/E-mail baseadas no histórico de compra (ex: "clientes que compraram Malbec nos últimos 60 dias").
- **QR Code Engine:** Geração automática de códigos para mesas ou prateleiras, permitindo que o cliente abra a loja instantaneamente.

### 🛍️ Loja Digital (B2C)
- **Sommelier por IA:** O motor "Me ajude a escolher" utiliza 3 perguntas rápidas e cruza com o histórico do usuário para sugerir os 3 vinhos ideais no estoque atual.
- **Checkout Integrado:** Pagamentos seguros (Pix, Cartão) com opções de delivery ou retirada na loja.
- **Histórico e Favoritos:** Acesso fácil a compras anteriores para recompra rápida.
- **Carteira de Fidelidade:** Acompanhamento de pontos acumulados e resgate direto na loja.

---

## 🛠️ Stack Tecnológica

O projeto foi construído seguindo rigorosos padrões de engenharia de software e arquitetura em camadas:

- **Backend:** Java 17 com Jakarta Servlet API 5.0.
- **Interface Admin:** JSP (Jakarta Server Pages) 3.0 + JSTL.
- **Design System:** Vanilla CSS com variáveis customizadas, foco em **Glassmorphism** e design responsivo (unidades relativas `rem`/`em`).
- **Banco de Dados:** PostgreSQL (Produção/RDS) e H2 (Testes).
- **Persistência:** JDBC com pool de conexões **HikariCP**.
- **Segurança:** Criptografia **BCrypt** para senhas e política de Cookies `httpOnly`.
- **Servidor:** Jetty (ambiente de desenvolvimento).

---

## 🏗️ Arquitetura e Padrões

A aplicação segue uma arquitetura **MVC (Model-View-Controller)** com separação clara de responsabilidades:

1.  **Servlet Layer (Controllers):** Roteamento e tratamento de requisições.
2.  **Service Layer:** Onde reside toda a lógica de negócio (Regras de fidelidade, calculo de recomendação, etc).
3.  **DAO Layer:** Abstração total do acesso ao banco de dados utilizando JDBC puro para máxima performance.
4.  **Filter Layer:** Implementação de **Multi-tenancy** (isolamento de dados por vinheria) e filtros de segurança/autenticação.

> [!IMPORTANT]
> **TDD (Test-Driven Development):** Todas as camadas de serviço e DAO são cobertas por testes unitários e de integração utilizando **JUnit 5** e **Mockito**.

---

## 🚦 Como Executar

### Pré-requisitos
- Java 17 LTS
- Maven 3.8+
- Banco de Dados PostgreSQL (ou utilize o profile de testes com H2)

### Passos para Instalação

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/Code-Masters-F/vinheria-agnello.git
   cd vinheria-agnello
   ```

2. **Configure as variáveis de ambiente:**
   Crie um arquivo `.env` na raiz do projeto baseado no `.env.example`:
   ```env
   DB_URL=jdbc:postgresql://localhost:5432/vinheiro_db
   DB_USER=seu_usuario
   DB_PASSWORD=sua_senha
   DB_DRIVER=org.postgresql.Driver
   ```

3. **Crie o banco de dados:**
   Execute os scripts localizados na raiz:
   ```bash
   # Primeiro o esquema
   psql -U seu_usuario -d vinheiro_db -f schema.sql
   # Opcional: Dados de demonstração
   psql -U seu_usuario -d vinheiro_db -f data.sql
   ```

4. **Execute localmente via Jetty:**
   ```bash
   mvn jetty:run
   ```
   Acesse a aplicação em: `http://localhost:8080`

---

## 📄 Documentação Adicional

Para mais detalhes técnicos e de negócio, consulte a pasta `docs/`:
- [Guia de Arquitetura](docs/PROJECT_ARCHITECTURE.md)
- [Visão Geral do Produto](docs/PROJECT_OVERVIEW.md)
---

## 🤝 Contribuição e Licença

Este é um projeto proprietário desenvolvido pela equipe **CodeMasters**.

---
*Vinheria Agnello — Elevando a cultura do vinho com tecnologia.*
