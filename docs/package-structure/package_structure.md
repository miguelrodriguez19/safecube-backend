# Package Structure
Updated: 16-01-2026 07:55:19

```
safecube-backend/
├── .github/
│   ├── actions/
│   │   └── java-steps/
│   │       └── action.yml
│   ├── scripts/
│   │   ├── check-package-structure.sh
│   │   └── check-version.sh
│   └── workflows/
│       ├── ci-java-reusable.yml
│       ├── pr_checks.yml
│       └── release-main.yml
├── .run/
│   ├── [LOCAL] clean verify (acceptance).run.xml
│   ├── [LOCAL] clean verify.run.xml
│   ├── [LOCAL] run acceptance (only).run.xml
│   └── [LOCAL] run mutationTests (piTest) (only).run.xml
├── docker/
│   └── postgres/
│       └── init-schema.sql
├── docs/
│   ├── architecture/
│   │   ├── decisions/
│   │   │   ├── adr-001-auth-authentication-and-session.md
│   │   │   └── adr-002-user-profile-lifecycle.md
│   │   ├── tests/
│   │   │   └── architecture_tests_safe_cube_backend.md
│   │   │   │   ├── database_strategy.md
│   │   └── safe_cube_backend_manifiesto_v_1.md
│   ├── glossary/
│   │   └── ubiquitous_glossary.md
│   ├── package-structure/
│   │   └── package_structure.md
│   ├── pipeline/
│   │   └── pipeline.md
│   ├── use-cases/
│   │   ├── auth/
│   │   │   ├── archive/
│   │   │   │   ├── auth_tokens_session_strategy_safe_cube_backend_v_1.md
│   │   │   │   └── auth_use_cases_safe_cube_backend_v_1.md
│   │   │   └── auth_slice_design_safe_cube_backend_v_1.md
│   │   ├── user/
│   │   │   └── user_use_cases_safe_cube_backend_v_1.md
│   │   ├── vault/
│   │   │   ├── vault_crypto_strategy_v_1.md
│   │   │   └── vault_use_cases_safe_cube_backend_v_1.md
│   │   │   │   └── README.md
├── scripts/
│   │   ├── resources/
│   │   └── com/
│   │       └── safecube/
│   │           └── tooling/
│   │               └── FolderTreeToFile.java
│   └── run-folder-tree.sh
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── miguelrodriguez19/
│   │   │           └── safecube/
│   │   │               ├── auth/
│   │   │               │   ├── application/
│   │   │               │   │   ├── dto/
│   │   │               │   │   │   ├── AuthenticateAccountCommand.java
│   │   │               │   │   │   ├── AuthenticateAccountResult.java
│   │   │               │   │   │   ├── IssuedTokensResult.java
│   │   │               │   │   │   ├── RegisterAccountCommand.java
│   │   │               │   │   │   └── RegisterAccountResult.java
│   │   │               │   │   ├── error/
│   │   │               │   │   │   └── AuthError.java
│   │   │               │   │   ├── port/
│   │   │               │   │   │   └── out/
│   │   │               │   │   │       ├── AccessTokenIssuer.java
│   │   │               │   │   │       ├── AuthAccountRepository.java
│   │   │               │   │   │       ├── PasswordHasher.java
│   │   │               │   │   │       ├── RefreshTokenRecord.java
│   │   │               │   │   │       └── RefreshTokenRepository.java
│   │   │               │   │   └── usecase/
│   │   │               │   │       ├── AuthenticateAccountUseCase.java
│   │   │               │   │       ├── IssueTokensUseCase.java
│   │   │               │   │       ├── LogoutUseCase.java
│   │   │               │   │       ├── RefreshTokensUseCase.java
│   │   │               │   │       └── RegisterAccountUseCase.java
│   │   │               │   ├── domain/
│   │   │               │   │   └── model/
│   │   │               │   │       └── AuthAccount.java
│   │   │               │   └── infrastructure/
│   │   │               │       ├── crypto/
│   │   │               │       │   └── BCryptPasswordHasher.java
│   │   │               │       ├── exception/
│   │   │               │       │   └── CryptoHashingException.java
│   │   │               │       ├── persistence/
│   │   │               │       │   ├── jpa/
│   │   │               │       │   │   ├── AuthAccountJpaEntity.java
│   │   │               │       │   │   ├── AuthAccountJpaRepository.java
│   │   │               │       │   │   ├── RefreshTokenJpaEntity.java
│   │   │               │       │   │   └── RefreshTokenJpaRepository.java
│   │   │               │       │   ├── mapper/
│   │   │               │       │   │   └── AuthAccountMapper.java
│   │   │               │       │   ├── JpaAuthAccountRepositoryAdapter.java
│   │   │               │       │   └── JpaRefreshTokenRepositoryAdapter.java
│   │   │               │       ├── security/
│   │   │               │       │   ├── JwtAccessTokenIssuer.java
│   │   │               │       │   ├── JwtAuthenticationFilter.java
│   │   │               │       │   ├── JwtTokenParser.java
│   │   │               │       │   └── RefreshTokenHasher.java
│   │   │               │       └── web/
│   │   │               │           ├── dto/
│   │   │               │           │   ├── AuthenticateAccountRequest.java
│   │   │               │           │   ├── AuthTokensResponse.java
│   │   │               │           │   ├── RefreshTokenRequest.java
│   │   │               │           │   └── RegisterAccountRequest.java
│   │   │               │           └── AuthController.java
│   │   │               ├── shared/
│   │   │               │   ├── exception/
│   │   │               │   │   ├── DomainException.java
│   │   │               │   │   ├── InfrastructureException.java
│   │   │               │   │   └── WebExceptionHandler.java
│   │   │               │   ├── result/
│   │   │               │   │   ├── Result.java
│   │   │               │   │   └── Void.java
│   │   │               │   └── security/
│   │   │               │       └── SecurityConfig.java
│   │   │               ├── user/
│   │   │               │   ├── application/
│   │   │               │   │   ├── dto/
│   │   │               │   │   │   ├── CreateUserProfileCommand.java
│   │   │               │   │   │   ├── DeleteUserProfileCommand.java
│   │   │               │   │   │   ├── UpdateUserProfileCommand.java
│   │   │               │   │   │   └── UserProfileResponse.java
│   │   │               │   │   ├── error/
│   │   │               │   │   │   └── UserError.java
│   │   │               │   │   ├── mapper/
│   │   │               │   │   │   └── UserProfileResponseMapper.java
│   │   │               │   │   ├── port/
│   │   │               │   │   │   └── out/
│   │   │               │   │   │       ├── AccountExistencePort.java
│   │   │               │   │   │       └── UserProfileRepository.java
│   │   │               │   │   └── usecase/
│   │   │               │   │       ├── CreateUserProfileUseCase.java
│   │   │               │   │       ├── GetUserProfileUseCase.java
│   │   │               │   │       └── UpdateUserProfileUseCase.java
│   │   │               │   ├── domain/
│   │   │               │   │   ├── exception/
│   │   │               │   │   │   └── InvalidDisplayNameException.java
│   │   │               │   │   └── model/
│   │   │               │   │       └── UserProfile.java
│   │   │               │   └── infrastructure/
│   │   │               │       ├── persistence/
│   │   │               │       │   ├── jpa/
│   │   │               │       │   │   ├── UserProfileJpaEntity.java
│   │   │               │       │   │   └── UserProfileJpaRepository.java
│   │   │               │       │   ├── mapper/
│   │   │               │       │   │   └── UserProfileMapper.java
│   │   │               │       │   ├── AccountExistenceAuthAdapter.java
│   │   │               │       │   └── JpaUserProfileRepositoryAdapter.java
│   │   │               │       └── web/
│   │   │               │           ├── dto/
│   │   │               │           │   ├── CreateUserProfileRequest.java
│   │   │               │           │   └── UpdateUserProfileRequest.java
│   │   │               │           └── UserProfileController.java
│   │   │               └── SafeCubeBackendApplication.java
│   │   └── resources/
│   │       ├── database/
│   │       ├── static/
│   │       ├── templates/
│   │       └── application.yaml
│   └── test/
│       ├── java/
│       │   ├── acceptance/
│       │   │   └── RunAcceptanceTest.java
│       │   ├── architecture/
│       │   │   ├── application/
│       │   │   │   ├── ApplicationDependencyTest.java
│       │   │   │   └── UseCaseContractTest.java
│       │   │   ├── domain/
│       │   │   │   ├── DomainIndependenceTest.java
│       │   │   │   └── DomainPurityTest.java
│       │   │   ├── infrastructure/
│       │   │   │   ├── AdapterImplementationTest.java
│       │   │   │   └── ControllerIsolationTest.java
│       │   │   ├── shared/
│       │   │   │   └── NamingAndConventionsTest.java
│       │   │   ├── slices/
│       │   │   │   └── SliceIsolationTest.java
│       │   │   └── support/
│       │   │       ├── ArchitectureConstants.java
│       │   │       └── ArchUnitConditions.java
│       │   ├── integration/
│       │   │   ├── annotation/
│       │   │   │   ├── support/
│       │   │   │   │   └── PostgresSQLInitializer.java
│       │   │   │   └── IntegrationTest.java
│       │   │   └── com/
│       │   │       └── miguelrodriguez19/
│       │   │           └── safecube/
│       │   │               ├── auth/
│       │   │               │   └── infrastructure/
│       │   │               │       └── persistence/
│       │   │               │           ├── JpaAuthAccountRepositoryAdapterIntegrationTest.java
│       │   │               │           └── JpaRefreshTokenRepositoryAdapterIntegrationTest.java
│       │   │               ├── user/
│       │   │               │   └── infrastructure/
│       │   │               │       └── persistence/
│       │   │               │           └── JpaUserProfileRepositoryAdapterIntegrationTest.java
│       │   │               └── SafeCubeBackendApplicationIntegrationTest.java
│       │   └── unit/
│       │       ├── annotation/
│       │       │   └── UnitTest.java
│       │       └── com/
│       │           └── miguelrodriguez19/
│       │               └── safecube/
│       │                   ├── auth/
│       │                   │   ├── application/
│       │                   │   │   └── usecase/
│       │                   │   │       ├── AuthenticateAccountUseCaseTest.java
│       │                   │   │       ├── IssueTokensUseCaseTest.java
│       │                   │   │       ├── LogoutUseCaseTest.java
│       │                   │   │       ├── RefreshTokensUseCaseTest.java
│       │                   │   │       └── RegisterAccountUseCaseTest.java
│       │                   │   ├── domain/
│       │                   │   │   └── model/
│       │                   │   │       └── AuthAccountTest.java
│       │                   │   └── infrastructure/
│       │                   │       ├── crypto/
│       │                   │       │   └── BCryptPasswordHasherTest.java
│       │                   │       ├── persistence/
│       │                   │       │   ├── mapper/
│       │                   │       │   │   └── AuthAccountMapperTest.java
│       │                   │       │   ├── JpaAuthAccountRepositoryAdapterTest.java
│       │                   │       │   └── JpaRefreshTokenRepositoryAdapterTest.java
│       │                   │       └── security/
│       │                   │           ├── JwtAccessTokenIssuerTest.java
│       │                   │           ├── JwtAuthenticationFilterTest.java
│       │                   │           ├── JwtTokenParserTest.java
│       │                   │           └── RefreshTokenHasherTest.java
│       │                   ├── shared/
│       │                   │   └── result/
│       │                   │       └── ResultTest.java
│       │                   └── user/
│       │                       ├── application/
│       │                       │   └── usecase/
│       │                       │       ├── CreateUserProfileUseCaseTest.java
│       │                       │       ├── GetUserProfileUseCaseTest.java
│       │                       │       └── UpdateUserProfileUseCaseTest.java
│       │                       ├── domain/
│       │                       │   └── model/
│       │                       │       └── UserProfileTest.java
│       │                       └── infrastructure/
│       │                           └── persistence/
│       │                               ├── AccountExistenceAuthAdapterTest.java
│       │                               └── JpaUserProfileRepositoryAdapterTest.java
│       └── resources/
│           ├── acceptance/
│           │   ├── features/
│           │   │   ├── auth/
│           │   │   │   ├── authentication.feature
│           │   │   │   ├── logout.feature
│           │   │   │   ├── refresh.feature
│           │   │   │   └── register.feature
│           │   │   ├── user/
│           │   │   │   ├── user-create.feature
│           │   │   │   ├── user-get.feature
│           │   │   │   ├── user-security.feature
│           │   │   │   └── user-update.feature
│           │   │   └── actuatorHealth.feature
│           │   └── resources/
│           │       ├── _helpers/
│           │       │   ├── createUserHelper.feature
│           │       │   ├── loginHelper.feature
│           │       │   └── registerHelper.feature
│           │       ├── config/
│           │       │   └── karate-config.js
│           │       └── js/
│           │           └── utils.js
│           ├── autogenerated/
│           │   └── info.md
│           ├── application-integration.yml
│           ├── application-jpa.yml
│           └── archunit.properties
├── .env.prod
├── .gitattributes
├── .gitignore
├── docker-compose.yml
├── Dockerfile
├── LICENSE
└── pom.xml
```
