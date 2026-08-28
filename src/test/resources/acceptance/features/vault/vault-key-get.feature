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
    And match responseHeaders['ETag'][0] == '"master-1"'
    And match responseHeaders['Cache-Control'][0] == 'no-store, no-transform'
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


  Scenario: Generated OpenAPI declares master key preconditions
    Given path '/v3/api-docs'
    When method get
    Then status 200
    * def getOperation = response.paths['/vault/keys'].get
    * def updateOperation = response.paths['/vault/keys/master'].put
    And match getOperation.responses['200'].headers.ETag.required == true
    And match getOperation.responses['200'].headers['Cache-Control'].required == true
    And match updateOperation.responses['200'].headers.ETag.required == true
    And match updateOperation.responses['200'].headers['Cache-Control'].required == true
    And match updateOperation.responses['412'] == '#notnull'
    And match updateOperation.responses['428'] == '#notnull'
    And match updateOperation.parameters[0].name == 'If-Match'
    And match updateOperation.parameters[0].in == 'header'
    And match updateOperation.parameters[0].required == true


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
