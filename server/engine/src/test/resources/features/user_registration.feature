Feature: User Registration

  Scenario: A new user registers successfully
    Given a new user with valid registration details
    When the user submits the registration form
    Then the registration should be successful

  Scenario: Should not register a new user without csrf token
    Given a new user with valid registration details
    When the user submits the registration form without a CSRF token
    Then the registration should be forbidden

  Scenario: Should not register a new user with an existing email
    Given a user has already registered with an email
    When another user tries to register with the same email
    Then the registration should fail

  Scenario: Should not register a new user with an invalid email
    Given a user provides an invalid email
    When the user submits the registration form
    Then the registration should fail

  Scenario Outline: Should not register a new user with an invalid password
    Given a user provides a password "<password>"
    When the user submits the registration form
    Then the registration should fail
    Examples:
      | password          |
      | "invalid-password"|
      | "1234"            |
      | "12345678"        |
      | "ashihweitwjjw"   |
      | "WEAKPASSWORD1"   |


  Scenario: Should not register a new user with an invalid firstname
    Given a user provides an empty firstname
    When the user submits the registration form
    Then the registration should fail

  Scenario: Should not register a new user with an invalid lastname
    Given a user provides an empty lastname
    When the user submits the registration form
    Then the registration should fail
