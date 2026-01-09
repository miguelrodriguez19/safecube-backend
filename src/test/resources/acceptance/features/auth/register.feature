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
        "email": "user@safecube.io",
        "password": "password123"
      }
      """
    When method post
    Then status 200
    And match response.accountId != null
    And match response.createdAt != null


  Scenario: Register fails when email already exists
    Given path '/auth/register'
    And request
      """
      {
        "email": "duplicate@safecube.io",
        "password": "password123"
      }
      """
    When method post
    Then status 200

    Given path '/auth/register'
    And request
      """
      {
        "email": "duplicate@safecube.io",
        "password": "password123"
      }
      """
    When method post
    Then status 409

  Scenario: Register fails with invalid payload
    Given path '/auth/register'
    * def rq = `{"email": "not-an-email","password": ""}`

    And request rq
    And print rq

    When method post
    Then print response
    And status 400
