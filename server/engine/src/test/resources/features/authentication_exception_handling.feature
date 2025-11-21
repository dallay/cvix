Feature: Authentication Exception Handling

  Scenario: Handle Not Authenticated User Exception
    When a request is made to an endpoint that requires authentication without being authenticated
    Then the response should be a 401 Unauthorized error

  Scenario: Handle Unknown Authentication Exception
    When a request is made to an endpoint that triggers an unknown authentication exception
    Then the response should be a 5xx Server Error
