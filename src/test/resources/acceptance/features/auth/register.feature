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
        "email": '#(utilsJs.randomEmail("register_ok"))',
        "password": "password123"
      }
      """
    When method post
    Then status 200


  Scenario: Register fails when email already exists
    * def rq = { email: '#(utilsJs.randomEmail("duplicate"))', password: 'password123' }

    # Register
    * call read(registerHelper) rq

    Given path '/auth/register'
    And request rq

    When method post
    Then status 409
