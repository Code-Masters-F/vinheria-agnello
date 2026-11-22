# Serviço de Catálogo - build multi-stage
# Contexto de build: pasta csharp-api/
#   docker build -t vinheria/catalogo csharp-api

# ---------- Stage 1: build e publicação ----------
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src

# Restaura as dependências primeiro para aproveitar o cache de camadas do Docker
COPY VinheriaApi/VinheriaApi.csproj VinheriaApi/
RUN dotnet restore VinheriaApi/VinheriaApi.csproj

COPY VinheriaApi/ VinheriaApi/
RUN dotnet publish VinheriaApi/VinheriaApi.csproj -c Release -o /app/publish --no-restore

# ---------- Stage 2: runtime ----------
# Imagem Alpine: menor superfície de ataque e já traz o cliente DHCP (udhcpc) do BusyBox
FROM mcr.microsoft.com/dotnet/aspnet:8.0-alpine AS runtime
WORKDIR /app
COPY --from=build /app/publish .

ENV ASPNETCORE_HTTP_PORTS=8080
EXPOSE 8080

# Executa sem privilégios de root (usuário 'app' das imagens oficiais do .NET 8)
USER $APP_UID

ENTRYPOINT ["dotnet", "VinheriaApi.dll"]
