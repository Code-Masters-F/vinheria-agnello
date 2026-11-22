namespace IdentidadeApi.Contracts;

/// <summary>Corpo da requisição de login.</summary>
public sealed record LoginRequest(string? Email, string? Senha);

/// <summary>Resposta de login bem-sucedido. O token deve ser enviado no header <c>Authorization: Bearer</c>.</summary>
public sealed record LoginResponse(string AccessToken, string TokenType, int ExpiresIn);

/// <summary>Token de acesso emitido pela Identidade.</summary>
public sealed record TokenEmitido(string AccessToken, int ExpiraEmSegundos);

/// <summary>
/// Sessão emitida no login ou na renovação: token de acesso (vai no corpo da resposta) e refresh token
/// (vai somente no cookie httpOnly, nunca no corpo).
/// </summary>
public sealed record SessaoEmitida(TokenEmitido Acesso, string RefreshToken, DateTime RefreshExpiraEm);
