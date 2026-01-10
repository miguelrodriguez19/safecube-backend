@test @auth @authentication
Feature: Authenticate account and use access token

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'


  Scenario: Login succeeds and returns tokens
    * def email = 'login@safecube.io'
    * def password = 'password123'
    * def rq = { email: '#(email)', password: '#(password)' }

    # Register
    * call read(registerHelper) rq

    # Login
    * def result = call read(loginHelper) rq

    Then match result.response.accessToken != null
    And match result.response.refreshToken != null
    And match result.response.issuedAt != null


  Scenario: Access protected endpoint with valid access token
    * def email = 'protected@safecube.io'
    * def password = 'password123'
    * def rq = { email: '#(email)', password: '#(password)' }

    # Register
    * call read(registerHelper) rq

    # Login
    * def loginResult = call read(loginHelper) rq
    * def accessToken = loginResult.response.accessToken

    Given path '/auth/logout'
    And headers utilsJs.bearer(accessToken)

    When method post
    Then status 204


  Scenario Outline: Login fails with invalid credentials
    Given path '/auth/login'
    And request
      """
      {
        "email": "<email>",
        "password": "<password>"
      }
      """

    When method post
    Then status 401

    Examples:
      | email               | password         |
      | unknown@safecube.io | password123      |
      | wrong@safecube.io   | invalid-password |


  Scenario Outline: Login fails with invalid payload
    * def rq = { "email": "<email>", "password": "<password>"}

    Given path '/auth/login'
    And request rq

    When method post
    Then status 400

    Examples:
      | email                  | password     |
      | not-an-email           | password1234 |
      | validEmail@safecube.io |              |