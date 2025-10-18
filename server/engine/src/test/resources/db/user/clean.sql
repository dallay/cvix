-- Finally delete the test users
DELETE FROM users
WHERE id IN (
             'efc4b2b8-08be-4020-93d5-f795762bf5c9',
             'b2864d62-003e-4464-a6d7-04d3567fb4ee'
  );
