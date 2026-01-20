@test @vault @keys @rotate
Feature: Rotate master wrapped KEK

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'


  Scenario: Rotate master wrapped KEK successfully
    * def email = utilsJs.randomEmail('vault_key_rotate_ok')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * call read(initVaultKeyMaterialHelper) { accessToken: '#(accessToken)' }

    * def newKek = utilsJs.base64('new-master-kek')

    Given path '/vault/keys/master'
    And headers utilsJs.bearer(accessToken)
    And request
      """
      {
        newKekEncMaster: '#(newKek)'
      }
      """
    When method put
    Then status 200


  Scenario: Fail to rotate master KEK when vault not initialized
    * def email = utilsJs.randomEmail('vault_key_rotate_not_init')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    Given path '/vault/keys/master'
    And headers utilsJs.bearer(accessToken)
    And request
      """
      {
        newKekEncMaster: 'AA=='
      }
      """
    When method put
    Then status 404


  Scenario: Fail to rotate master KEK without authentication
    Given path '/vault/keys/master'
    And request { newKekEncMaster: 'AA==' }
    When method put
    Then status 401
