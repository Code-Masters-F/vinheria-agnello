---
description: "Task list template for C# API for Produto CRUD implementation"
---

# Tasks: C# API for Produto CRUD

**Input**: Design documents from `/specs/010-csharp-produto-api/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/api.md

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Initialize ASP.NET Core Minimal API project in `csharp-api/` using `dotnet new webapi -n VinheriaApi`
- [X] T002 Add required NuGet packages (Dapper, MySqlConnector) to `csharp-api/VinheriaApi/VinheriaApi.csproj`
- [X] T003 Configure MySQL connection string in `csharp-api/VinheriaApi/appsettings.json`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T004 Create `Produto` model/entity in `csharp-api/VinheriaApi/Models/Produto.cs`
- [X] T005 Create `IProdutoRepository` interface with basic connection setup in `csharp-api/VinheriaApi/Repositories/IProdutoRepository.cs`
- [X] T006 Register Dapper repository dependency injection in `csharp-api/VinheriaApi/Program.cs`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Read Products (Catalog) (Priority: P1) 🎯 MVP

**Goal**: Mobile user connects to the internet and views the catalog of products. Also retrieves single products.

**Independent Test**: Call `GET /api/produtos` and `GET /api/produtos/{id}` to verify data from MySQL.

### Implementation for User Story 1

- [X] T007 [P] [US1] Implement `GetAll` and `GetById` methods in `IProdutoRepository` and `csharp-api/VinheriaApi/Repositories/ProdutoRepository.cs`
- [X] T008 [US1] Create `GET /api/produtos` endpoint in `csharp-api/VinheriaApi/Program.cs`
- [X] T009 [US1] Create `GET /api/produtos/{id}` endpoint in `csharp-api/VinheriaApi/Program.cs`

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Create Product (Priority: P2)

**Goal**: Mobile user creates a new product through the app interface.

**Independent Test**: Call `POST /api/produtos` and verify insertion in MySQL.

### Implementation for User Story 2

- [X] T010 [P] [US2] Implement `Create` method in `IProdutoRepository` and `csharp-api/VinheriaApi/Repositories/ProdutoRepository.cs`
- [X] T011 [US2] Create `POST /api/produtos` endpoint with basic validation in `csharp-api/VinheriaApi/Program.cs`

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - Update Product (Priority: P3)

**Goal**: Mobile user updates an existing product.

**Independent Test**: Call `PUT /api/produtos/{id}` and verify update in MySQL.

### Implementation for User Story 3

- [X] T012 [P] [US3] Implement `Update` method in `IProdutoRepository` and `csharp-api/VinheriaApi/Repositories/ProdutoRepository.cs`
- [X] T013 [US3] Create `PUT /api/produtos/{id}` endpoint in `csharp-api/VinheriaApi/Program.cs`

**Checkpoint**: All user stories up to US3 should now be independently functional

---

## Phase 6: User Story 4 - Delete Product (Priority: P4)

**Goal**: Mobile user deletes a product.

**Independent Test**: Call `DELETE /api/produtos/{id}` and verify removal in MySQL.

### Implementation for User Story 4

- [X] T014 [P] [US4] Implement `Delete` method in `IProdutoRepository` and `csharp-api/VinheriaApi/Repositories/ProdutoRepository.cs`
- [X] T015 [US4] Create `DELETE /api/produtos/{id}` endpoint in `csharp-api/VinheriaApi/Program.cs`

**Checkpoint**: All user stories should now be independently functional

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [X] T016 Setup Swagger/OpenAPI documentation mapping in `csharp-api/VinheriaApi/Program.cs`
- [X] T017 Verify error handling returns proper JSON schemas

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can proceed sequentially in priority order (US1 → US2 → US3 → US4)

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2)
- **User Story 2 (P2)**: Can start after Foundational (Phase 2)
- **User Story 3 (P3)**: Can start after Foundational (Phase 2)
- **User Story 4 (P4)**: Can start after Foundational (Phase 2)

### Implementation Strategy

#### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently

#### Incremental Delivery

1. Complete Setup + Foundational
2. Add User Story 1 → Test independently
3. Add User Story 2 → Test independently
4. Add User Story 3 → Test independently
5. Add User Story 4 → Test independently
