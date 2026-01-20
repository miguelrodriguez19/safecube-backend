Feature: Helper - initialize vault key material

  Scenario: init vault key material
    * def accessToken = __arg.accessToken

    * def kekMaster = utilsJs.base64('kek-master')
    * def kekRecovery = utilsJs.base64('kek-recovery')
    * def salt = utilsJs.base64('salt')

    Given url baseUrl
    And path '/vault/keys'
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
        cryptoVersion: 'v1'
      }
      """
    When method post
    Then status 201
