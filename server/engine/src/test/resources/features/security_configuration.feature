Feature: Security Configuration

  Scenario: Map authorities with OidcUserAuthority
    Given a user with OidcUserAuthority
    When the authorities are mapped
    Then the mapping should be successful

  Scenario: Map authorities with SimpleGrantedAuthority
    Given a user with SimpleGrantedAuthority
    When the authorities are mapped
    Then the mapping should be successful
