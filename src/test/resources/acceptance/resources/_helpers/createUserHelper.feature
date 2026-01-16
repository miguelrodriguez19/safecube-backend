Feature: createUser helper

  Scenario:
    # Register the user
    * call read(registerHelper) credentials

    # Login the user
    * def loginResult = call read(loginHelper) credentials

    # Expose useful values
    * def accessToken = loginResult.response.accessToken
    * def accountId = loginResult.response.accountId
