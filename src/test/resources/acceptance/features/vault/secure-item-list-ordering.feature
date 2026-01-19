@test @vault @list @ordering
Feature: List secure items with ordering

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'


  Scenario: Order by display name ascending
    * def email = utilsJs.randomEmail('sec_items_order_asc')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * call read(createSecureItemHelper) { accessToken: '#(accessToken)', displayHint: 'B item' }
    * call read(createSecureItemHelper) { accessToken: '#(accessToken)', displayHint: 'A item' }
    * call read(createSecureItemHelper) { accessToken: '#(accessToken)', displayHint: 'C item' }

    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    And param order = 'DISPLAY_NAME_ASC'
    When method get
    Then status 200

    And match response.items[0].displayHint == 'A item'
    And match response.items[1].displayHint == 'B item'
    And match response.items[2].displayHint == 'C item'


  Scenario: Order by display name descending
    * def email = utilsJs.randomEmail('sec_items_order_desc')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * call read(createSecureItemHelper) { accessToken: '#(accessToken)', displayHint: 'A item' }
    * call read(createSecureItemHelper) { accessToken: '#(accessToken)', displayHint: 'B item' }
    * call read(createSecureItemHelper) { accessToken: '#(accessToken)', displayHint: 'C item' }

    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    And param order = 'DISPLAY_NAME_DESC'
    When method get
    Then status 200

    And match response.items[0].displayHint == 'C item'
    And match response.items[1].displayHint == 'B item'
    And match response.items[2].displayHint == 'A item'


  Scenario: Order by updatedAt ascending
    * def email = utilsJs.randomEmail('sec_items_order_updated_asc')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * call read(createSecureItemHelper) { accessToken: '#(accessToken)', displayHint: 'First' }
    * eval karate.pause(1200)
    * call read(createSecureItemHelper) { accessToken: '#(accessToken)', displayHint: 'Second' }
    * eval karate.pause(1200)
    * call read(createSecureItemHelper) { accessToken: '#(accessToken)', displayHint: 'Third' }

    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    And param order = 'UPDATED_AT_ASC'
    When method get
    Then status 200

    And match response.items[0].displayHint == 'First'
    And match response.items[2].displayHint == 'Third'


  Scenario: Order by updatedAt descending
    * def email = utilsJs.randomEmail('sec_items_order_updated_desc')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * call read(createSecureItemHelper) { accessToken: '#(accessToken)', displayHint: 'First' }
    * eval karate.pause(1200)
    * call read(createSecureItemHelper) { accessToken: '#(accessToken)', displayHint: 'Second' }
    * eval karate.pause(1200)
    * call read(createSecureItemHelper) { accessToken: '#(accessToken)', displayHint: 'Third' }

    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    And param order = 'UPDATED_AT_DESC'
    When method get
    Then status 200

    And match response.items[0].displayHint == 'Third'
    And match response.items[2].displayHint == 'First'


  Scenario: Fail when order value is invalid
    * def email = utilsJs.randomEmail('sec_items_order_invalid')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    And param order = 'INVALID_ORDER'
    When method get
    Then status 400
