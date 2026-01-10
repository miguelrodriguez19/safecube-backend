Feature: Register helper

  Scenario:
    Given url baseUrl
    And path '/auth/register'
    And header Content-Type = 'application/json'
    And request { email: '#(email)', password: '#(password)' }

    When method post
    Then print response
    And status 200