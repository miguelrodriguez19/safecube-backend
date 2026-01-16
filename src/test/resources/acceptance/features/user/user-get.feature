@test @user @profile @get
Feature: Retrieve authenticated user profile

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'


  Scenario: Get existing user profile
    * def email = 'profile_get@safecube.io'
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    Given path '/user/profile'
    And headers utilsJs.bearer(accessToken)
    And request { displayName: 'Safecube' }
    When method post
    Then status 201

    Given path '/user/profile'
    And headers utilsJs.bearer(accessToken)
    When method get
    Then status 200
    And match response.displayName == 'Safecube'
    And match response.userId == '#uuid'
    And match response.accountId == '#uuid'


  Scenario: Get user profile when none exists
    * def email = 'profile_get_missing@safecube.io'
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    Given path '/user/profile'
    And headers utilsJs.bearer(accessToken)
    When method get
    Then status 404
