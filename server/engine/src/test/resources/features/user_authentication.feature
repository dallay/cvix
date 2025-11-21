Feature: User Authentication

  Scenario: Authenticate a user
    Given a user with valid credentials
    When the user authenticates
    Then the user should be authenticated successfully
