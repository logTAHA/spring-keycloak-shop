# Spring Boot & Keycloak E-Commerce API

A full-featured e-commerce backend API built with Spring Boot 4, PostgreSQL, and Keycloak for identity and access management (OAuth2 / OIDC).

---

## Table of Contents

- [Overview](#overview)
- [Architecture and Tech Stack](#architecture-and-tech-stack)
- [Installation and Quick Start](#installation-and-quick-start)
  - [Prerequisites](#prerequisites)
  - [Step 1: Clone the Repository](#step-1-clone-the-repository)
  - [Step 2: Configure Environment Variables](#step-2-configure-environment-variables)
  - [Step 3: Start Services with Docker Compose](#step-3-start-services-with-docker-compose)
- [Pre-configured Users and Authentication](#pre-configured-users-and-authentication)
- [API Overview](#api-overview)
  - [Authentication Endpoint](#authentication-endpoint)
  - [Products Endpoints](#products-endpoints)
  - [Cart Endpoints](#cart-endpoints)
- [API Testing with Bruno Collection](#api-testing-with-bruno-collection)
- [Environment Variables Reference](#environment-variables-reference)
- [Managing the Services](#managing-the-services)

---

## Overview

This project provides an e-commerce API with role-based access control. Authentication and authorization are handled via Keycloak as an OAuth2 Resource Server emitting JWT tokens with realm roles (`ADMIN`, `CUSTOMER`).

The repository includes:
- Automated CI/CD workflow via GitHub Actions that builds and publishes production-ready Docker images to Docker Hub.
- Automated database initialization scripts for multi-database PostgreSQL setup.
- Automated Keycloak realm configuration import (`shop` realm) on container startup.
- Sample dataset preloaded on first application launch.

---

## Architecture and Tech Stack

- **Framework**: Spring Boot 4.1.1 (Java 21)
- **Security**: Spring Security 6 (OAuth2 Resource Server / JWT)
- **Identity Provider**: Keycloak 26.7.2
- **Database**: PostgreSQL 17 (Alpine)
- **Persistence**: Spring Data JPA / Hibernate
- **Containerization**: Docker & Docker Compose
- **CI/CD**: GitHub Actions (Docker Hub automated builds)

---

## Installation and Quick Start

### Prerequisites

Ensure you have the following installed on your system:
- Docker (version 24.0 or higher)
- Docker Compose (Compose V2 recommended)

### Step 1: Clone the Repository

```bash
git clone https://github.com/logTAHA/spring-keycloak-shop.git
cd spring-keycloak-shop
```

### Step 2: Configure Environment Variables

The repository includes a template file `.env.example`. Copy it to create your local `.env` configuration file:

```bash
cp .env.example .env
```

Open `.env` in your text editor and update the default passwords and credentials as required:

```env
# PostgreSQL Configuration
POSTGRES_USER=shop
POSTGRES_PASSWORD=your_secure_password_here
POSTGRES_DB=shop

# Keycloak Master Admin Credentials
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=your_secure_password_here
```

### Step 3: Start Services with Docker Compose

Run the following command to download and start all containers in detached mode:

```bash
docker compose up -d
```

> **Note on Docker Images**:
> The application image is automatically built and published to Docker Hub (`logtaha/spring-keycloak-shop:latest`) on every push to the `main` branch via GitHub Actions CI/CD workflow.
> Docker Compose pulls the pre-built image directly from Docker Hub. You do not need to build the image locally or have Java / Maven installed on your host machine.

Verify that all containers are healthy and running:

```bash
docker compose ps
```

The services will be accessible at the following URLs:
- **Spring Boot API**: `http://localhost:8080`
- **Keycloak Admin Console**: `http://localhost:8180`
- **PostgreSQL Database**: `localhost:5432`

---

## Pre-configured Users and Authentication

The Keycloak realm (`shop`) is automatically imported on startup with the client `shop-front` and the following pre-configured user accounts:

| Username | Password | Role | Description |
| :--- | :--- | :--- | :--- |
| `admin` | `admin` | `ADMIN` | Full access to manage products, view any cart, and manage the system |
| `taha` | `taha` | `CUSTOMER` | Customer access to manage own cart, add items, and checkout |

To obtain a JWT access token, send a request to Keycloak's token endpoint:

```bash
curl -X POST "http://localhost:8180/realms/shop/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=shop-front" \
  -d "username=taha" \
  -d "password=taha"
```

Use the returned `access_token` in the `Authorization` header for protected endpoints:
```text
Authorization: Bearer <your_access_token>
```

---

## API Overview

### Authentication Endpoint

- **POST** `http://localhost:8180/realms/shop/protocol/openid-connect/token`: Exchange credentials for a JWT access and refresh token.

### Products Endpoints

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/products` | Public | List products with pagination, filtering, and sorting |
| `GET` | `/api/products/{identifier}` | Public | Get product details by ID or slug (extended details for Admin) |
| `POST` | `/api/products` | `ADMIN` | Create or update a product |

### Cart Endpoints

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/carts` | Authenticated | Create a new active shopping cart for the user |
| `GET` | `/api/carts/{id}` | Authenticated | Get cart by ID (Owner or Admin only) |
| `POST` | `/api/carts/items` | Authenticated | Add, update, or remove items in the cart |
| `POST` | `/api/carts/checkout` | Authenticated | Checkout the active cart |

---

## API Testing with Bruno Collection

An API request collection is included in the `api-collection/` folder. You can open and execute these requests using [Bruno](https://www.usebruno.com/) or any compatible API client:

- `Login.yml`: Obtain JWT token using pre-configured users.
- `Refresh.yml`: Refresh expired JWT access token.
- `Get Products.yml`: Retrieve list of products.
- `Get Product.yml`: Retrieve single product details.
- `Upsert Product.yml`: Create/update product (Admin token required).
- `Create Cart.yml`: Create user cart.
- `Upsert Cart Item.yml`: Add/update items inside cart.
- `Get Cart By Id.yml`: Retrieve user cart with line items and totals.
- `Checkout Cart.yml`: Complete cart checkout.

---

## Environment Variables Reference

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `POSTGRES_USER` | `shop` | Username for PostgreSQL databases (`shop` and `keycloak`) |
| `POSTGRES_PASSWORD` | `your_password_here` | Password for PostgreSQL |
| `POSTGRES_DB` | `shop` | Name of the application PostgreSQL database |
| `KEYCLOAK_ADMIN_USERNAME` | `admin` | Keycloak master administrator username |
| `KEYCLOAK_ADMIN_PASSWORD` | `your_password_here` | Keycloak master administrator password |

---

## Managing the Services

### View Container Logs

To stream logs from all running services:

```bash
docker compose logs -f
```

To view logs for a specific service:

```bash
# Application logs
docker compose logs -f app

# Keycloak logs
docker compose logs -f keycloak

# PostgreSQL logs
docker compose logs -f postgres
```

### Stop Containers

To stop and remove containers while preserving persistent volumes (database data):

```bash
docker compose down
```

To stop containers and delete all data volumes (resets database and starts fresh):

```bash
docker compose down -v
```
