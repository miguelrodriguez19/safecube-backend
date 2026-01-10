@test @auth @refresh
Feature: Refresh access tokens

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'


  Scenario: Refresh token rotates tokens successfully
    * def email = 'refresh@safecube.io'
    * def password = 'password123'
    * def rq = { email: '#(email)', password: '#(password)' }

    # Register
    * call read(registerHelper) rq

    # Login
    * def loginResult = call read(loginHelper) rq
    * def refreshToken = loginResult.response.refreshToken

    Given path '/auth/refresh'
    And request { refreshToken: '#(refreshToken)' }

    When method post
    Then status 200
    And match response.accessToken != null
    And match response.refreshToken != refreshToken
