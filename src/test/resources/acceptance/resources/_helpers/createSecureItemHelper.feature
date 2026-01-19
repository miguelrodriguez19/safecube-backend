Feature: Create secure item helper

  Scenario:
    * def accessToken = karate.get('accessToken')

    # Optional overrides
    * def itemType = karate.get('itemType', 'PASSWORD')
    * def schemaVersion = karate.get('schemaVersion', 1)
    * def displayHint = karate.get('displayHint', 'Default secure item')
    * def payloadRaw = karate.get('payload', '{"secret":"value"}')

    * def payloadBytes = utilsJs.base64(payloadRaw)
    * def requestBody =
      """
      {
        itemType: '#(itemType)',
        schemaVersion: '#(schemaVersion)',
        displayHint: '#(displayHint)',
        payload: '#(payloadBytes)'
      }
      """
    * print 'REQUEST BODY:', requestBody

    Given url baseUrl
    And path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    And header Content-Type = 'application/json'
    And request requestBody

    When method post
    Then status 201
    And print response

    # Expose useful values
    * def itemId = response.itemId
    * def createdAt = response.createdAt
