Feature: Workspace Repository

  Scenario: Create a workspace
    Given a workspace to be created
    When the workspace is created
    Then the workspace should be saved in the database

  Scenario: Update a workspace
    Given an existing workspace
    When the workspace is updated
    Then the workspace should be updated in the database

  Scenario: Handle error when updating a non-existent workspace
    Given a non-existent workspace
    When the workspace is updated
    Then a workspace exception should be thrown

  Scenario: Delete a workspace
    Given an existing workspace
    When the workspace is deleted
    Then the workspace should be removed from the database

  Scenario: Find a workspace by id
    Given an existing workspace
    When the workspace is searched by id
    Then the workspace should be found

  Scenario: Find all workspaces by memberId
    Given an existing workspace with a member
    When the workspaces are searched by member id
    Then the workspaces should be found

  Scenario: Find workspace members by workspace id
    Given an existing workspace with a member
    When the members are searched by workspace id
    Then the members should be found

  Scenario: Find workspace members by user id
    Given an existing workspace with a member
    When the members are searched by user id
    Then the members should be found

  Scenario: Check if user is member of workspace
    Given an existing workspace with a member
    When checking if the user is a member of the workspace
    Then the result should be true

  Scenario: Insert and delete workspace member
    Given an existing workspace with a member
    When the member is deleted and then inserted again
    Then the member should be correctly deleted and inserted
