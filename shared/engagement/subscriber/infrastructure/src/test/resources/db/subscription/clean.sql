-- file: clean.sql
-- Clean subscription test data
DELETE FROM outbox WHERE aggregate_id = '11111111-1111-1111-1111-111111111111';
DELETE FROM subscription_forms WHERE id = '11111111-1111-1111-1111-111111111111';
