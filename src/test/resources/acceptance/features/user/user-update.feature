@test @user @profile @update
Feature: Update authenticated user profile

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'


  Scenario: Update existing user profile
    * def email = 'profile_update@safecube.io'
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    Given path '/user/profile'
    And headers utilsJs.bearer(accessToken)
    And request { displayName: 'Initial' }
    When method post
    Then status 201

    Given path '/user/profile'
    And headers utilsJs.bearer(accessToken)
    And request { displayName: 'Updated' }
    When method put
    Then status 200
    And match response.displayName == 'Updated'


  Scenario: Update user profile when none exists
    * def email = 'profile_update_missing@safecube.io'
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    Given path '/user/profile'
    And headers utilsJs.bearer(accessToken)
    And request { displayName: 'Should fail' }
    When method put
    Then status 404
