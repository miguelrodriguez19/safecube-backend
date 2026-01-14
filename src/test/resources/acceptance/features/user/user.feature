@user @profile
Feature: Manage authenticated user profile

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'

  Scenario: Create user profile successfully
    * def email = 'profile_create@safecube.io'
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    Given path '/user/profile'
    And headers utilsJs.bearer(accessToken)
    And request { displayName: 'Safecube' }

    When method post
    Then status 201
    And match response ==
      """
      {
        userId: '#uuid',
        accountId: '#uuid',
        displayName: 'Safecube',
        createdAt: '#string',
        updatedAt: '#string'
      }
      """

  Scenario: Creating user profile twice fails
    * def email = 'profile_duplicate@safecube.io'
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    # First create
    Given path '/user/profile'
    And headers utilsJs.bearer(accessToken)
    And request { displayName: 'Safecube' }
    When method post
    Then status 201

    # Second create
    Given path '/user/profile'
    And headers utilsJs.bearer(accessToken)
    And request { displayName: 'Safecube Again' }
    When method post
    Then status 409

  Scenario: Get existing user profile
    * def email = 'profile_get@safecube.io'
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    # Create profile
    Given path '/user/profile'
    And headers utilsJs.bearer(accessToken)
    And request { displayName: 'Safecube' }
    When method post
    Then status 201

    # Get profile
    Given path '/user/profile'
    And headers utilsJs.bearer(accessToken)
    When method get
    Then status 200
    And match response.displayName == 'Safecube'
    And match response.userId == '#uuid'
    And match response.accountId == '#uuid'

  Scenario: Update user profile
    * def email = 'profile_update@safecube.io'
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    # Create profile
    Given path '/user/profile'
    And headers utilsJs.bearer(accessToken)
    And request { displayName: 'Safecube' }
    When method post
    Then status 201

    # Update profile
    Given path '/user/profile'
    And headers utilsJs.bearer(accessToken)
    And request { displayName: 'Safecube Updated' }
    When method put
    Then status 200
    And match response.displayName == 'Safecube Updated'

  Scenario: Delete user profile
    * def email = 'profile_delete@safecube.io'
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    # Create profile
    Given path '/user/profile'
    And headers utilsJs.bearer(accessToken)
    And request { displayName: 'Safecube' }
    When method post
    Then status 201

    # Delete profile
    Given path '/user/profile'
    And headers utilsJs.bearer(accessToken)
    When method delete
    Then status 204

    # Profile no longer exists
    Given path '/user/profile'
    And headers utilsJs.bearer(accessToken)
    When method get
    Then status 404
