using System.Text.RegularExpressions;

namespace IdentidadeApi.Tests;

/// <summary>
/// Testes de regressão de segurança (DevSecOps): nenhum arquivo de configuração versionado pode conter
/// senha de banco ou chave privada. Esses valores vêm de variáveis de ambiente / arquivos montados.
/// </summary>
public class ConfiguracaoSegurancaTests
{
    // Casa "Password=<valor>" (connection string) e "Password": "<valor>" (propriedade JSON), com valor não vazio.
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
    public void ArquivoDeConfiguracao_NaoContemSegredos(string arquivo)
    {
        // [TokenOptimization] - Leitura única do arquivo e validação por regex estática.
        var conteudo = File.ReadAllText(Path.Combine(LocalizarProjetoApi(), arquivo));

        Assert.False(SenhaPreenchida.IsMatch(conteudo), $"{arquivo} contém uma senha de banco.");
        Assert.DoesNotContain("PRIVATE KEY", conteudo);
    }

    private static string LocalizarProjetoApi()
    {
        // [TokenOptimization] - Sobe os diretórios a partir da saída do teste e para no primeiro match.
        var dir = new DirectoryInfo(AppContext.BaseDirectory);
        while (dir is not null)
        {
            var candidato = Path.Combine(dir.FullName, "IdentidadeApi", "IdentidadeApi.csproj");
            if (File.Exists(candidato)) return Path.GetDirectoryName(candidato)!;
            dir = dir.Parent;
        }
        throw new DirectoryNotFoundException("Projeto IdentidadeApi não encontrado a partir de " + AppContext.BaseDirectory);
    }
}
