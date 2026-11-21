#!/usr/bin/env bash
# Roda os testes automatizados dos dois microsserviços (os mesmos executados pelo pipeline do Jenkins).
# Usa o .NET local se ele tiver o runtime do ASP.NET Core 8; caso contrário, roda no container oficial do
# SDK 8 (os testes falham em runtimes mais novos por incompatibilidade do Mvc.Testing 8).
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJETOS="identidade-api/IdentidadeApi.Tests/IdentidadeApi.Tests.csproj csharp-api/VinheriaApi.Tests/VinheriaApi.Tests.csproj"

if command -v dotnet >/dev/null 2>&1 && dotnet --list-runtimes 2>/dev/null | grep -q '^Microsoft.AspNetCore.App 8\.'; then
    echo "==> Usando o .NET local"
    cd "$RAIZ"
    for PROJETO in $PROJETOS; do dotnet test "$PROJETO"; done
else
    echo "==> .NET 8 não encontrado: rodando no container mcr.microsoft.com/dotnet/sdk:8.0"
    # Cópia do código dentro do container (repositório montado só-leitura; nada é gravado na sua pasta)
    docker run --rm --security-opt label=disable -e DOTNET_NOLOGO=1 -e DOTNET_CLI_TELEMETRY_OPTOUT=1 \
        -v "$RAIZ/identidade-api:/src/identidade-api:ro" -v "$RAIZ/csharp-api:/src/csharp-api:ro" \
        mcr.microsoft.com/dotnet/sdk:8.0 sh -c "cp -r /src /work && cd /work \
            && find . -type d \( -name bin -o -name obj \) -prune -exec rm -rf {} + \
            && for P in $PROJETOS; do dotnet test \"\$P\" || exit 1; done"
fi
