-- file: workspace.sql
-- This file contains test data for a single workspace and its membership.
-- Now insert the workspace
INSERT INTO workspaces (id, name, description, owner_id, created_by, created_at, updated_by, updated_at)
VALUES ('a0654720-35dc-49d0-b508-1f7df5d915f1', 'Test: My First Workspace', 'Super workspace',
        'efc4b2b8-08be-4020-93d5-f795762bf5c9', 'system', '2024-06-02 11:00:08.251',
        'system', '2024-06-02 11:00:08.281');

-- Insert the workspace member for the owner
INSERT INTO workspace_members (workspace_id, user_id, role, created_at, updated_at)
VALUES ('a0654720-35dc-49d0-b508-1f7df5d915f1', 'efc4b2b8-08be-4020-93d5-f795762bf5c9', 'OWNER', '2024-06-02 11:00:08.251', '2024-06-02 11:00:08.251');
