@test @auth @authentication
Feature: Authenticate authentication account

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'

  Scenario: Authenticate account successfully
    # Register account first
    Given path '/auth/register'
    And request
      """
      {
        "email": "login@safecube.io",
        "password": "password123"
      }
      """
    When method post
    Then status 200

    # Authenticate
    Given path '/auth/login'
    And request
      """
      {
        "email": "login@safecube.io",
        "password": "password123"
      }
      """
    When method post
    Then status 200
    And match response.accountId != null
    And match response.authenticatedAt != null


  Scenario: Authenticate fails with wrong password
    # Register account first
    Given path '/auth/register'
    And request
      """
      {
        "email": "wrong-password@safecube.io",
        "password": "password123"
      }
      """
    When method post
    Then status 200

    # Wrong password
    Given path '/auth/login'
    And request
      """
      {
        "email": "wrong-password@safecube.io",
        "password": "invalid-password"
      }
      """
    When method post
    Then status 401


  Scenario: Authenticate fails when account does not exist
    Given path '/auth/login'
    And request
      """
      {
        "email": "unknown@safecube.io",
        "password": "password123"
      }
      """
    When method post
    Then status 401


  @disabled
  Scenario: Authenticate fails when account is disabled
    # Register account first
    Given path '/auth/register'
    And request
      """
      {
        "email": "disabled@safecube.io",
        "password": "password123"
      }
      """
    When method post
    Then status 200

    # NOTE:
    # This scenario assumes the account is disabled out-of-band.
    # It can be implemented later via:
    # - test-only endpoint
    # - database fixture
    # - SQL script
    # - admin use case

    Given path '/auth/login'
    And request
      """
      {
        "email": "disabled@safecube.io",
        "password": "password123"
      }
      """
    When method post
    Then status 403
