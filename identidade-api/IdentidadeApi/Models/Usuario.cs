namespace IdentidadeApi.Models;

/// <summary>Perfis de acesso (RBAC) definidos na arquitetura de microsserviços.</summary>
public static class Perfis
{
    public const string Admin = "Admin";
    public const string Cliente = "Cliente";
}

/// <summary>
/// Usuário autenticável da plataforma (cliente B2C ou administrador da vinheria B2B).
/// Sempre associado a uma vinheria, para manter o isolamento multi-tenant.
/// </summary>
public sealed class Usuario
{
    public long Id { get; init; }
    public long VinheriaId { get; init; }
    public string Nome { get; init; } = string.Empty;
    public string Email { get; init; } = string.Empty;
    public string SenhaHash { get; init; } = string.Empty;
    public string Perfil { get; init; } = Perfis.Cliente;
    public bool Ativo { get; init; } = true;
}
