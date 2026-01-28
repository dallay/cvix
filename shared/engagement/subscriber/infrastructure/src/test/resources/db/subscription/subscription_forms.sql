-- file: subscription_forms.sql
-- Insert a subscription form and related data for integration tests

INSERT INTO subscription_forms (
  id, name, description, header, input_placeholder, button_text, button_color,
  background_color, text_color, button_text_color, status, workspace_id, created_at, created_by
)
VALUES (
  '11111111-1111-1111-1111-111111111111',
  'Test Form',
  'Test Description',
  'Header',
  'Placeholder',
  'Submit',
  '#000000',
  '#FFFFFF',
  '#000000',
  '#FFFFFF',
  'PUBLISHED',
  '22222222-2222-2222-2222-222222222222',
  now(),
  'system'
);
