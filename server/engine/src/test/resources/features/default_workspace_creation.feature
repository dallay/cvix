Feature: Default Workspace Creation

  Scenario: Create default workspace for new user
    Given a new user is created with a first and last name
    When the user created event is published
    Then a default workspace should be created with the user's full name

  Scenario: Create default workspace with firstname only
    Given a new user is created with only a first name
    When the user created event is published
    Then a default workspace should be created with the user's first name

  Scenario: Create default workspace with lastname only
    Given a new user is created with only a last name
    When the user created event is published
    Then a default workspace should be created with the user's last name

  Scenario: Create default workspace with "My Workspace"
    Given a new user is created with no name
    When the user created event is published
    Then a default workspace should be created with the name "My Workspace"

  Scenario: Should not create a workspace when the user already has one
    Given a user already has a workspace
    When a user created event is published for that user
    Then no new workspace should be created

  Scenario: Handle workspace names with special characters and whitespace
    Given a new user is created with a name containing special characters and whitespace
    When the user created event is published
    Then a default workspace should be created with a trimmed and formatted name

  Scenario: Create only one workspace for concurrent duplicate events
    Given duplicate user created events are published concurrently
    When the events are processed
    Then only one workspace should be created
