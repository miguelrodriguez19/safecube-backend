@test @vault @delete
Feature: Delete secure item (soft delete)

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'


  Scenario: Delete existing secure item successfully
    * def email = utilsJs.randomEmail('sec_items_delete_ok')
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
    When method delete
    Then status 200

    And match response.itemId == item.itemId
    And match response.mutationId == mutationId
    And match response.payloadVersion == item.responsePayloadVersion
    And match response.itemRevision == item.itemRevision + 1
    And match response.changeSequence == item.changeSequence + 1
    And match response.deletedAt == '#present'
    And match responseHeaders['ETag'][0] == utilsJs.etag(response.itemRevision)


  Scenario: Deleted item is not returned in list by default
    * def email = utilsJs.randomEmail('sec_items_delete_hidden')
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
    When method delete
    Then status 200

    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    When method get
    Then status 200
    And match response.items == []


  Scenario: Fail to delete item from another account
    # User A
    * def emailA = utilsJs.randomEmail('sec_items_delete_A')
    * def password = 'password123'
    * def credentialsA = { email: '#(emailA)', password: '#(password)' }

    * def authA = call read(createUserHelper) credentialsA
    * def tokenA = authA.accessToken

    * def itemA = call read(createSecureItemHelper) { accessToken: '#(tokenA)' }

    # User B
    * def emailB = utilsJs.randomEmail('sec_items_delete_B')
    * def credentialsB = { email: '#(emailB)', password: '#(password)' }

    * def authB = call read(createUserHelper) credentialsB
    * def tokenB = authB.accessToken
    * def mutationId = utilsJs.uuid()

    Given path '/vault/items', itemA.itemId
    And headers utilsJs.bearer(tokenB)
    And header Idempotency-Key = mutationId
    And header If-Match = utilsJs.etag(itemA.itemRevision)
    When method delete
    Then status 404


  Scenario: Fail to delete non existing item
    * def email = utilsJs.randomEmail('sec_items_delete_missing')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken
    * def mutationId = utilsJs.uuid()

    Given path '/vault/items', '00000000-0000-0000-0000-000000000000'
    And headers utilsJs.bearer(accessToken)
    And header Idempotency-Key = mutationId
    And header If-Match = '"1"'
    When method delete
    Then status 404


  Scenario: Fail to delete item without authentication
    * def mutationId = utilsJs.uuid()
    Given path '/vault/items', '00000000-0000-0000-0000-000000000000'
    And header Idempotency-Key = mutationId
    And header If-Match = '"1"'
    When method delete
    Then status 401
