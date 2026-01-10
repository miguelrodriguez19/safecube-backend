@test @auth @register
Feature: Register authentication account

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'


  Scenario: Register account successfully
    Given path '/auth/register'
    And request
      """
      {
        "email": "register@safecube.io",
        "password": "password123"
      }
      """
    When method post
    Then status 200


  Scenario: Register fails when email already exists
    * def rq = { email: 'duplicate@safecube.io', password: 'password123' }

    # Register
    * call read(registerHelper) rq

    Given path '/auth/register'
    And request rq

    When method post
    Then status 409
