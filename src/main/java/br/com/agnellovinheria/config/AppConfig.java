package br.com.agnellovinheria.config;

public class AppConfig {
    /**
     * Centro de controle global da aplicação. Será responsável por centralizar
     * constantes e propriedades que não pertencem ao banco de dados, mas que
     * governam o comportamento do sistema
     */

    // --- 🔐 SEGURANÇA & SESSÃO ---
    // Define a força da criptografia BCrypt (12 é o padrão de mercado atual)
    public static final int BCRYPT_SALT_ROUNDS = 12;

    // Expiração da sessão do usuário (ex: 24 horas)
    public static final int SESSION_TIMEOUT_SECONDS = 60 * 60 * 24;

    // --- 🍷 REGRAS DE NEGÓCIO ---
    // Valor padrão de fidelidade caso a vinheria não configure o valor customizado
    public static final double DEFAULT_PONTOS_POR_REAL = 1.0;
    // Limite crítico de estoque para emitir alertas no Dashboard
    public static final int ESTOQUE_MINIMO_CRITICO = 5;
    // Quantidade de vinhos exibidos por página de catálogo
    public static final int ITENS_POR_PAGINA = 12;

    // --- 🤖 MOTOR DE RECOMENDAÇÃO (IA) ---
    // Configurações para integração futura com a Anthropic Claude API
    public static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";

    // --- 📁 ARQUIVOS E UPLOADS
    // Caminho relativo para salvar as imagens de upload
    public static final String UPLOAD_PATH_VINHOS = "/static/img/vinhos";

    // --- 🌍 AMBIENTE ---
    // Flag para diferenciar logs de desenvolvimento de produção
    public static final boolean IS_DEVELOPMENT = true;

    /**
     * Construtor privado para impedir a instanciação da classe.
     * Como é uma classe de constantes utilitários, não queremos objetos dela.
     */
    private AppConfig() {
    }
}
