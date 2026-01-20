@test @vault @keys @init
Feature: Initialize vault key material

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'


  Scenario: Initialize vault key material successfully
    * def email = utilsJs.randomEmail('vault_key_init_ok')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * def kekMaster = utilsJs.base64('kek-master')
    * def kekRecovery = utilsJs.base64('kek-recovery')
    * def salt = utilsJs.base64('salt')

    Given path '/vault/keys'
    And headers utilsJs.bearer(accessToken)
    And request
      """
      {
        kekEncMaster: '#(kekMaster)',
        kekEncRecovery: '#(kekRecovery)',
        kdfAlgorithm: 'ARGON2ID',
        kdfSalt: '#(salt)',
        kdfMemoryKib: 65536,
        kdfIterations: 3,
        kdfParallelism: 1,
        kdfOutputLen: 32,
        cryptoVersion: 'v1',
      }
      """
    When method post
    Then status 201


  Scenario: Fail to initialize vault when already initialized
    * def email = utilsJs.randomEmail('vault_key_init_conflict')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * call read(initVaultKeyMaterialHelper) { accessToken: '#(accessToken)' }

    Given path '/vault/keys'
    And headers utilsJs.bearer(accessToken)
    And request
      """
      {
        kekEncMaster: 'AA==',
        kekEncRecovery: 'AA==',
        kdfAlgorithm: 'ARGON2ID',
        kdfSalt: 'AA==',
        kdfMemoryKib: 65536,
        kdfIterations: 3,
        kdfParallelism: 1,
        kdfOutputLen: 32,
        cryptoVersion: 'v1'
      }
      """
    When method post
    Then status 409


  Scenario: Fail to initialize vault without authentication
    Given path '/vault/keys'
    And request {}
    When method post
    Then status 401
