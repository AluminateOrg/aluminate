# Aluminate Chat Subsystem (Backend)

This document explains how to run and use the chat subsystem implemented inside the `aluminate-organization-backend`.

## Overview

- MongoDB stores chat messages (per org).
- Redis used for:
  - short TTL membership caching
  - pub/sub fan-out across instances
  - rate-limiting counters
- WebSocket (STOMP over `/ws`) for real-time delivery.
- REST endpoints under `/api/v1`.

## Run locally

1. Bring up Mongo and Redis (compose override):
   docker compose -f docker-compose.yml -f docker-compose.chat.yml up -d

2. Configure environment (if using compose variables):
   - MONGO_URI=mongodb://mongo_root:mongo_password@localhost:27017/aluminate_chat?authSource=admin
   - REDIS_URI=redis://localhost:6379

3. Start Spring Boot app:
   ./mvnw spring-boot:run

## REST API

- GET /api/v1/orgs/{orgId}/groups/{groupId}/messages?limit=50&cursor=<ISOInstant>
- POST /api/v1/orgs/{orgId}/groups/{groupId}/messages
  - body: {"content":"Hello"}

Auth: Bearer JWT with claims {sub, orgId, roles}.

## WebSocket (STOMP)

- Endpoint: ws://localhost:8098/ws
- CONNECT headers:
  - Authorization: Bearer <jwt>

- Subscribe:
  - /topic/org.{orgId}.group.{groupId}

- Send:
  - destination: /app/org/{orgId}/group/{groupId}/send
  - body: {"content":"Hello"}

## Examples

curl -H "Authorization: Bearer $JWT" \
  "http://localhost:8098/api/v1/orgs/org_123/groups/UNIVERSAL/messages?limit=20"

curl -XPOST -H "Authorization: Bearer $JWT" -H "Content-Type: application/json" \
  -d '{"content":"Hello"}' \
  "http://localhost:8098/api/v1/orgs/org_123/groups/group_456/messages"

## Notes

- Universal chat send restricted to ADMIN.
- Membership checks cached in Redis for 60s.
- Rate limits: 5 msg/sec, 100 msg/min per user (configurable).
