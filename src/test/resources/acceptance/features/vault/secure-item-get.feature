@test @vault @get
Feature: Get secure item by id

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'


  Scenario: Get existing secure item
    * def email = utilsJs.randomEmail('sec_items_get_ok')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * def item = call read(createSecureItemHelper) { accessToken: '#(accessToken)' }

    Given path '/vault/items', item.itemId
    And headers utilsJs.bearer(accessToken)
    When method get
    Then status 200

    And match response.itemId == item.itemId
    And match response.itemType == 'PASSWORD'
    And match response.schemaVersion == 1
    And match response.displayHint == 'Default secure item'
    And match response.payload == '#notnull'
    And match response.payloadVersion == item.responsePayloadVersion
    And match response.itemRevision == item.itemRevision
    And match response.changeSequence == item.changeSequence
    And match response.updatedAt == '#present'
    And match response.deletedAt == null
    And match responseHeaders['ETag'][0] == utilsJs.etag(item.itemRevision)


  Scenario: Fail to get item from another account
    # User A
    * def emailA = utilsJs.randomEmail('sec_items_get_A')
    * def password = 'password123'
    * def credentialsA = { email: '#(emailA)', password: '#(password)' }

    * def authA = call read(createUserHelper) credentialsA
    * def tokenA = authA.accessToken

    * def itemA = call read(createSecureItemHelper) { accessToken: '#(tokenA)' }

    # User B
    * def emailB = utilsJs.randomEmail('sec_items_get_B')
    * def credentialsB = { email: '#(emailB)', password: '#(password)' }

    * def authB = call read(createUserHelper) credentialsB
    * def tokenB = authB.accessToken

    Given path '/vault/items', itemA.itemId
    And headers utilsJs.bearer(tokenB)
    When method get
    Then status 404


  Scenario: Fail to get non existing item
    * def email = utilsJs.randomEmail('sec_items_get_missing')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    Given path '/vault/items', '00000000-0000-0000-0000-000000000000'
    And headers utilsJs.bearer(accessToken)
    When method get
    Then status 404


  Scenario: Fail to get item without authentication
    Given path '/vault/items', '00000000-0000-0000-0000-000000000000'
    When method get
    Then status 401
