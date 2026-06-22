# Data Model: Produto

## Entity: Produto

Matches the existing `produto` table in the shared MySQL database.

### Fields
| Field Name | Type | Constraints | Description |
|------------|------|-------------|-------------|
| `id` | `int` | Primary Key, Auto Increment | The unique identifier |
| `nome` | `string` | Max length 100, Not Null | The name of the product |
| `descricao` | `string` | Max length 255 | Description of the product |
| `preco` | `decimal(10,2)` | Not Null | Price of the product |
| `quantidade` | `int` | Not Null, Default 0 | Available stock quantity |

### Validation Rules
- `nome` must not be empty and must not exceed 100 characters.
- `preco` must be greater than or equal to 0.
- `quantidade` must be greater than or equal to 0.

### Database Mapping
The C# API will use Dapper to map queries directly to the `produto` table.
- **Table Name:** `produto`
- Dapper will be configured to automatically map column names to C# properties (e.g., `id` -> `Id`, `nome` -> `Nome`).
