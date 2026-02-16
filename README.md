# 🔗 URL Shortener

> A production-grade, scalable URL shortening service built with **Java 17 + Spring Boot**, backed by **PostgreSQL**, **Redis**, and deployable to **AWS ECS Fargate** via **Terraform**.

[![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub_Actions-blue?logo=github-actions)](/.github/workflows/ci-cd.yml)
[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-green?logo=spring)](https://spring.io/projects/spring-boot)
[![AWS](https://img.shields.io/badge/AWS-ECS_Fargate-yellow?logo=amazon-aws)](https://aws.amazon.com/ecs/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green)](LICENSE)

---

## ✨ Features

| Feature | Details |
|---|---|
| **URL Shortening** | Base62-encoded 7-char codes with collision-safe generation |
| **Custom Aliases** | User-defined short codes (e.g. `/my-link`) |
| **URL Expiration** | Per-URL TTL with automatic scheduled cleanup |
| **Rate Limiting** | Per-IP token-bucket limiting (Bucket4j) — 20 req/min shorten, 100 req/min redirect |
| **Caching** | Redis-backed cache on the redirect hot path (sub-millisecond lookups) |
| **Click Analytics** | Total clicks, clicks by time window, clicks by country, daily trends |
| **Observability** | Prometheus metrics + Spring Actuator health endpoints |
| **Swagger UI** | Interactive API docs at `/swagger-ui.html` |
| **AWS-Ready** | Terraform for VPC, RDS, ElastiCache, ECS Fargate, ALB, ECR |

---

## 🏗️ Architecture

```
Client
  │
  ▼
Application Load Balancer (AWS ALB)
  │
  ▼
ECS Fargate (2 tasks, auto-scaled)
  │         │
  ▼         ▼
RDS       ElastiCache
PostgreSQL   Redis
(persistent) (hot cache)
```

**Request flow for redirect (hot path):**
1. `GET /{shortCode}` hits the controller
2. Redis cache checked → cache HIT returns original URL instantly
3. Cache MISS → PostgreSQL lookup → result cached in Redis (TTL 5 min)
4. Click event recorded asynchronously
5. `302` redirect returned to client

---

## 📁 Project Structure

```
url-shortener/
├── src/
│   ├── main/
│   │   ├── java/com/urlshortener/
│   │   │   ├── UrlShortenerApplication.java
│   │   │   ├── controller/
│   │   │   │   └── UrlController.java       # REST endpoints
│   │   │   ├── service/
│   │   │   │   ├── UrlService.java          # Core business logic
│   │   │   │   └── RateLimiterService.java  # Token-bucket rate limiting
│   │   │   ├── repository/
│   │   │   │   ├── UrlRepository.java       # JPA queries
│   │   │   │   └── ClickEventRepository.java
│   │   │   ├── model/
│   │   │   │   ├── Url.java                 # URL entity
│   │   │   │   └── ClickEvent.java          # Analytics entity
│   │   │   ├── dto/
│   │   │   │   └── UrlDtos.java             # Request/Response DTOs
│   │   │   ├── config/
│   │   │   │   ├── CacheConfig.java         # Redis cache setup
│   │   │   │   ├── LocalCacheConfig.java    # In-memory cache (local/test)
│   │   │   │   └── OpenApiConfig.java       # Swagger/OpenAPI config
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ShortCodeNotFoundException.java
│   │   │   │   ├── UrlExpiredException.java
│   │   │   │   └── ShortCodeConflictException.java
│   │   │   └── util/
│   │   │       └── UrlEncoder.java          # Base62 encoding
│   │   └── resources/
│   │       ├── application.yml              # Default (PostgreSQL + Redis)
│   │       ├── application-local.yml        # Local dev (H2, no Redis)
│   │       └── application-test.yml         # Tests (H2, no Redis)
│   └── test/
│       └── java/com/urlshortener/
│           └── UrlShortenerIntegrationTest.java
├── terraform/
│   └── main.tf                              # Full AWS infrastructure
├── .github/
│   └── workflows/
│       └── ci-cd.yml                        # GitHub Actions CI/CD
├── docker-compose.yml                       # Local full-stack
├── Dockerfile                               # Multi-stage build
├── pom.xml
└── README.md
```

---

## 🚀 How to Run

### Option 1 — Quickest: Local Profile (No Docker)

> Uses H2 in-memory database. No Redis, no Docker needed.

```bash
# 1. Clone the repo
git clone https://github.com/your-username/url-shortener.git
cd url-shortener

# 2. Run with the local profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

App starts at **http://localhost:8080**
Swagger UI: **http://localhost:8080/swagger-ui.html**
H2 Console: **http://localhost:8080/h2-console** (JDBC URL: `jdbc:h2:mem:urlshortener`)

---

### Option 2 — Full Stack: Docker Compose (PostgreSQL + Redis)

> Spins up the app, PostgreSQL 16, and Redis 7 in containers.

**Prerequisites:** Docker Desktop installed

```bash
# 1. Clone the repo
git clone https://github.com/your-username/url-shortener.git
cd url-shortener

# 2. Start everything
docker-compose up --build

# 3. To stop
docker-compose down

# 4. To stop and remove all data
docker-compose down -v
```

App starts at **http://localhost:8080**
Swagger UI: **http://localhost:8080/swagger-ui.html**

---

### Option 3 — Deploy to AWS (Terraform + ECS Fargate)

> Deploys a production-ready setup on AWS.

**Prerequisites:** AWS CLI configured, Terraform ≥ 1.6, Docker

```bash
# 1. Build and push the Docker image to ECR
cd url-shortener
aws ecr create-repository --repository-name url-shortener --region us-east-1

ECR_URI=$(aws ecr describe-repositories \
  --repository-names url-shortener \
  --query 'repositories[0].repositoryUri' --output text)

aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin $ECR_URI

docker build -t $ECR_URI:latest .
docker push $ECR_URI:latest

# 2. Apply Terraform
cd terraform
terraform init
terraform apply \
  -var="container_image=$ECR_URI:latest" \
  -var="db_password=YOUR_SECURE_PASSWORD"

# 3. Get the ALB DNS name
terraform output alb_dns_name
```

---

## 📡 API Reference

### Base URL
```
http://localhost:8080
```

### Endpoints

#### `POST /api/v1/shorten` — Create a short URL
```bash
curl -X POST http://localhost:8080/api/v1/shorten \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://www.example.com/very/long/path?query=param",
    "customAlias": "my-link",
    "expiresAt": "2026-12-31T23:59:59",
    "title": "My Short Link"
  }'
```
**Response (201):**
```json
{
  "shortCode": "my-link",
  "shortUrl": "http://localhost:8080/my-link",
  "originalUrl": "https://www.example.com/very/long/path?query=param",
  "createdAt": "2026-02-12T10:00:00",
  "expiresAt": "2026-12-31T23:59:59",
  "title": "My Short Link"
}
```

---

#### `GET /{shortCode}` — Redirect
```bash
curl -v http://localhost:8080/my-link
# → HTTP 302 Location: https://www.example.com/very/long/path?query=param
```

---

#### `GET /api/v1/urls/{shortCode}/stats` — URL Stats
```bash
curl http://localhost:8080/api/v1/urls/my-link/stats
```
```json
{
  "shortCode": "my-link",
  "shortUrl": "http://localhost:8080/my-link",
  "originalUrl": "https://www.example.com/...",
  "totalClicks": 42,
  "createdAt": "2026-02-12T10:00:00",
  "expiresAt": "2026-12-31T23:59:59",
  "active": true
}
```

---

#### `GET /api/v1/urls/{shortCode}/analytics` — Full Analytics
```bash
curl http://localhost:8080/api/v1/urls/my-link/analytics
```
```json
{
  "shortCode": "my-link",
  "totalClicks": 42,
  "clicksLast24Hours": 5,
  "clicksLast7Days": 18,
  "clicksLast30Days": 42,
  "clicksByCountry": { "US": 30, "UK": 8, "Unknown": 4 },
  "dailyStats": [
    { "date": "2026-02-12", "clicks": 5 },
    { "date": "2026-02-11", "clicks": 7 }
  ]
}
```

---

#### `DELETE /api/v1/urls/{shortCode}` — Deactivate URL
```bash
curl -X DELETE http://localhost:8080/api/v1/urls/my-link
# → HTTP 204 No Content
```

---

### HTTP Status Codes

| Code | Meaning |
|------|---------|
| 201 | Short URL created |
| 302 | Redirect successful |
| 400 | Validation error (invalid URL, bad alias) |
| 404 | Short code not found |
| 409 | Custom alias already taken |
| 410 | URL has expired or been deactivated |
| 429 | Rate limit exceeded |

---

## 🧪 Running Tests

```bash
# Run all tests
./mvnw test

# Run with verbose output
./mvnw test -Dspring.profiles.active=test

# Generate coverage report
./mvnw test jacoco:report
# Report available at: target/site/jacoco/index.html
```

---

## 📊 Observability

| Endpoint | Description |
|---|---|
| `/actuator/health` | Service health check |
| `/actuator/metrics` | All metrics |
| `/actuator/prometheus` | Prometheus scrape endpoint |
| `/swagger-ui.html` | Interactive API docs |
| `/v3/api-docs` | OpenAPI JSON spec |

**Key Metrics:**
- `urls.created` — counter of URLs shortened
- `urls.clicks` — counter of redirects (tagged by `shortCode`)
- `urls.expired` — counter of expired URLs cleaned up

---

## ⚙️ Configuration Reference

| Property | Env Var | Default | Description |
|---|---|---|---|
| `app.base-url` | `BASE_URL` | `http://localhost:8080` | Base URL for short links |
| `app.default-expiry-days` | `DEFAULT_EXPIRY_DAYS` | `30` | Default link TTL in days |
| `app.cleanup-interval-ms` | `CLEANUP_INTERVAL_MS` | `3600000` | Expiry cleanup interval |
| `app.cache.ttl-seconds` | `CACHE_TTL` | `300` | Redis cache TTL |
| `spring.datasource.url` | `DB_URL` | `jdbc:postgresql://...` | Database URL |
| `spring.data.redis.host` | `REDIS_HOST` | `localhost` | Redis host |

---

## 🔑 AWS Secrets Setup (GitHub Actions)

Add these secrets in your GitHub repository (`Settings → Secrets`):

| Secret | Description |
|---|---|
| `AWS_ACCESS_KEY_ID` | AWS IAM access key |
| `AWS_SECRET_ACCESS_KEY` | AWS IAM secret key |

---

## 🧠 Design Decisions

**Why Base62?** Gives 62^7 = ~3.5 trillion unique codes from 7 characters — more than enough for any scale, URL-safe, case-sensitive.

**Why Redis cache on redirect?** The redirect path is the most latency-critical. Caching the `shortCode → originalUrl` mapping in Redis keeps redirects at sub-millisecond latency without hitting the DB on every click.

**Why token-bucket rate limiting?** Token buckets allow short bursts while enforcing a sustained rate limit — better UX than hard per-second limits. In-process Bucket4j is used for simplicity; for multi-instance deployments, store bucket state in Redis.

**Why scheduled expiry cleanup?** Rather than checking expiry on every read (adds latency), a background job deactivates expired URLs periodically. Active checks still happen on redirect to ensure consistency.

---

## 📄 License

MIT License — see [LICENSE](LICENSE)
