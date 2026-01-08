@test @health
Feature: Component health

  Background:
    * url baseURL


  Scenario: Check if application has started OK

    Given path '/actuator/health'
    And request ""

    When method get

    Then print response
    And match response.status == 'UP'
    And status 200
