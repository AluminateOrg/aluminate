# Chat Design (Aluminate)

## Storage
- MongoDB collection `messages` with fields:
  - _id, orgId, groupId ("UNIVERSAL" for universal chat), senderId, senderRole, content, createdAt, editedAt, deletedAt, meta, readBy.
- Index: { orgId: 1, groupId: 1, createdAt: -1 }.

## Authorization
- JWT validated server-side (HS256). Required for REST and WS.
- Universal chat: only admins may send; readable by all org members.
- Group chat: only members may read/send.

## Membership
- Authoritative: Postgres.
- MembershipClient (JDBC) queries:
  - isMember(org_id, group_id, user_id)
  - isAdmin(org_id, user_id)
  - groupsForUser(org_id, user_id)
- Cached in Redis: key `org:{orgId}:user:{userId}:groups` TTL 60s.

## WebSocket
- STOMP endpoint `/ws`.
- Topics: `/topic/org.{orgId}.group.{groupId}`.
- Send mapping: `/app/org/{orgId}/group/{groupId}/send`.
- Auth via STOMP CONNECT header `Authorization: Bearer <jwt>`.

## Pub/Sub
- Redis channel: `chat:org:{orgId}:events`.
- Publisher emits saved messages; subscriber forwards to local WS clients.

## Rate Limiting
- Redis counters:
  - `rate:{orgId}:{userId}:sec` (5/s)
  - `rate:{orgId}:{userId}:min` (100/min)

## Monitoring (future)
- Micrometer timers for Mongo writes, pub/sub latency, WS connections.
- Log failed auth and rate-limit hits.
