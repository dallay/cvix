-- Delete user_authority first due to FK constraint
DELETE
FROM user_authority
WHERE user_id IN (
             'efc4b2b8-08be-4020-93d5-f795762bf5c9',
             'b2864d62-003e-4464-a6d7-04d3567fb4ee',
             'c4af4e2f-b432-4c3b-8405-cca86cd5b97b',
             'd5af5e3f-c543-5d4c-9516-ddb97ce6c98c'
  );

-- Then delete the test users
DELETE
FROM users
WHERE id IN (
             'efc4b2b8-08be-4020-93d5-f795762bf5c9',
             'b2864d62-003e-4464-a6d7-04d3567fb4ee',
             'c4af4e2f-b432-4c3b-8405-cca86cd5b97b',
             'd5af5e3f-c543-5d4c-9516-ddb97ce6c98c'
  );
