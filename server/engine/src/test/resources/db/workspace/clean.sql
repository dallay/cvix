-- Clean all test data to ensure a clean state for testing.

-- Delete memberships first due to FK constraint
DELETE FROM workspace_members
WHERE workspace_id IN (
  'a0654720-35dc-49d0-b508-1f7df5d915f1',
  '95ded4bb-2946-4dbe-87df-afb701788eb4',
  '894812b3-deb9-469f-b988-d8dfa5a1cf52',
  '949a8d91-1f53-4082-a4a9-7760fed234b0'
);

-- Then delete the workspaces
DELETE FROM workspaces
WHERE id IN (
  'a0654720-35dc-49d0-b508-1f7df5d915f1',
  '95ded4bb-2946-4dbe-87df-afb701788eb4',
  '894812b3-deb9-469f-b988-d8dfa5a1cf52',
  '949a8d91-1f53-4082-a4a9-7760fed234b0'
);
