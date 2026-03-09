# 🍷 Vinheiro — SaaS para Vinherias
! Este é um documento não-técnico utilizado para o desenvolvedor/arquiteto/cliente entender a lógica de negócio do projeto e entender o objetivo do projeto. Deve ser entendido com facilidade por qualquer pessoa.

> **"O Vinheiro é o Shopify dos vinhos: você traz o vinho, nós trazemos a tecnologia."**

O Vinheiro é uma plataforma SaaS que fornece às vinherias, adegas e restaurantes toda a infraestrutura digital para vender melhor, fidelizar clientes e se comunicar de forma segmentada — sem precisar contratar equipe de TI ou sommelier em tempo integral.

O Vinheiro **não vende vinho**, não gerencia estoque físico e não faz entrega. Essas responsabilidades permanecem com a vinheria. A plataforma fornece a tecnologia por baixo: cada vinheria parceira tem sua própria loja digital com identidade visual, motor de recomendação por IA, painel de gestão e ferramentas de fidelização. O consumidor final interage com a marca da vinheria — o Vinheiro é invisível.

---

## 📋 Índice

- [O Problema](#-o-problema)
- [Público-Alvo](#-público-alvo)
- [A Solução](#-a-solução)
- [Funcionalidades](#-funcionalidades)
- [Motor de Recomendação por IA](#-motor-de-recomendação-por-ia--funcionalidade-core)

---

## 🔴 O Problema

### Dores da Vinheria (B2B)

O Vinheiro existe para resolver problemas operacionais e comerciais que vinherias, adegas e restaurantes de pequeno e médio porte enfrentam no dia a dia:

**Carta de vinhos desatualizada**
A maioria das vinherias opera com cardápio impresso ou PDFs enviados por WhatsApp. Quando um vinho acaba, o cliente só descobre na hora do pedido. A vinheria perde venda e credibilidade.

**Impossível manter sommelier em todos os turnos**
Contratar um sommelier custa entre R$ 3.000 e R$ 8.000/mês — inviável para a maioria dos estabelecimentos. Sem orientação especializada, o cliente escolhe pelo preço e a vinheria perde margem.

**Sem canal digital próprio**
Pedidos chegam por WhatsApp de forma informal, sem automação, sem catálogo atualizado e sem pagamento integrado. A vinheria perde vendas fora do horário de funcionamento e não aproveita o crescimento do consumo online — que já representa quase 1/3 do mercado de vinhos no Brasil.

**Fidelização manual e sem escala**
Programas de fidelidade são feitos em cartõezinhos de papel ou planilhas. Não há gatilho de recompra, nem comunicação automática com clientes que somem.

**Comunicação em massa e sem segmentação**
Quando chega um Malbec novo, a vinheria manda a mesma mensagem para todos os contatos do WhatsApp — incluindo quem só bebe espumante. A vinheria não consegue avisar só os clientes certos, porque o canal não permite segmentação por histórico de compra.

---

### Dores do Consumidor Final (B2C)

**Insegurança na escolha**
- Centenas de rótulos sem orientação clara
- Vocabulário técnico que intimida (terroir, taninos, denominação de origem)
- Vergonha de perguntar — cliente escolhe pelo preço para não errar
- 52% dos consumidores citam preço elevado como barreira, mas a raiz é não saber justificar o gasto
- Decisão delegada a amigos e familiares por falta de confiança no próprio julgamento

**Experiência de compra limitada**
- Sem canal digital próprio da vinheria favorita
- Sem histórico de compras acessível para recompra fácil
- Sem notificações de novidades relevantes ao seu perfil
- Sem programa de fidelidade que reconheça o cliente frequente

---

## 👥 Público-Alvo

O Vinheiro opera em dois níveis simultâneos.

### Cliente B2B — A Vinheria (quem paga pelo SaaS)
- Vinherias, adegas e restaurantes de pequeno a médio porte
- Sem equipe de TI própria
- Dono ou gerente é o decisor de compra
- Já usa WhatsApp como canal de vendas informal
- Paga assinatura mensal pelo acesso à plataforma

### Cliente B2C — O Consumidor Final (quem usa a loja gerada pelo SaaS)
- Clientes das vinherias parceiras
- Perfil variado: do iniciante ao apreciador frequente
- Acessa a loja digital com a marca da vinheria
- Usa a IA para descobrir vinhos ou navega pelo catálogo sozinho
- Acumula histórico e pontos de fidelidade

### Personas

| Persona | Perfil | Dor Principal |
|---|---|---|
| **Fernanda Alcântara**, 44 anos | Proprietária de vinheria em BH | Perde vendas por não ter canal digital. Gerencia estoque em planilha e atende por WhatsApp |
| **Bruno Oliveira**, 32 anos | Analista de TI em SP, consumidor frequente | Sente vergonha na loja quando não sabe o que pedir. Escolhe pelo preço para não errar |
| **Rafael Mendes**, 38 anos | Dono de restaurante em Florianópolis, sem sommelier | Carta de vinhos desatualizada. Garçons sem preparo para orientar clientes sobre vinhos |

---

## 💡 A Solução

O Vinheiro entrega dois ambientes integrados:

- **Painel da Vinheria (B2B)** — o dono configura a loja, gerencia estoque, acompanha pedidos e cria campanhas segmentadas
- **Loja Digital (B2C)** — o consumidor navega pelo catálogo, recebe recomendação da IA ou busca diretamente, e finaliza o pedido com pagamento integrado

Cada vinheria tem sua própria URL (`vinheira-nome.vinheiro.com.br`) com logo e cores personalizadas. O consumidor vê apenas a marca da vinheria — não o Vinheiro.

---

## ⚙️ Funcionalidades

### 🏪 Painel da Vinheria — B2B

| Funcionalidade | Descrição |
|---|---|
| **Gestão de Catálogo e Estoque** | Cadastro de vinhos com nome, tipo, uva, país, safra, preço e foto. Estoque atualizado em tempo real — vinho esgotado some automaticamente da loja. Alerta de estoque mínimo configurável. |
| **Configuração da Loja** | Personalização com logotipo, cores e nome próprios. URL exclusiva por vinheria. |
| **Gestão de Pedidos** | Pedidos em tempo real no painel. Confirmação, separação e atualização de status com notificação automática ao cliente. |
| **Histórico e Perfil de Clientes** | Visualização de quem são os clientes cadastrados, o que compraram, frequência e ticket médio. Alimenta a IA e as campanhas. |
| **Campanhas Segmentadas** | Notificações para clientes com perfil específico: quem comprou tinto nos últimos 60 dias, quem não compra há 30 dias, quem nunca experimentou espumante. Disponível apenas para clientes cadastrados. |
| **Programa de Fidelidade** | Configuração de pontos por real gasto, recompensas disponíveis e prazo de validade. Gerenciado pelo painel sem suporte técnico. |
| **QR Code para Loja Física** | QR code gerado automaticamente. Cliente escaneia na mesa ou gôndola, abre a loja no navegador sem baixar app, responde o questionário e faz o pedido. |
| **Relatórios** | Dois painéis independentes: **Comportamento da Loja Física** (dados agregados e anônimos do QR Code: escaneamentos, ocasiões mais escolhidas, faixas de preço, taxa de conversão) e **Clientes Cadastrados** (histórico individual, ticket médio, frequência de retorno). |

---

### 🛍️ Loja Digital — B2C

| Funcionalidade | Descrição |
|---|---|
| **Pontos de Entrada para o Consumidor** | O consumidor pode chegar à loja digital da vinheria por diferentes caminhos: QR Code na mesa ou na gôndola da loja física, link compartilhado pela vinheria no Instagram ou WhatsApp, ou indicação de outro cliente. Em todos os casos, a loja abre diretamente no navegador do celular — sem necessidade de baixar aplicativo. O questionário de recomendação está disponível em todos os pontos de entrada, com ou sem cadastro. |
| **Dois Caminhos de Entrada** | Ao abrir a loja, o consumidor escolhe: **"Já sei o que quero"** → vai ao catálogo com busca e filtros. **"Me ajude a escolher"** → inicia o questionário da IA. |
| **Catálogo com Busca e Filtros** | Busca por nome, uva, país ou produtor. Filtros por tipo, preço, ocasião e harmonização. Apenas vinhos em estoque são exibidos. Linguagem do dia a dia, sem termos técnicos. |
| **Motor de Recomendação por IA** | 3 perguntas visuais → 3 sugestões personalizadas. Detalhado na seção abaixo. |
| **Checkout Integrado** | Pix, cartão de crédito e parcelamento. Opção de retirada na loja ou entrega (a cargo da vinheria). Pagamento processado pelo Vinheiro e repassado à vinheria. |
| **Histórico de Compras e Favoritos** | Acesso ao histórico completo, rótulos favoritos e recompra com um clique. Disponível para usuários cadastrados. |
| **Notificações Personalizadas** | Disponível para todos os cadastrados — físicos e online. Canal: WhatsApp (cadastro físico) ou e-mail/WhatsApp (cadastro online). Sempre baseadas no perfil de compras do cliente, nunca genéricas. |
| **Programa de Fidelidade** | Acúmulo automático de pontos a cada compra. Saldo visível na loja. Resgate conforme regras configuradas pela vinheria. |
| **Cadastro Físico** | Convite opcional ao fim do pedido presencial. Coleta apenas nome + WhatsApp ou e-mail. Sem senha — acesso por contato. |
| **Cadastro Online** | Criado durante o checkout de compra com entrega. Coleta nome completo, CPF, endereço, telefone, e-mail e senha. Conta ativa imediatamente após o pedido. |

> **MVP:** cadastro vinculado por vinheria. **Versão futura:** conta única que funciona em todos os estabelecimentos parceiros.

---

## 🤖 Motor de Recomendação por IA — Funcionalidade Core

### O que é
O motor de recomendação é a principal inovação do Vinheiro. Diferente de um quiz estático feito uma vez no cadastro, ele captura o **contexto da compra atual** e combina com o histórico acumulado do usuário.

### Como funciona

**Passo 1 — 3 perguntas visuais (cards com ícone + texto)**

```
🥩 Churrasco    🍝 Jantar em casa    💝 Presente    🧀 Petisco    🎉 Comemoração
        → Qual estilo você prefere?   → Qual faixa de preço?
```

Respondidas em até 20 segundos. Sem cadastro obrigatório. Sem termos técnicos.

**Passo 2 — Identificação do usuário**

Antes do cruzamento de dados, o sistema precisa saber quem está acessando:

| Tipo de usuário | Como se identifica |
|---|---|
| Anônimo | Não se identifica — acessa direto |
| Cadastrado presencialmente | Digita o WhatsApp cadastrado e recebe um código de 6 dígitos por SMS/WhatsApp para confirmar |
| Cadastrado online | Faz login com e-mail e senha criados no checkout |

**Passo 3 — Cruzamento com dados disponíveis**

| Tipo de usuário | Dados usados na recomendação |
|---|---|
| Anônimo | Respostas do questionário + estoque real da vinheria |
| Cadastrado (presencial ou online) | Respostas do questionário + estoque + histórico de compras e avaliações |

**Passo 3 — 3 sugestões ranqueadas**
Cada sugestão exibe nome, foto, preço e descrição em linguagem simples. Sem jargão técnico.

### Como a IA aprende (apenas usuários cadastrados)
A cada nova compra registrada, o sistema acumula padrões individuais:

```
"Toda vez que Bruno compra para churrasco,
 prefere tintos acessíveis e raramente avalia bem vinhos acima de R$ 80."
```

Com o tempo, as perguntas chegam pré-preenchidas com as preferências salvas e as sugestões ficam progressivamente mais precisas. Usuários anônimos começam do zero a cada acesso — sem histórico acumulado.

### Onde é acionado
- Na loja digital, quando o consumidor escolhe **"Me ajude a escolher"**
- No QR Code da loja física ou restaurante, após escanear o código

---

## 🗺️ Modelo de Negócio

- A **vinheria paga** assinatura mensal pelo SaaS (Starter, Pro ou Restaurante)
- O **consumidor usa de graça** a loja digital gerada pela vinheria
- O Vinheiro **processa os pagamentos** das compras online e repassa à vinheria
- A **entrega é responsabilidade da vinheria** — ela define área e prazo no painel
- Sem comissão por venda — receita baseada apenas em assinatura

---

*Documento de referência para desenvolvimento — Sprint 1 · 2026*
