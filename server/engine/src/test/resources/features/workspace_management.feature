Feature: Workspace Management

  Scenario: Create a new workspace
    Given a user exists with ID "efc4b2b8-08be-4020-93d5-f795762bf5c9"
    When the user creates a new workspace
    Then the workspace should be created successfully

  Scenario: Should fail when the workspace already exists
    Given a workspace with ID "95ded4bb-2946-4dbe-87df-afb701788eb4" already exists
    When the user tries to create a workspace with the same ID
    Then the workspace creation should fail

  Scenario: Delete a workspace
    Given a workspace with ID "a0654720-35dc-49d0-b508-1f7df5d915f1" exists
    When the user deletes the workspace
    Then the workspace should be deleted successfully

  Scenario: Should return OK when deleting a workspace that does not exist
    Given a workspace with ID "94be1a32-cf2e-4dfc-892d-bdd8ac7ad354" does not exist
    When the user deletes the workspace
    Then the workspace deletion should return OK

  Scenario: Update a workspace
    Given a workspace with ID "a0654720-35dc-49d0-b508-1f7df5d915f1" exists
    When the user updates the workspace with valid data
    Then the workspace should be updated successfully

  Scenario: Should return 404 when updating a workspace that does not exist
    Given a workspace with ID "a0654720-35dc-49d0-b508-1f7df5d915f2" does not exist
    When the user updates the workspace with valid data
    Then the workspace update should return 404

  Scenario: Get all workspaces
    Given several workspaces exist
    When the user requests all workspaces
    Then a list of all workspaces should be returned

  Scenario: Find a workspace by ID
    Given a workspace with ID "a0654720-35dc-49d0-b508-1f7df5d915f1" exists
    When the user requests the workspace by its ID
    Then the workspace details should be returned

  Scenario: Should return 404 when a workspace is not found by ID
    Given a workspace with ID "94be1a32-cf2e-4dfc-892d-bdd8ac7ad354" does not exist
    When the user requests the workspace by its ID
    Then the workspace should not be found
