# Amazon E-Commerce API

Spring Boot REST API for an Amazon-style e-commerce platform with JWT authentication, role-based access control, MySQL persistence, and PDF invoice generation.

## Requirements

- Java 17+
- Maven 3.8+
- MySQL 8.0+ (production only)

## Profiles

The project uses Spring profiles to switch between environments:

| Profile | Database     | Usage      |
| ------- | ------------ | ---------- |
| `dev`   | H2 (in-memory) | Local development (default) |
| `prod`  | MySQL        | Production                 |

### Environment Variables

Sensitive configuration is loaded from a `.env` file at the project root via [spring-dotenv](https://github.com/paulschwarz/spring-dotenv). **The `.env` file is gitignored** — never commit secrets.

1. Copy the template and fill in your values:

```bash
cp .env.template .env
```

2. Available variables:

| Variable          | Default                                              | Description            |
| ----------------- | ---------------------------------------------------- | ---------------------- |
| `MYSQL_USER`      | `root`                                               | MySQL username         |
| `MYSQL_PASSWORD`  | `root`                                               | MySQL password         |
| `JWT_SECRET`      | *(hardcoded fallback)*                               | 256-bit key (Base64)   |
| `JWT_EXPIRATION_MS` | `86400000`                                         | Token lifetime (ms)    |
| `SERVER_PORT`     | `8080`                                               | HTTP server port       |

### Development (default)

No external database required — H2 starts in-memory and is auto-configured.

- H2 Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:amazon_ecommerce`
  - Username: `sa`, Password: *(blank)*

```bash
mvn spring-boot:run
```

### Production

Requires a running MySQL instance with an `amazon_ecommerce` database.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

Credentials can be overridden via environment variables:

```bash
set MYSQL_USER=myuser
set MYSQL_PASSWORD=mypass
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

The API starts at `http://localhost:8080`.

### Swagger UI

Once running, browse the interactive API documentation at:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI spec**: `http://localhost:8080/v3/api-docs`

Swagger UI is publicly accessible (no authentication required) and includes a pre-configured **Authorize** button for the JWT Bearer token.

## Default Admin

On first startup, the `DataInitializer` seeds the database with:

| Username | Password   | Roles              |
| -------- | ---------- | ------------------ |
| `admin`  | `admin123` | ROLE_ADMIN, ROLE_USER |

## Authentication

All endpoints except `/api/auth/**` require a JWT Bearer token in the `Authorization` header:

```
Authorization: Bearer <token>
```

### Register a new user

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john",
  "email": "john@example.com",
  "password": "pass123",
  "firstName": "John",
  "lastName": "Doe"
}
```

### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

Response includes a `token` field — use this as the Bearer token.

## API Endpoints

### Products (`/api/products`)

| Method | Path            | Auth     | Description              |
| ------ | --------------- | -------- | ------------------------ |
| GET    | `/api/products` | User     | List all products        |
| GET    | `/api/products/{id}` | User | Get product by ID       |
| POST   | `/api/products` | Admin    | Create product           |
| PUT    | `/api/products/{id}` | Admin | Update product         |
| DELETE | `/api/products/{id}` | Admin | Delete product         |

```json
// POST / PUT request body
{
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse",
  "price": 29.99,
  "quantity": 100,
  "imageUrls": ["https://example.com/mouse.jpg"]
}
```

### Cart (`/api/cart`) — USER role only

| Method | Path           | Description              |
| ------ | -------------- | ------------------------ |
| GET    | `/api/cart`    | View cart items          |
| POST   | `/api/cart`    | Add item to cart         |
| PUT    | `/api/cart/{id}` | Update item quantity   |
| DELETE | `/api/cart/{id}` | Remove item from cart |

```json
// POST / PUT request body
{
  "productId": 1,
  "quantity": 2
}
```

### Orders (`/api/orders`)

| Method | Path                  | Auth | Description                  |
| ------ | --------------------- | ---- | ---------------------------- |
| POST   | `/api/orders`         | User | Place order from cart        |
| GET    | `/api/orders`         | User | List own orders              |
| GET    | `/api/orders/{id}`    | User | Get order details            |
| GET    | `/api/orders/{id}/pdf` | User | Download order as PDF       |

Optional query parameter for POST: `?shippingAddress=123 Main St`

### Admin (`/api/admin`) — ADMIN role only

| Method | Path               | Description              |
| ------ | ------------------ | ------------------------ |
| GET    | `/api/admin/orders` | List all customer orders |

## Validation

Request validation errors return `400 Bad Request` with field-level details:

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "price": "Price must be greater than 0",
    "name": "Product name is required"
  },
  "timestamp": "2026-07-26T12:00:00"
}
```

## Error Responses

| Status | Description                          |
| ------ | ------------------------------------ |
| 400    | Bad request / validation error       |
| 401    | Invalid or missing JWT               |
| 404    | Resource not found                   |
| 500    | Internal server error                |

## Tech Stack

- **Spring Boot 3.2** — Web, Security, Data JPA, Validation
- **MySQL** — Production database
- **H2** — Development in-memory database
- **JWT (jjwt 0.12.3)** — Stateless authentication
- **iText 7** — PDF invoice generation
- **Springdoc OpenAPI 2.3** — Swagger UI + OpenAPI spec
- **BCrypt** — Password hashing
