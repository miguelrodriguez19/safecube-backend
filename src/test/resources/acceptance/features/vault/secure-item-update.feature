@test @vault @update
Feature: Update secure item

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'


  Scenario: Update existing secure item successfully
    * def email = utilsJs.randomEmail('sec_items_update_ok')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * def item = call read(createSecureItemHelper) { accessToken: '#(accessToken)' }

    * def instantNow = utilsJs.instantNow()
    * def newPayload = utilsJs.base64('{"secret":"updated"}')
    * def mutationId = utilsJs.uuid()
    * def nextPayloadVersion = item.responsePayloadVersion + 1
    * def requestBody =
      """
      {
        itemType: 'PASSWORD',
        schemaVersion: 2,
        displayHint: 'Updated secure item',
        payload: '#(newPayload)',
        payloadVersion: '#(nextPayloadVersion)'
      }
      """

    Given path '/vault/items', item.itemId
    And headers utilsJs.bearer(accessToken)
    And header Idempotency-Key = mutationId
    And header If-Match = utilsJs.etag(item.itemRevision)
    And request requestBody

    When method put
    Then status 200

    And match response.itemId == item.itemId
    And match response.mutationId == mutationId
    And match response.payloadVersion == nextPayloadVersion
    And match response.itemRevision == item.itemRevision + 1
    And match response.changeSequence == item.changeSequence + 1
    And match response.updatedAt == '#present'
    And match responseHeaders['ETag'][0] == utilsJs.etag(response.itemRevision)
    And assert utilsJs.compareDates(response.updatedAt, instantNow) >= 1


  Scenario: Fail to update item from another account
    # User A
    * def emailA = utilsJs.randomEmail('sec_items_update_A')
    * def password = 'password123'
    * def credentialsA = { email: '#(emailA)', password: '#(password)' }

    * def authA = call read(createUserHelper) credentialsA
    * def tokenA = authA.accessToken

    * def itemA = call read(createSecureItemHelper) { accessToken: '#(tokenA)' }

    # User B
    * def emailB = utilsJs.randomEmail('sec_items_update_B')
    * def credentialsB = { email: '#(emailB)', password: '#(password)' }

    * def authB = call read(createUserHelper) credentialsB
    * def tokenB = authB.accessToken

    * def chipherPayload = utilsJs.base64("my updated secret")
    * def mutationId = utilsJs.uuid()

    Given path '/vault/items', itemA.itemId
    And headers utilsJs.bearer(tokenB)
    And header Idempotency-Key = mutationId
    And header If-Match = utilsJs.etag(itemA.itemRevision)
    And request
      """
      {
        itemType: 'PASSWORD',
        schemaVersion: 1,
        displayHint: 'Hacked',
        payload: '#(chipherPayload)',
        payloadVersion: 2
      }
      """
    When method put
    Then status 404


  Scenario: Fail to update item with invalid data
    * def email = utilsJs.randomEmail('sec_items_update_invalid')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * def item = call read(createSecureItemHelper) { accessToken: '#(accessToken)' }
    * def mutationId = utilsJs.uuid()

    Given path '/vault/items', item.itemId
    And headers utilsJs.bearer(accessToken)
    And header Idempotency-Key = mutationId
    And header If-Match = utilsJs.etag(item.itemRevision)
    And request
      """
      {
        itemType: null,
        schemaVersion: 1,
        displayHint: '',
        payload: null,
        payloadVersion: null
      }
      """
    When method put
    Then status 400


  Scenario: Fail to update item without authentication
    * def mutationId = utilsJs.uuid()
    Given path '/vault/items', '00000000-0000-0000-0000-000000000000'
    And header Idempotency-Key = mutationId
    And header If-Match = '"1"'
    And request
      """
      {
        itemType: 'PASSWORD',
        schemaVersion: 1,
        displayHint: 'X',
        payload: '#(utilsJs.base64("x"))',
        payloadVersion: 1
      }
      """
    When method put
    Then status 401
