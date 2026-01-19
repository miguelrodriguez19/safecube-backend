@test @vault @create
Feature: Create secure item

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'


  Scenario: Create secure item successfully
    * def email = utilsJs.randomEmail('sec_item_create_ok')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * print auth
    * def accessToken = auth.accessToken

    * def payloadBase64 = utilsJs.base64('{"secret":"value"}')

    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    And request
      """
      {
        itemType: 'PASSWORD',
        schemaVersion: 1,
        displayHint: 'My password',
        payload: '#(payloadBase64)'
      }
      """
    When method post
    Then status 201



  Scenario: Fail to create secure item without authentication
    * def payloadBytes = utilsJs.base64('{"secret":"value"}')

    Given path '/vault/items'
    And request
      """
      {
        itemType: 'PASSWORD',
        schemaVersion: 1,
        displayHint: 'My password',
        payload: '#(payloadBytes)'
      }
      """
    When method post
    Then status 401


  Scenario: Fail to create secure item with missing required fields
    * def email = utilsJs.randomEmail('sec_item_create_missing_fields')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * def payloadBytes = utilsJs.base64('{"secret":"value"}')

    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    And request
      """
      {
        schemaVersion: 1,
        displayHint: 'My password'
      }
      """
    When method post
    Then status 400


  Scenario: Fail to create secure item with displayHint too long
    * def email = utilsJs.randomEmail('sec_item_create_long_display')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * def longText = 'a'.repeat(256)
    * def payloadBytes = utilsJs.base64('{"secret":"value"}')

    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    And request
      """
      {
        itemType: 'PASSWORD',
        schemaVersion: 1,
        displayHint: '#(longText)',
        payload: '#(payloadBytes)'
      }
      """
    When method post
    Then status 400


  Scenario: Fail to create secure item with invalid item type
    * def email = utilsJs.randomEmail('sec_item_create_invalid_type')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * def payloadBytes = utilsJs.base64('{"secret":"value"}')

    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    And request
      """
      {
        itemType: 'INVALID_TYPE',
        schemaVersion: 1,
        displayHint: 'My password',
        payload: '#(payloadBytes)'
      }
      """
    When method post
    Then status 400
