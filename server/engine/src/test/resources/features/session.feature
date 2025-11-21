Feature: Session

  Scenario: Get user session
    When the user requests their session information
    Then the session information should be returned
