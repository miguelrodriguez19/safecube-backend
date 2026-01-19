@test @vault @list
Feature: List secure items

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'


  Scenario: List secure items for authenticated user
    * def email = utilsJs.randomEmail('sec_items_list')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    # Create two items
    * call read(createSecureItemHelper) { accessToken: '#(accessToken)', displayHint: 'Item 1' }
    * call read(createSecureItemHelper) { accessToken: '#(accessToken)', displayHint: 'Item 2' }

    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    When method get
    Then status 200

    And match response.items == '#[2]'
    And match response.items[*].displayHint contains ['Item 1', 'Item 2']
    And match each response.items[*].itemId == '#uuid'


  Scenario: List returns empty when user has no items
    * def email = utilsJs.randomEmail('sec_items_empty')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    When method get
    Then status 200
    And match response.items == []


  Scenario: Items are isolated per account
    # User A
    * def emailA = utilsJs.randomEmail('sec_items_A')
    * def password = 'password123'
    * def credentialsA = { email: '#(emailA)', password: '#(password)' }

    * def authA = call read(createUserHelper) credentialsA
    * def tokenA = authA.accessToken

    * call read(createSecureItemHelper) { accessToken: '#(tokenA)', displayHint: 'User A item' }

    # User B
    * def emailB = utilsJs.randomEmail('sec_items_B')
    * def credentialsB = { email: '#(emailB)', password: '#(password)' }

    * def authB = call read(createUserHelper) credentialsB
    * def tokenB = authB.accessToken

    * call read(createSecureItemHelper) { accessToken: '#(tokenB)', displayHint: 'User B item' }

    # List for user A
    Given path '/vault/items'
    And headers utilsJs.bearer(tokenA)
    When method get
    Then status 200

    And match response.items == '#[1]'
    And match response.items[0].displayHint == 'User A item'


  Scenario: Fail to list secure items without authentication
    Given path '/vault/items'
    When method get
    Then status 401
