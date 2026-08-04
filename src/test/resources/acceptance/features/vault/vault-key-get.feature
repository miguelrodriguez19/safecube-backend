@test @vault @keys @get
Feature: Get vault key material

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'


  Scenario: Get vault key material successfully
    * def email = utilsJs.randomEmail('vault_key_get_ok')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    * call read(initVaultKeyMaterialHelper) { accessToken: '#(accessToken)' }

    Given path '/vault/keys'
    And headers utilsJs.bearer(accessToken)
    When method get
    Then status 200
    And match response ==
      """
      {
        accountId: '#uuid',
        kekEncMaster: '#string',
        kekEncRecovery: '#string',
        kdfAlgorithm: 'ARGON2ID',
        kdfSalt: '#string',
        kdfMemoryKib: 65536,
        kdfIterations: 3,
        kdfParallelism: 1,
        kdfOutputLen: 32,
        cryptoVersion: 'v1',
        createdAt: '#string',
        updatedAt: '#string'
      }
      """

  Scenario: Fail to get vault key material when not initialized
    * def email = utilsJs.randomEmail('vault_key_get_not_init')
    * def password = 'password123'
    * def credentials = { email: '#(email)', password: '#(password)' }

    * def auth = call read(createUserHelper) credentials
    * def accessToken = auth.accessToken

    Given path '/vault/keys'
    And headers utilsJs.bearer(accessToken)
    When method get
    Then status 404


  Scenario: Fail to get vault key material without authentication
    Given path '/vault/keys'
    When method get
    Then status 401


  Scenario: User cannot obtain another account vault key material
    * def password = 'password123'

    * def credentialsA = { email: '#(utilsJs.randomEmail("vault_key_security_A"))', password: '#(password)' }
    * def authA = call read(createUserHelper) credentialsA
    * def tokenA = authA.accessToken
    * call read(initVaultKeyMaterialHelper) { accessToken: '#(tokenA)' }

    * def credentialsB = { email: '#(utilsJs.randomEmail("vault_key_security_B"))', password: '#(password)' }
    * def authB = call read(createUserHelper) credentialsB
    * def tokenB = authB.accessToken

    Given path '/vault/keys'
    And headers utilsJs.bearer(tokenB)
    When method get
    Then status 404
