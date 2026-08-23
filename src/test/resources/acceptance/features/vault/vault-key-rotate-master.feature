@test @vault @keys @rotate
Feature: Rotate master wrapped KEK

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'


  Scenario: Rotate master wrapped KEK with the current ETag
    * def email = utilsJs.randomEmail('vault_key_rotate_current_etag')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * call read(initVaultKeyMaterialHelper) { accessToken: '#(accessToken)' }

    * def newKek = utilsJs.base64('new-master-kek')

    Given path '/vault/keys'
    And headers utilsJs.bearer(accessToken)
    When method get
    Then status 200
    * def currentEtag = responseHeaders['ETag'][0]
    And match currentEtag == '"master-1"'

    Given path '/vault/keys/master'
    And headers utilsJs.bearer(accessToken)
    And header If-Match = currentEtag
    And request
      """
      {
        newKekEncMaster: '#(newKek)'
      }
    """
    When method put
    Then status 200
    And match responseHeaders['ETag'][0] == '"master-2"'
    And match responseHeaders['Cache-Control'][0] == 'no-store'


  Scenario: Get the rotated master wrapped KEK with its incremented ETag
    * def email = utilsJs.randomEmail('vault_key_rotate_get_updated')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * call read(initVaultKeyMaterialHelper) { accessToken: '#(accessToken)' }

    * def newKek = utilsJs.base64('new-master-kek')

    Given path '/vault/keys'
    And headers utilsJs.bearer(accessToken)
    When method get
    Then status 200
    * def currentEtag = responseHeaders['ETag'][0]

    Given path '/vault/keys/master'
    And headers utilsJs.bearer(accessToken)
    And header If-Match = currentEtag
    And request
      """
      {
        newKekEncMaster: '#(newKek)'
      }
      """
    When method put
    Then status 200

    Given path '/vault/keys'
    And headers utilsJs.bearer(accessToken)
    When method get
    Then status 200
    And match responseHeaders['ETag'][0] == '"master-2"'
    And match response.kekEncMaster == newKek


  Scenario: Reject a repeated master wrapped KEK rotation with a stale ETag
    * def email = utilsJs.randomEmail('vault_key_rotate_stale_etag')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * call read(initVaultKeyMaterialHelper) { accessToken: '#(accessToken)' }

    * def newKek = utilsJs.base64('new-master-kek')

    Given path '/vault/keys'
    And headers utilsJs.bearer(accessToken)
    When method get
    Then status 200
    * def currentEtag = responseHeaders['ETag'][0]

    Given path '/vault/keys/master'
    And headers utilsJs.bearer(accessToken)
    And header If-Match = currentEtag
    And request
      """
      {
        newKekEncMaster: '#(newKek)'
      }
      """
    When method put
    Then status 200

    Given path '/vault/keys/master'
    And headers utilsJs.bearer(accessToken)
    And header If-Match = currentEtag
    And request
      """
      {
        newKekEncMaster: '#(newKek)'
      }
      """
    When method put
    Then status 412

    Given path '/vault/keys'
    And headers utilsJs.bearer(accessToken)
    When method get
    Then status 200
    And match responseHeaders['ETag'][0] == '"master-2"'
    And match response.kekEncMaster == newKek


  Scenario: Fail to rotate master KEK when vault not initialized
    * def email = utilsJs.randomEmail('vault_key_rotate_not_init')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    Given path '/vault/keys/master'
    And headers utilsJs.bearer(accessToken)
    And header If-Match = '"master-1"'
    And request
      """
      {
        newKekEncMaster: 'AA=='
      }
      """
    When method put
    Then status 404


  Scenario: Fail to rotate master KEK when If-Match is missing
    * def email = utilsJs.randomEmail('vault_key_rotate_missing_if_match')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken
    * call read(initVaultKeyMaterialHelper) { accessToken: '#(accessToken)' }

    Given path '/vault/keys/master'
    And headers utilsJs.bearer(accessToken)
    And request { newKekEncMaster: 'AA==' }
    When method put
    Then status 428

    Given path '/vault/keys'
    And headers utilsJs.bearer(accessToken)
    When method get
    Then status 200
    And match responseHeaders['ETag'][0] == '"master-1"'


  Scenario Outline: Fail to rotate master KEK with an invalid If-Match
    * def email = utilsJs.randomEmail('vault_key_rotate_invalid_if_match')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken
    * call read(initVaultKeyMaterialHelper) { accessToken: '#(accessToken)' }

    Given path '/vault/keys/master'
    And headers utilsJs.bearer(accessToken)
    And header If-Match = '<ifMatch>'
    And request { newKekEncMaster: 'AA==' }
    When method put
    Then status 400

    Given path '/vault/keys'
    And headers utilsJs.bearer(accessToken)
    When method get
    Then status 200
    And match responseHeaders['ETag'][0] == '"master-1"'

    Examples:
      | ifMatch                 |
      | W/"master-1"           |
      | *                       |
      | "master-1", "master-2" |


  Scenario: Fail to rotate master KEK without authentication
    Given path '/vault/keys/master'
    And request { newKekEncMaster: 'AA==' }
    When method put
    Then status 401
