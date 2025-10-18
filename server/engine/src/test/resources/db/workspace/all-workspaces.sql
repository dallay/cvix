-- file: all-workspaces.sql
-- This file contains test data for workspaces and their memberships.
-- WORKSPACES
INSERT INTO workspaces (id, name, description, owner_id, created_by, created_at, updated_by, updated_at)
VALUES
  ('a0654720-35dc-49d0-b508-1f7df5d915f1', 'Test: ⚡ Main Workspace', 'Super workspace',
   'efc4b2b8-08be-4020-93d5-f795762bf5c9', 'system', '2024-06-02 11:00:08.251',
   'system', '2024-06-02 11:00:08.281'),
  ('95ded4bb-2946-4dbe-87df-afb701788eb4', 'Test: My First Workspace', 'Super workspace',
   'efc4b2b8-08be-4020-93d5-f795762bf5c9', 'system', now(), 'system', now()),

  ('894812b3-deb9-469f-b988-d8dfa5a1cf52', 'Test: My Second Workspace', 'Super workspace',
   'efc4b2b8-08be-4020-93d5-f795762bf5c9', 'system', now(), 'system', now()),

  ('949a8d91-1f53-4082-a4a9-7760fed234b0', 'Test: My Third Workspace', 'Super workspace',
   'b2864d62-003e-4464-a6d7-04d3567fb4ee', 'system', now(), 'system', now());

-- MEMBERSHIP
INSERT INTO workspace_members (workspace_id, user_id, role, created_at, updated_at)
VALUES
  ('a0654720-35dc-49d0-b508-1f7df5d915f1', 'efc4b2b8-08be-4020-93d5-f795762bf5c9', 'OWNER', '2024-06-02 11:00:08.251', '2024-06-02 11:00:08.251'),
  ('95ded4bb-2946-4dbe-87df-afb701788eb4', 'efc4b2b8-08be-4020-93d5-f795762bf5c9', 'OWNER', now(), now()),
  ('894812b3-deb9-469f-b988-d8dfa5a1cf52', 'efc4b2b8-08be-4020-93d5-f795762bf5c9', 'OWNER', now(), now()),
  ('949a8d91-1f53-4082-a4a9-7760fed234b0', 'b2864d62-003e-4464-a6d7-04d3567fb4ee', 'OWNER', now(), now());
