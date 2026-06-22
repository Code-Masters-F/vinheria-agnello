# Feature Specification: C# API for Produto CRUD

## 1. Feature Description
This feature introduces a new Backend-for-Frontend (BFF) API built in C# to serve the Mobile Android application. It exposes a set of RESTful endpoints to perform CRUD (Create, Read, Update, Delete) operations on the `Produto` entity. The API will connect to the existing shared database used by the current Java/JSP web application.

## 2. Business Value
By building a dedicated, lightweight C# API for the mobile application, the mobile clients can manage their local database (Room) sync state while relying on the backend for canonical truth. This hybrid architecture fits the academic project's goals of demonstrating multiple backend ecosystems coexisting.

## 3. User Scenarios
- **Scenario 1:** Mobile user connects to the internet and views the catalog of products. The Android app calls the `GET /api/produtos` endpoint to retrieve all available products and updates its local Room database.
- **Scenario 2:** Mobile user creates a new product through the app interface. The app sends a `POST /api/produtos` request with the product details. The API validates and inserts the product into the shared SQL database.
- **Scenario 3:** Mobile user updates an existing product. The app sends a `PUT /api/produtos/{id}` request, and the API updates the canonical database record.
- **Scenario 4:** Mobile user deletes a product. The app sends a `DELETE /api/produtos/{id}` request, and the API removes the item from the database.

## 4. Functional Requirements
- **FR-1:** The system MUST provide a REST API endpoint to retrieve all products.
- **FR-2:** The system MUST provide a REST API endpoint to retrieve a single product by its ID.
- **FR-3:** The system MUST provide a REST API endpoint to create a new product.
- **FR-4:** The system MUST provide a REST API endpoint to update an existing product.
- **FR-5:** The system MUST provide a REST API endpoint to delete an existing product.
- **FR-6:** The system MUST return structured JSON responses for all endpoints.

## 5. Non-Functional Requirements & Assumptions
- **Assumption 1:** The C# API will NOT implement authentication or authorization. All endpoints will be publicly accessible.
- **Assumption 2:** The C# API will NOT manage database migrations or schema creation. The tables are assumed to already exist and be managed by the main SQL scripts.
- **Assumption 3:** The mobile app handles its own offline capability (Room) and will only invoke these endpoints when connected to the internet. 

## 6. Success Criteria
- The mobile application can successfully create, read, update, and delete products by communicating solely with the C# API.
- The C# API runs successfully and connects to the existing database.
- The existing Java/JSP application remains fully functional and unaffected by the introduction of the C# API.

## 7. Key Entities
- **Produto** (Product):
  - `id`: Unique identifier
  - `nome`: Name of the product
  - `descricao`: Description
  - `preco`: Price
  - `quantidade`: Available stock quantity
