@test @user @profile @create
Feature: Create authenticated user profile

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

    Given path '/user/profile'
    And headers utilsJs.bearer(accessToken)
    And request { displayName: 'First' }
    When method post
    Then status 201

    Given path '/user/profile'
    And headers utilsJs.bearer(accessToken)
    And request { displayName: 'Second' }
    When method post
    Then status 409


  Scenario Outline: Creating user profile with invalid display name fails
    * def email = '<email>'
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * def requestBody =
      """
      {
        "displayName": <displayName>
      }
      """

    Given path '/user/profile'
    And headers utilsJs.bearer(accessToken)
    And request requestBody
    When method post
    Then status 400

    Examples:
      | email                             | displayName          |
      | profile_invalid_empty@safecube.io | ""                   |
      | profile_invalid_null@safecube.io  | null                 |
      | profile_invalid_long@safecube.io  | "#('a'.repeat(101))" |

