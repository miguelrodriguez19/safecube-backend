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
    * def mutationId = utilsJs.uuid()

    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    And header Idempotency-Key = mutationId
    And request
      """
      {
        itemType: 'PASSWORD',
        schemaVersion: 1,
        displayHint: 'My password',
        payload: '#(payloadBase64)',
        payloadVersion: 1
      }
      """
    When method post
    Then status 201
    And match response.mutationId == mutationId
    And match response.payloadVersion == 1
    And match response.itemRevision == 1
    And match response.changeSequence == '#number'
    And match response.updatedAt == '#string'
    And match responseHeaders['ETag'][0] == '"1"'

  Scenario: Fail to create secure item without authentication
    * def payloadBytes = utilsJs.base64('{"secret":"value"}')
    * def mutationId = utilsJs.uuid()

    Given path '/vault/items'
    And header Idempotency-Key = mutationId
    And request
      """
      {
        itemType: 'PASSWORD',
        schemaVersion: 1,
        displayHint: 'My password',
        payload: '#(payloadBytes)',
        payloadVersion: 1
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
    * def mutationId = utilsJs.uuid()

    * def payloadBytes = utilsJs.base64('{"secret":"value"}')

    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    And header Idempotency-Key = mutationId
    And request
      """
      {
        schemaVersion: 1,
        displayHint: 'My password',
        payloadVersion: 1
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
    * def mutationId = utilsJs.uuid()

    * def longText = 'a'.repeat(256)
    * def payloadBytes = utilsJs.base64('{"secret":"value"}')

    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    And header Idempotency-Key = mutationId
    And request
      """
      {
        itemType: 'PASSWORD',
        schemaVersion: 1,
        displayHint: '#(longText)',
        payload: '#(payloadBytes)',
        payloadVersion: 1
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
    * def mutationId = utilsJs.uuid()

    * def payloadBytes = utilsJs.base64('{"secret":"value"}')

    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    And header Idempotency-Key = mutationId
    And request
      """
      {
        itemType: 'INVALID_TYPE',
        schemaVersion: 1,
        displayHint: 'My password',
        payload: '#(payloadBytes)',
        payloadVersion: 1
      }
      """
    When method post
    Then status 400


  Scenario: Fail to create secure item without Idempotency-Key
    * def email = utilsJs.randomEmail('sec_item_create_missing_idempotency')
    * def credentials = { email: '#(email)', password: 'password123' }
    * def auth = call read(createUserHelper) credentials
    * def payloadBytes = utilsJs.base64('{"secret":"value"}')

    Given path '/vault/items'
    And headers utilsJs.bearer(auth.accessToken)
    And request
      """
      {
        itemType: 'PASSWORD',
        schemaVersion: 1,
        displayHint: 'My password',
        payload: '#(payloadBytes)',
        payloadVersion: 1
      }
      """
    When method post
    Then status 400
    And match response.error == 'VALIDATION_FAILED'
    And match response.fields['Idempotency-Key'] == '#present'
