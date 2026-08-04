@test @security
Feature: Secure items API security

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'


  Scenario: Reject malformed token
    Given path '/vault/items'
    And header Authorization = 'Bearer this-is-not-a-jwt'
    When method get
    Then status 401


  Scenario: Reject missing Bearer prefix
    Given path '/vault/items'
    And header Authorization = 'invalidtoken'
    When method get
    Then status 401


  Scenario: Reject empty token
    Given path '/vault/items'
    And header Authorization = 'Bearer '
    When method get
    Then status 401


  Scenario: Reject access with token of another account (cross-account protection)
    # User A
    * def emailA = utilsJs.randomEmail('sec_items_sec_A')
    * def password = 'password123'
    * def credentialsA = { email: '#(emailA)', password: '#(password)' }

    * def authA = call read(createUserHelper) credentialsA
    * def tokenA = authA.accessToken

    * def itemA = call read(createSecureItemHelper) { accessToken: '#(tokenA)' }

    # User B
    * def emailB = utilsJs.randomEmail('sec_items_sec_B')
    * def credentialsB = { email: '#(emailB)', password: '#(password)' }

    * def authB = call read(createUserHelper) credentialsB
    * def tokenB = authB.accessToken

    Given path '/vault/items', itemA.itemId
    And headers utilsJs.bearer(tokenB)
    When method get
    Then status 404


  Scenario: Prevent access using manipulated token payload
    * def email = utilsJs.randomEmail('sec_items_sec_tamper')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def validToken = auth.accessToken

    # Corrupt the token (change last char)
    * def tamperedToken = validToken.substring(0, validToken.length - 2) + 'xx'

    Given path '/vault/items'
    And header Authorization = 'Bearer ' + tamperedToken
    When method get
    Then status 401


  Scenario: Reject SQL injection attempt in itemId
    * def email = utilsJs.randomEmail('sec_items_sec_sql')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    Given path '/vault/items', "' OR '1'='1"
    And headers utilsJs.bearer(accessToken)
    When method get
    Then status 400


  Scenario: Reject XSS payload in query params
    * def email = utilsJs.randomEmail('sec_items_sec_xss')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    Given path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    And param type = '<script>alert(1)</script>'
    When method get
    Then status 400
