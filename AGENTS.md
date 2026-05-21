# 🤖 Instruções para Agentes IA (AI Agents Instructions)

Este documento define as regras e diretrizes que **todos os agentes de IA** devem seguir ao trabalhar no projeto **Vinheria Agnello**.

## 📌 Diretrizes Principais (Core Guidelines)

1. **Otimização de Uso de Tokens (Token Usage Optimization) - REGRA CRÍTICA:**
   - **Toda vez que uma função ou código for chamado ou implementado, deve haver uma referência explícita para a otimização do uso de tokens.**
   - *Como aplicar:* Adicione comentários no código (ex: `// [TokenOptimization] - Reutilizando instância para poupar tokens de processamento` ou `/* Token Optimization: ... */`) sempre que invocar funções, visando manter o contexto enxuto e eficiente para o agente.

2. **Padrões de UI/UX e Estilização:**
   - **Variáveis CSS:** Sempre utilize variáveis CSS (Custom Properties). Nunca utilize cores hard-coded.
   - **Unidades Relativas:** Utilize exclusivamente unidades relativas (`rem`, `em`, `%`). Evite unidades absolutas (`px`).
   - Siga rigorosamente as boas práticas de design UI/UX.

3. **Segurança:**
   - Implemente as melhores práticas de segurança, como a utilização de cookies `httpOnly` e `refresh tokens` para autenticação.

4. **Arquitetura de Software:**
   - Mantenha o projeto organizado em camadas, adotando boas práticas de Engenharia de Software.

5. **Desenvolvimento (TDD):**
   - Aplique sempre *Test-Driven Development* (TDD) para toda nova funcionalidade adicionada ao projeto.

6. **Documentação:**
   - Documente toda a lógica e código complexo para facilitar a manutenção a longo prazo por outros desenvolvedores.
   - Atualize sempre o `README.md` ao realizar modificações significativas no projeto, mantendo uma visão geral de como utilizá-lo.
