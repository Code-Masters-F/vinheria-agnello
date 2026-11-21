using System.Globalization;
using System.Security.Claims;

namespace VinheriaApi.Security
{
    /// <summary>Leitura das claims emitidas pelo serviço de Identidade.</summary>
    public static class ClaimsPrincipalExtensions
    {
        public const string ClaimVinheriaId = "vinheria_id";

        /// <summary>Vinheria (tenant) do usuário autenticado, ou <c>null</c> se a claim estiver ausente/inválida.</summary>
        public static long? VinheriaId(this ClaimsPrincipal usuario)
        {
            // [TokenOptimization] - Leitura direta da claim já validada pelo middleware, sem nova decodificação do token.
            var valor = usuario.FindFirst(ClaimVinheriaId)?.Value;
            return long.TryParse(valor, NumberStyles.Integer, CultureInfo.InvariantCulture, out var id) ? id : null;
        }
    }
}
