@test @user @profile @security
Feature: User profile security constraints

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'


  Scenario: Access profile endpoint without authentication fails
    Given path '/user/profile'
    When method get
    Then status 401


  Scenario: User cannot update another account profile
    * def password = 'password123'

    * def credentialsA = { email: '#(utilsJs.randomEmail("profile_security_A"))', password: '#(password)' }
    * def authA = call read(createUserHelper) credentialsA
    * def tokenA = authA.accessToken

    Given path '/user/profile'
    And headers utilsJs.bearer(tokenA)
    And request { displayName: 'User A' }
    When method post
    Then status 201

    * def credentialsB = { email: '#(utilsJs.randomEmail("profile_security_B"))', password: '#(password)' }
    * def authB = call read(createUserHelper) credentialsB
    * def tokenB = authB.accessToken

    Given path '/user/profile'
    And headers utilsJs.bearer(tokenB)
    And request { displayName: 'User B tries to update A' }
    When method put
    Then status 404

    Given path '/user/profile'
    And headers utilsJs.bearer(tokenA)
    When method get
    Then status 200
    And match response.displayName == 'User A'
