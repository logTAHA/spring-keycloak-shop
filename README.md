# Spring Boot & Keycloak E-Commerce API

E-Commerce REST API built with Spring Boot 4, PostgreSQL, and Keycloak (OAuth2 / OIDC).

---

## Table of Contents

- [Overview](#overview)
- [Installation](#installation)
- [Pre-configured Users](#pre-configured-users)
- [API Endpoints](#api-endpoints)
- [API Testing](#api-testing)
- [Environment Variables](#environment-variables)
- [Managing Services](#managing-services)

---

## Overview

E-Commerce backend API with role-based access control (RBAC), automated database initialization, and pre-configured IAM realm import.

### Tech Stack

- **Backend**: Spring Boot 4.1.1 (Java 21), Spring Data JPA
- **Security**: Spring Security 6 (OAuth2 Resource Server / JWT)
- **IAM**: Keycloak 26.7.2
- **Database**: PostgreSQL 17
- **DevOps**: Docker, Docker Compose & GitHub Actions (Docker Hub CI/CD)

---

## Installation

### Prerequisites

- Docker & Docker Compose

### 1. Clone Repository

```bash
git clone https://github.com/logTAHA/spring-keycloak-shop.git
cd spring-keycloak-shop
```

### 2. Configure Environment

Copy `.env.example` to `.env` and adjust credentials as needed:

```bash
cp .env.example .env
```

```env
# PostgreSQL
POSTGRES_USER=shop
POSTGRES_PASSWORD=your_password_here
POSTGRES_DB=shop

# Keycloak Admin
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=your_password_here
```

### 3. Start Services

#### Option A: Use Pre-built Image (Recommended)

Pulls the pre-built image from Docker Hub (`logtaha/spring-keycloak-shop:latest`) published by GitHub Actions:

```bash
docker compose up -d
```

#### Option B: Build Locally from Source

Builds the image locally using the `Dockerfile`:

```bash
docker compose up -d --build
```

### Service URLs

- **API**: `http://localhost:8080`
- **Keycloak**: `http://localhost:8180`
- **PostgreSQL**: `localhost:5432`

---

## Pre-configured Users

The `shop` realm comes pre-configured with the `shop-front` client and the following accounts:

| Username | Password | Role | Access |
| :--- | :--- | :--- | :--- |
| `admin` | `admin` | `ADMIN` | Manage products and view all carts |
| `taha` | `taha` | `CUSTOMER` | Manage own cart and checkout |

### Get JWT Token

You can execute the pre-configured `Login` request in `api-collection/` or use `curl`:

```bash
curl -X POST "http://localhost:8180/realms/shop/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=shop-front" \
  -d "username=taha" \
  -d "password=taha"
```

---

## API Endpoints

Application APIs run on port **`8080`** (`http://localhost:8080`). Authentication is handled by Keycloak on port **`8180`** (`http://localhost:8180`), returning JWT access and refresh tokens.

### Auth (Keycloak - Port 8180)

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/realms/shop/protocol/openid-connect/token` | `POST` | Login or refresh JWT token |

### Products (Port 8080)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/products` | Public | List products (paginated) |
| `GET` | `/api/products/{identifier}` | Public | Get product by ID or slug |
| `POST` | `/api/products` | `ADMIN` | Create / update product |

### Cart (Port 8080)

| Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/carts` | User | Create cart |
| `GET` | `/api/carts/{id}` | User / Admin | Get cart by ID |
| `POST` | `/api/carts/items` | User | Add / update items |
| `POST` | `/api/carts/checkout` | User | Checkout cart |

---

## API Testing

A Bruno collection is available in the `api-collection/` directory:

- `Login.yml` / `Refresh.yml`: Auth tokens
- `Get Products.yml` / `Get Product.yml` / `Upsert Product.yml`: Product requests
- `Create Cart.yml` / `Upsert Cart Item.yml` / `Get Cart By Id.yml` / `Checkout Cart.yml`: Cart requests

---

## Environment Variables

| Variable | Default | Description |
| :--- | :--- | :--- |
| `POSTGRES_USER` | `shop` | PostgreSQL username |
| `POSTGRES_PASSWORD` | `your_password_here` | PostgreSQL password |
| `POSTGRES_DB` | `shop` | Application database name |
| `KEYCLOAK_ADMIN_USERNAME` | `admin` | Keycloak admin user |
| `KEYCLOAK_ADMIN_PASSWORD` | `your_password_here` | Keycloak admin password |

---

## Managing Services

```bash
# View logs
docker compose logs -f

# Stop services
docker compose down

# Stop services and remove volumes (resets database)
docker compose down -v
```
