# Research & Decisions

## Decision 1: ORM Framework
- **Decision:** Dapper
- **Rationale:** The user explicitly requires that the database schema is managed strictly by the existing SQL scripts, without reliance on the C# application to run migrations or create tables. Dapper is a lightweight micro-ORM that allows executing raw SQL queries and mapping them to C# objects perfectly, avoiding the overhead and "magic" of EF Core.
- **Alternatives considered:** Entity Framework Core (rejected due to heavier overhead and the risk of accidental schema migrations).

## Decision 2: API Architecture
- **Decision:** ASP.NET Core Minimal APIs
- **Rationale:** Minimal APIs provide a clean, concise way to define endpoints without the boilerplate of traditional Controllers. This fits the scope of a simple CRUD API for a single entity (`Produto`).
- **Alternatives considered:** ASP.NET Core MVC Controllers (rejected as unnecessary overhead for a simple API).

## Decision 3: Authentication
- **Decision:** None
- **Rationale:** The user specified that if authentication doesn't already exist, it shouldn't be added, as this is an academic project.
- **Alternatives considered:** JWT tokens, Cookie auth (rejected based on user constraint).
