@test @vault @sync-protocol
Feature: Secure item synchronization protocol

  Background:
    * url baseUrl
    * header Content-Type = 'application/json'


  Scenario: CREATE is idempotent and rejects reuse with different content
    * def email = utilsJs.randomEmail('sec_item_protocol_create_idempotency')
    * def auth = call read(createUserHelper) { email: '#(email)', password: 'password123' }
    * def mutationId = utilsJs.uuid()
    * def payload = utilsJs.base64('{"secret":"value"}')
    * def requestBody =
      """
      {
        itemType: 'PASSWORD',
        schemaVersion: 1,
        displayHint: 'Idempotent create',
        payload: '#(payload)',
        payloadVersion: 1
      }
      """

    Given path '/vault/items'
    And headers utilsJs.bearer(auth.accessToken)
    And header Idempotency-Key = mutationId
    And request requestBody
    When method post
    Then status 201
    * def originalResponse = response
    * def originalEtag = responseHeaders['ETag'][0]

    Given path '/vault/items'
    And headers utilsJs.bearer(auth.accessToken)
    And header Idempotency-Key = mutationId
    And request requestBody
    When method post
    Then status 201
    And match response == originalResponse
    And match responseHeaders['ETag'][0] == originalEtag

    * set requestBody.displayHint = 'Different content'
    Given path '/vault/items'
    And headers utilsJs.bearer(auth.accessToken)
    And header Idempotency-Key = mutationId
    And request requestBody
    When method post
    Then status 409

    Given path '/vault/items'
    And headers utilsJs.bearer(auth.accessToken)
    When method get
    Then status 200
    And match response.items == '#[1]'


  Scenario: UPDATE retry is stable and stale revisions are rejected
    * def email = utilsJs.randomEmail('sec_item_protocol_update')
    * def auth = call read(createUserHelper) { email: '#(email)', password: 'password123' }
    * def item = call read(createSecureItemHelper) { accessToken: '#(auth.accessToken)' }
    * def mutationId = utilsJs.uuid()
    * def nextPayload = utilsJs.base64('{"secret":"updated"}')
    * def updateBody =
      """
      {
        itemType: 'PASSWORD',
        schemaVersion: 2,
        displayHint: 'Updated once',
        payload: '#(nextPayload)',
        payloadVersion: 2
      }
      """

    Given path '/vault/items', item.itemId
    And headers utilsJs.bearer(auth.accessToken)
    And header Idempotency-Key = mutationId
    And header If-Match = utilsJs.etag(item.itemRevision)
    And request updateBody
    When method put
    Then status 200
    And match response.itemRevision == 2
    And match response.changeSequence == 2
    * def originalResponse = response
    * def originalEtag = responseHeaders['ETag'][0]

    Given path '/vault/items', item.itemId
    And headers utilsJs.bearer(auth.accessToken)
    And header Idempotency-Key = mutationId
    And header If-Match = utilsJs.etag(item.itemRevision)
    And request updateBody
    When method put
    Then status 200
    And match response == originalResponse
    And match responseHeaders['ETag'][0] == originalEtag

    Given path '/vault/items', item.itemId
    And headers utilsJs.bearer(auth.accessToken)
    And header Idempotency-Key = utilsJs.uuid()
    And header If-Match = utilsJs.etag(item.itemRevision)
    And request updateBody
    When method put
    Then status 412

    * set updateBody.displayHint = 'Mutation key conflict'
    Given path '/vault/items', item.itemId
    And headers utilsJs.bearer(auth.accessToken)
    And header Idempotency-Key = mutationId
    And header If-Match = utilsJs.etag(item.itemRevision)
    And request updateBody
    When method put
    Then status 409

    Given path '/vault/items', item.itemId
    And headers utilsJs.bearer(auth.accessToken)
    When method get
    Then status 200
    And match response.itemRevision == 2
    And match response.changeSequence == 2
    And match responseHeaders['ETag'][0] == '"2"'


  Scenario: Required mutation headers return explicit client errors
    * def email = utilsJs.randomEmail('sec_item_protocol_headers')
    * def auth = call read(createUserHelper) { email: '#(email)', password: 'password123' }
    * def item = call read(createSecureItemHelper) { accessToken: '#(auth.accessToken)' }
    * def updateBody =
      """
      {
        itemType: 'PASSWORD',
        schemaVersion: 2,
        displayHint: 'Header validation',
        payload: '#(utilsJs.base64("updated"))',
        payloadVersion: 2
      }
      """

    Given path '/vault/items', item.itemId
    And headers utilsJs.bearer(auth.accessToken)
    And header Idempotency-Key = utilsJs.uuid()
    And request updateBody
    When method put
    Then status 428

    Given path '/vault/items', item.itemId
    And headers utilsJs.bearer(auth.accessToken)
    And header If-Match = utilsJs.etag(item.itemRevision)
    And request updateBody
    When method put
    Then status 400
    And match response.error == 'VALIDATION_FAILED'
    And match response.fields['Idempotency-Key'] == '#present'

    Given path '/vault/items', item.itemId
    And headers utilsJs.bearer(auth.accessToken)
    And header Idempotency-Key = utilsJs.uuid()
    When method delete
    Then status 428

    Given path '/vault/items', item.itemId
    And headers utilsJs.bearer(auth.accessToken)
    And header If-Match = utilsJs.etag(item.itemRevision)
    When method delete
    Then status 400
    And match response.error == 'VALIDATION_FAILED'
    And match response.fields['Idempotency-Key'] == '#present'


  Scenario: Change feed pages complete snapshots and tombstones without skipping changes
    * def email = utilsJs.randomEmail('sec_item_protocol_changes')
    * def auth = call read(createUserHelper) { email: '#(email)', password: 'password123' }
    * def first = call read(createSecureItemHelper) { accessToken: '#(auth.accessToken)', displayHint: 'First' }
    * def second = call read(createSecureItemHelper) { accessToken: '#(auth.accessToken)', displayHint: 'Second' }

    Given path '/vault/items/changes'
    And headers utilsJs.bearer(auth.accessToken)
    And param after = 0
    And param limit = 1
    When method get
    Then status 200
    And match response.items == '#[1]'
    And match response.items[0].itemId == first.itemId
    And match response.items[0].payload == '#present'
    And match response.nextCursor == first.changeSequence
    And match response.hasMore == true

    * def updateMutationId = utilsJs.uuid()
    * def updatedPayload = utilsJs.base64('{"secret":"updated-first"}')
    Given path '/vault/items', first.itemId
    And headers utilsJs.bearer(auth.accessToken)
    And header Idempotency-Key = updateMutationId
    And header If-Match = utilsJs.etag(first.itemRevision)
    And request
      """
      {
        itemType: 'PASSWORD',
        schemaVersion: 2,
        displayHint: 'First updated',
        payload: '#(updatedPayload)',
        payloadVersion: 2
      }
      """
    When method put
    Then status 200
    * def updatedFirst = response

    Given path '/vault/items/changes'
    And headers utilsJs.bearer(auth.accessToken)
    And param after = first.changeSequence
    And param limit = 1
    When method get
    Then status 200
    And match response.items == '#[1]'
    And match response.items[0].itemId == second.itemId
    And match response.nextCursor == second.changeSequence
    And match response.hasMore == true

    Given path '/vault/items/changes'
    And headers utilsJs.bearer(auth.accessToken)
    And param after = second.changeSequence
    And param limit = 1
    When method get
    Then status 200
    And match response.items == '#[1]'
    And match response.items[0].itemId == first.itemId
    And match response.items[0].displayHint == 'First updated'
    And match response.items[0].payload == updatedPayload
    And match response.items[0].itemRevision == updatedFirst.itemRevision
    And match response.nextCursor == updatedFirst.changeSequence
    And match response.hasMore == false

    * def deleteMutationId = utilsJs.uuid()
    Given path '/vault/items', second.itemId
    And headers utilsJs.bearer(auth.accessToken)
    And header Idempotency-Key = deleteMutationId
    And header If-Match = utilsJs.etag(second.itemRevision)
    When method delete
    Then status 200
    * def deletedSecond = response
    * def deleteEtag = responseHeaders['ETag'][0]

    Given path '/vault/items', second.itemId
    And headers utilsJs.bearer(auth.accessToken)
    And header Idempotency-Key = deleteMutationId
    And header If-Match = utilsJs.etag(second.itemRevision)
    When method delete
    Then status 200
    And match response == deletedSecond
    And match responseHeaders['ETag'][0] == deleteEtag

    Given path '/vault/items/changes'
    And headers utilsJs.bearer(auth.accessToken)
    And param after = updatedFirst.changeSequence
    And param limit = 10
    When method get
    Then status 200
    And match response.items == '#[1]'
    And match response.items[0].itemId == second.itemId
    And match response.items[0].payload == '#present'
    And match response.items[0].itemRevision == deletedSecond.itemRevision
    And match response.items[0].changeSequence == deletedSecond.changeSequence
    And match response.items[0].deletedAt == deletedSecond.deletedAt
    And match response.nextCursor == deletedSecond.changeSequence
    And match response.hasMore == false
