@test @auth @logout
Feature: Logout and revoke refresh tokens

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'

  Scenario: Logout revokes refresh token
   * def email = utilsJs.randomEmail('logout')
    * def password = 'password123'
    * def rq = { email: '#(email)', password: '#(password)' }

    # Register
    * call read(registerHelper) rq

    # Login
    * def loginResult = call read(loginHelper) rq
    * def accessToken = loginResult.response.accessToken
    * def refreshToken = loginResult.response.refreshToken

    # Logout
    Given path '/auth/logout'
    And headers utilsJs.bearer(accessToken)
    When method post
    Then status 204

    # Refresh should fail
    Given path '/auth/refresh'
    And request { refreshToken: '#(refreshToken)' }
    When method post
    Then status 401
