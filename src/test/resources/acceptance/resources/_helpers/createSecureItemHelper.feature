Feature: Create secure item helper

  Scenario:
    * def accessToken = karate.get('accessToken')

    # Optional overrides
    * def itemType = karate.get('itemType', 'PASSWORD')
    * def schemaVersion = karate.get('schemaVersion', 1)
    * def displayHint = karate.get('displayHint', 'Default secure item')
    * def payloadRaw = karate.get('payload', '{"secret":"value"}')
    * def payloadVersion = karate.get('payloadVersion', 1)
    * def mutationId = karate.get('__arg.mutationId', utilsJs.uuid())

    * def payloadBytes = utilsJs.base64(payloadRaw)
    * def requestBody =
      """
      {
        itemType: '#(itemType)',
        schemaVersion: '#(schemaVersion)',
        displayHint: '#(displayHint)',
        payload: '#(payloadBytes)',
        payloadVersion: '#(payloadVersion)'
      }
      """
    * print 'REQUEST BODY:', requestBody

    Given url baseUrl
    And path '/vault/items'
    And headers utilsJs.bearer(accessToken)
    And header Idempotency-Key = mutationId
    And header Content-Type = 'application/json'
    And request requestBody

    When method post
    Then status 201
    And print response
    And match response ==
      """
      {
        itemId: '#uuid',
        mutationId: '#(mutationId)',
        payloadVersion: '#(payloadVersion)',
        itemRevision: 1,
        changeSequence: '#number',
        updatedAt: '#string'
      }
      """
    And match responseHeaders['ETag'][0] == utilsJs.etag(response.itemRevision)

    # Expose useful values
    * def itemId = response.itemId
    * def updatedAt = response.updatedAt
    * def itemRevision = response.itemRevision
    * def changeSequence = response.changeSequence
    * def responseMutationId = response.mutationId
    * def responsePayloadVersion = response.payloadVersion
