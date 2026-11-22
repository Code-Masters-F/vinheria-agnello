using System.IO;
using System.Text.RegularExpressions;
using Xunit;

namespace VinheriaApi.Tests
{
    /// <summary>
    /// Testes de regressão de segurança (DevSecOps): garantem que nenhum arquivo de
    /// configuração versionado contenha credenciais. A connection string deve vir de
    /// variável de ambiente (ConnectionStrings__DefaultConnection), nunca do repositório.
    /// </summary>
    public class ConfiguracaoSegurancaTests
    {
        // Casa "Password=<valor>" (connection string Npgsql/ADO.NET) e "Password": "<valor>" (propriedade JSON),
        // com valor não vazio.
        private static readonly Regex SenhaPreenchida = new(
            @"(Password|Pwd)\s*=\s*[^;""\s]+|""(Password|Pwd)""\s*:\s*""[^""]+""", RegexOptions.IgnoreCase);

        [Theory]
        [InlineData("Host=db;Password=segredo")]
        [InlineData("{ \"Password\": \"segredo\" }")]
        [InlineData("{ \"pwd\":\"segredo\" }")]
        public void Regex_DetectaSenhaEmConnectionStringEEmPropriedadeJson(string conteudo)
        {
            Assert.Matches(SenhaPreenchida, conteudo);
        }

        [Theory]
        [InlineData("Host=db;Password=")]
        [InlineData("{ \"Password\": \"\" }")]
        public void Regex_IgnoraSenhaVazia(string conteudo)
        {
            Assert.DoesNotMatch(SenhaPreenchida, conteudo);
        }

        [Theory]
        [InlineData("appsettings.json")]
        [InlineData("appsettings.Development.json")]
        public void ArquivoDeConfiguracao_NaoContemSenha(string arquivo)
        {
            // [TokenOptimization] - Lê o arquivo uma única vez e valida via regex compilada estática, sem desserializar o JSON inteiro.
            var conteudo = File.ReadAllText(Path.Combine(LocalizarProjetoApi(), arquivo));

            Assert.False(SenhaPreenchida.IsMatch(conteudo),
                $"{arquivo} contém uma senha. Use a variável de ambiente ConnectionStrings__DefaultConnection.");
        }

        /// <summary>
        /// Sobe a árvore de diretórios a partir da saída do teste (bin/...) até achar a pasta do projeto VinheriaApi.
        /// </summary>
        private static string LocalizarProjetoApi()
        {
            // [TokenOptimization] - Busca incremental pelos diretórios pais; para no primeiro match.
            var dir = new DirectoryInfo(System.AppContext.BaseDirectory);
            while (dir is not null)
            {
                var candidato = Path.Combine(dir.FullName, "VinheriaApi", "VinheriaApi.csproj");
                if (File.Exists(candidato)) return Path.GetDirectoryName(candidato)!;
                dir = dir.Parent;
            }
            throw new DirectoryNotFoundException("Projeto VinheriaApi não encontrado a partir de " + System.AppContext.BaseDirectory);
        }
    }
}
