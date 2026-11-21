using IdentidadeApi.Models;

namespace IdentidadeApi.Repositories;

/// <summary>Acesso aos usuários do banco próprio da Identidade (Database per Service).</summary>
public interface IUsuarioRepository
{
    /// <summary>Busca um usuário pelo e-mail, sem diferenciar maiúsculas/minúsculas. Retorna <c>null</c> se não existir.</summary>
    Task<Usuario?> BuscarPorEmailAsync(string email, CancellationToken ct = default);
}
