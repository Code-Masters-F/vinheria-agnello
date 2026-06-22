# API Contract: Produto CRUD

## Base URL
`/api/produtos`

## Endpoints

### 1. Get All Products
- **Method:** `GET`
- **Path:** `/`
- **Response (200 OK):**
```json
[
  {
    "id": 1,
    "nome": "Vinho Tinto",
    "descricao": "Delicioso vinho",
    "preco": 45.99,
    "quantidade": 10
  }
]
```

### 2. Get Product by ID
- **Method:** `GET`
- **Path:** `/{id}`
- **Response (200 OK):**
```json
{
  "id": 1,
  "nome": "Vinho Tinto",
  "descricao": "Delicioso vinho",
  "preco": 45.99,
  "quantidade": 10
}
```
- **Response (404 Not Found):** If product doesn't exist.

### 3. Create Product
- **Method:** `POST`
- **Path:** `/`
- **Request Body:**
```json
{
  "nome": "Novo Vinho",
  "descricao": "Vinho recém chegado",
  "preco": 50.00,
  "quantidade": 5
}
```
- **Response (201 Created):** Returns the created product with its generated `id`.

### 4. Update Product
- **Method:** `PUT`
- **Path:** `/{id}`
- **Request Body:**
```json
{
  "id": 1,
  "nome": "Vinho Tinto Atualizado",
  "descricao": "Nova descrição",
  "preco": 49.99,
  "quantidade": 8
}
```
- **Response (204 No Content):** On successful update.
- **Response (400 Bad Request):** If the ID in the URL does not match the ID in the body.
- **Response (404 Not Found):** If the product doesn't exist.

### 5. Delete Product
- **Method:** `DELETE`
- **Path:** `/{id}`
- **Response (204 No Content):** On successful deletion.
- **Response (404 Not Found):** If the product doesn't exist.
