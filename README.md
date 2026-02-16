# url-shortener
A scalable URL shortening service with custom aliases, link expiration, rate limiting, and click analytics.

## What It Does

Paste a long URL and get back a short 7-character code you can share anywhere. The service tracks every click, supports custom aliases, and automatically expires links after 30 days. Rate limiting prevents abuse, and Redis caching keeps redirects fast under high load.

## Architecture

```
Client
  │
  ▼
REST API (Spring Boot)          ← Shorten, redirect, analytics endpoints
  │
  ├──► Redis Cache              ← Short code → URL lookup (5 min TTL)
  │
  ├──► PostgreSQL               ← Persistent URL and click event storage
  │
  └──► Scheduled Job            ← Hourly expiry cleanup
```

**Redirect flow:**
1. `GET /{shortCode}` hits the controller
2. Redis checked first — cache hit returns URL instantly
3. Cache miss → PostgreSQL lookup → result written to Redis
4. Click recorded, `302` returned to client

## Tech Stack

| Component | Technology |
|-----------|------------|
| API | Java 17, Spring Boot 3.2 |
| Database | PostgreSQL (H2 for local dev) |
| Cache | Redis (in-memory for local dev) |
| Rate Limiting | Bucket4j — token bucket per IP |
| Infrastructure | AWS ECS Fargate, RDS, ElastiCache, ALB via Terraform |
| API Docs | Swagger UI at `/swagger-ui.html` |

## Getting Started

**Requirements:** Java 17+, Maven

```bash
git clone https://github.com/nishitha1312/url-shortener.git
cd url-shortener
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Runs at `http://localhost:8080` using H2 in-memory database — no Docker or Redis needed.

Swagger UI: `http://localhost:8080/swagger-ui.html`
H2 Console: `http://localhost:8080/h2-console`

## Running with Docker

Starts the app with PostgreSQL and Redis:

```bash
docker-compose up --build
```

## API

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/shorten` | Create a short URL |
| `GET` | `/{shortCode}` | Redirect to original URL |
| `GET` | `/api/v1/urls/{shortCode}/stats` | Click count and link info |
| `GET` | `/api/v1/urls/{shortCode}/analytics` | Detailed analytics |
| `DELETE` | `/api/v1/urls/{shortCode}` | Deactivate a link |

**Shorten a URL:**
```bash
curl -X POST http://localhost:8080/api/v1/shorten \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://github.com", "customAlias": "gh"}'
```

```json
{
  "shortCode": "gh",
  "shortUrl": "http://localhost:8080/gh",
  "originalUrl": "https://github.com",
  "createdAt": "2026-02-15T22:23:42",
  "expiresAt": "2026-03-17T22:23:42"
}
```

## AWS Deployment

Infrastructure is defined in `terraform/main.tf` — provisions VPC, RDS, ElastiCache, ECS Fargate, and an Application Load Balancer.

```bash
cd terraform
terraform init
terraform apply \
  -var="container_image=YOUR_ECR_IMAGE" \
  -var="db_password=YOUR_PASSWORD"
```

## Design Notes

**Base62 encoding** — 7 characters gives 62^7 ≈ 3.5 trillion unique codes. No external ID service needed, collision-safe under concurrent load using an atomic counter.

**Redis on the redirect path** — the most latency-sensitive operation never touches the database on a cache hit, keeping p99 redirect latency in single-digit milliseconds.

**Token bucket rate limiting** — each IP gets its own bucket (20 req/min to shorten, 100 req/min to redirect). Allows short bursts without a hard per-second wall.

**Scheduled expiry** — a background job deactivates expired links every hour instead of checking TTL on every read, keeping the redirect path as lean as possible.
