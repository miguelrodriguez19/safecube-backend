@test @user @profile @security
Feature: User profile security constraints

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'


  Scenario: Access profile endpoint without authentication fails
    Given path '/user/profile'
    When method get
    Then status 401
