-- file: resumes.sql
-- Seed three distinct resumes for the default test user/workspace
-- User: efc4b2b8-08be-4020-93d5-f795762bf5c9 (from users.sql)
-- Workspace: a0654720-35dc-49d0-b508-1f7df5d915f1 (from workspace.sql)

INSERT INTO resumes (id, user_id, workspace_id, title, data, created_by, created_at, updated_by,
                     updated_at)
VALUES ('11111111-1111-1111-1111-111111111111', 'efc4b2b8-08be-4020-93d5-f795762bf5c9',
        'a0654720-35dc-49d0-b508-1f7df5d915f1',
        'Minimal CV', '{
    "basics": {
      "name": "Red Minimal",
      "email": "red@example.com",
      "profiles": []
    },
    "work": [
      {
        "name": "ACME Corp",
        "position": "Engineer",
        "startDate": "2020-01-01"
      }
    ],
    "volunteer": [],
    "education": [],
    "awards": [],
    "certificates": [],
    "publications": [],
    "skills": [],
    "languages": [],
    "interests": [],
    "references": [],
    "projects": []
  }'::jsonb, 'system', '2024-06-02 11:00:09.001', 'system', '2024-06-02 11:05:09.001'),
       ('22222222-2222-2222-2222-222222222222', 'efc4b2b8-08be-4020-93d5-f795762bf5c9',
        'a0654720-35dc-49d0-b508-1f7df5d915f1',
        'Mi first CV', '{
         "basics": {
           "name": "Jane Minimal",
           "email": "jane@example.com",
           "profiles": []
         },
         "work": [
           {
             "name": "Tech Inc",
             "position": "Developer",
             "startDate": "2021-01-01"
           }
         ],
         "volunteer": [],
         "education": [],
         "awards": [],
         "certificates": [],
         "publications": [],
         "skills": [],
         "languages": [],
         "interests": [],
         "references": [],
         "projects": []
       }'::jsonb, 'system', '2024-06-02 11:00:09.002', 'system', '2024-06-02 11:04:09.002'),
       ('33333333-3333-3333-3333-333333333333', 'efc4b2b8-08be-4020-93d5-f795762bf5c9',
        'a0654720-35dc-49d0-b508-1f7df5d915f1',
        'YAP CV', '{
         "basics": {
           "name": "John Doe",
           "email": "john@example.com",
           "profiles": []
         },
         "work": [
           {
             "name": "StartupCo",
             "position": "Lead Dev",
             "startDate": "2022-01-01"
           }
         ],
         "volunteer": [],
         "education": [],
         "awards": [],
         "certificates": [],
         "publications": [],
         "skills": [],
         "languages": [],
         "interests": [],
         "references": [],
         "projects": []
       }'::jsonb, 'system', '2024-06-02 11:00:09.003', 'system', '2024-06-02 11:03:09.003');
