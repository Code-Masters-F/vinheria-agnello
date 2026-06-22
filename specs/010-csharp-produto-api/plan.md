# Implementation Plan: C# API for Produto CRUD

## Technical Context
- **Framework:** ASP.NET Core 8+ (Minimal APIs)
- **Data Access:** Dapper (Micro-ORM)
- **Database:** MySQL (Shared with the Java application)
- **Authentication:** None (Public endpoints for the academic project context)
- **Schema Management:** Handled via existing SQL scripts (`database/schema.sql`). The C# app will NOT run migrations.

## Constitution Check
- **Token Optimization:** Will use standard Dapper connection practices and dependency injection to keep logic minimal.
- **UI/UX Patterns:** N/A (Backend only).
- **Security:** Standard REST practices. Auth is explicitly excluded per user requirements.
- **Software Architecture:** Layered structure (Routes/Endpoints -> Repository -> Dapper).
- **TDD:** Tests will be written using xUnit and Moq or testcontainers before implementation.
- **Documentation:** All endpoints will be documented via Swagger/OpenAPI.

## Outline

### Phase 1: Foundation & Setup
- Initialize ASP.NET Core Web API project (`dotnet new webapi`).
- Add Dapper and MySqlConnector NuGet packages.
- Configure `appsettings.json` with the existing MySQL connection string.

### Phase 2: Domain & Data Access
- Create `Produto` model/entity mirroring the database table.
- Create `IProdutoRepository` interface.
- Implement `ProdutoRepository` using Dapper for `Get`, `GetAll`, `Create`, `Update`, `Delete`.

### Phase 3: Endpoints (Minimal API)
- Map `GET /api/produtos` -> `GetAll`
- Map `GET /api/produtos/{id}` -> `GetById`
- Map `POST /api/produtos` -> `Create`
- Map `PUT /api/produtos/{id}` -> `Update`
- Map `DELETE /api/produtos/{id}` -> `Delete`
- Add validations (FluentValidation or simple bad requests) for the inputs.

### Phase 4: Testing & Verification
- Write unit tests for the endpoints and repository.
- Run the API locally and verify with HTTP requests.
- Verify that it connects to the local MySQL instance correctly.

## Execution Readiness
The feature is fully specified and the technical path (Dapper + Minimal APIs) is approved.
