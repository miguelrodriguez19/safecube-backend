@test @vault @list @filters
Feature: List secure items with filters

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'


  Scenario: Filter items by type
    * def email = utilsJs.randomEmail('sec_items_filter_type')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * call read(createSecureItemHelper) { accessToken: '#(accessToken)', itemType: 'PASSWORD', displayHint: 'Password item' }
    * call read(createSecureItemHelper) { accessToken: '#(accessToken)', itemType: 'NOTE', displayHint: 'Note item' }

    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    And param type = 'PASSWORD'
    When method get
    Then status 200

    And match response.items == '#[1]'
    And match response.items[0].itemType == 'PASSWORD'


  Scenario: Filter items created after a given instant
    * def email = utilsJs.randomEmail('sec_items_filter_created_after')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * def first = call read(createSecureItemHelper) { accessToken: '#(accessToken)', displayHint: 'Old item' }

    * eval karate.pause(1200)

    * def second = call read(createSecureItemHelper) { accessToken: '#(accessToken)', displayHint: 'New item' }

    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    And param createdAfter = second.createdAt
    When method get
    Then status 200

    And match response.items == '#[1]'
    And match response.items[0].displayHint == 'New item'


  Scenario: Filter items updated after a given instant
    * def email = utilsJs.randomEmail('sec_items_filter_updated_after')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * def first = call read(createSecureItemHelper) { accessToken: '#(accessToken)', displayHint: 'Old item' }

    * eval karate.pause(1200)

    * def second = call read(createSecureItemHelper) { accessToken: '#(accessToken)', displayHint: 'New item' }

    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    And param updatedAfter = second.createdAt
    When method get
    Then status 200

    And match response.items == '#[1]'
    And match response.items[0].displayHint == 'New item'


  Scenario: Exclude deleted items by default
    * def email = utilsJs.randomEmail('sec_items_filter_deleted_default')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * def item = call read(createSecureItemHelper) { accessToken: '#(accessToken)', displayHint: 'To be deleted' }

    # Soft delete
    Given path '/vault/items', item.itemId
    And headers utilsJs.bearer(accessToken)
    When method delete
    Then status 200

    # List without includeDeleted
    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    When method get
    Then status 200
    And match response.items == []


  Scenario: Include deleted items when includeDeleted=true
    * def email = utilsJs.randomEmail('sec_items_filter_deleted_include')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * def item = call read(createSecureItemHelper) { accessToken: '#(accessToken)', displayHint: 'Deleted item' }

    # Soft delete
    Given path '/vault/items', item.itemId
    And headers utilsJs.bearer(accessToken)
    When method delete
    Then status 200
    And match response.itemId == item.itemId
    And match response.deletedAt == '#present'

    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    And param includeDeleted = true
    When method get
    Then status 200

    And match response.items == '#[1]'
    And match response.items[0].itemId == item.itemId


  Scenario: Combine filters (type + createdAfter)
    * def email = utilsJs.randomEmail('sec_items_filter_combo')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * def oldNote = call read(createSecureItemHelper) { accessToken: '#(accessToken)', itemType: 'NOTE', displayHint: 'Old note' }

    * eval karate.pause(1200)

    * def newPassword = call read(createSecureItemHelper) { accessToken: '#(accessToken)', itemType: 'PASSWORD', displayHint: 'New password' }

    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    And param type = 'PASSWORD'
    And param createdAfter = oldNote.createdAt
    When method get
    Then status 200

    And match response.items == '#[1]'
    And match response.items[0].displayHint == 'New password'
