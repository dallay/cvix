-- file: users.sql
-- USERS (use ON CONFLICT to make script idempotent)
INSERT INTO users (id, email, full_name, created_at)
VALUES ('efc4b2b8-08be-4020-93d5-f795762bf5c9', 'test1@example.com', 'Test User1', now()),
       ('b2864d62-003e-4464-a6d7-04d3567fb4ee', 'test2@example.com', 'Test User2', now()),
       ('c4af4e2f-b432-4c3b-8405-cca86cd5b97b', 'user@profiletailors.com', 'Basic User', now()),
       ('d5af5e3f-c543-5d4c-9516-ddb97ce6c98c', 'john.doe@profiletailors.com', 'John Doe', now())
ON CONFLICT (id) DO NOTHING;

-- USER_ROLES (use ON CONFLICT to make script idempotent)
INSERT INTO user_authority (user_id, authority_name)
VALUES ('efc4b2b8-08be-4020-93d5-f795762bf5c9', 'ROLE_ADMIN'),
       ('efc4b2b8-08be-4020-93d5-f795762bf5c9', 'ROLE_USER'),
       ('b2864d62-003e-4464-a6d7-04d3567fb4ee', 'ROLE_USER'),
       ('c4af4e2f-b432-4c3b-8405-cca86cd5b97b', 'ROLE_USER'),
       ('d5af5e3f-c543-5d4c-9516-ddb97ce6c98c', 'ROLE_USER')
ON CONFLICT (user_id, authority_name) DO NOTHING;
