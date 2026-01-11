# Package Structure
Updated: 12-01-2026 12:28:46

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
│   ├── [LOCAL] verify acceptance (only).run.xml
│   └── [LOCAL] verify mutationTests (piTest) (only).run.xml
├── docker/
│   └── postgres/
│       └── init-schema.sql
├── docs/
│   ├── architecture/
│   │   ├── decisions/
│   │   │   └── .gitkeep
│   │   ├── tests/
│   │   │   └── architecture_tests_safe_cube_backend.md
│   │   └── safe_cube_backend_manifiesto_v_1.md
│   ├── glossary/
│   │   └── ubiquitous_glossary.md
│   ├── package-structure/
│   │   └── package_structure.md
│   ├── pipeline/
│   │   └── pipeline.md
│   ├── use-cases/
│   │   ├── auth/
│   │   │   ├── auth_tokens_session_strategy_safe_cube_backend_v_1.md
│   │   │   └── auth_use_cases_safe_cube_backend_v_1.md
│   │   ├── user/
│   │   │   └── user_use_cases_safe_cube_backend_v_1.md
│   │   └── vault/
│   │       ├── vault_crypto_strategy_v_1.md
│   │       └── vault_use_cases_safe_cube_backend_v_1.md
│   │   └── README.md
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
│   │   │               │   │   └── WebExceptionHandler.java
│   │   │               │   ├── result/
│   │   │               │   │   └── Result.java
│   │   │               │   └── security/
│   │   │               │       └── SecurityConfig.java
│   │   │               └── SafeCubeBackendApplication.java
│   │   └── resources/
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
│       │   │   │   │   └── PostgreSQLInitializer.java
│       │   │   │   └── IntegrationTest.java
│       │   │   └── com/
│       │   │       └── miguelrodriguez19/
│       │   │           └── safecube/
│       │   │               ├── auth/
│       │   │               │   └── infrastructure/
│       │   │               │       └── persistence/
│       │   │               │           ├── JpaAuthAccountRepositoryAdapterIntegrationTest.java
│       │   │               │           └── JpaRefreshTokenRepositoryAdapterIntegrationTest.java
│       │   │               └── SafeCubeBackendApplicationIntegrationTest.java
│       │   └── unit/
│       │       ├── annotation/
│       │       │   └── UnitTest.java
│       │       └── com/
│       │           └── miguelrodriguez19/
│       │               └── safecube/
│       │                   └── auth/
│       │                       ├── application/
│       │                       │   └── usecase/
│       │                       │       ├── AuthenticateAccountUseCaseTest.java
│       │                       │       ├── IssueTokensUseCaseTest.java
│       │                       │       ├── LogoutUseCaseTest.java
│       │                       │       ├── RefreshTokensUseCaseTest.java
│       │                       │       └── RegisterAccountUseCaseTest.java
│       │                       ├── domain/
│       │                       │   └── model/
│       │                       │       └── AuthAccountTest.java
│       │                       └── infrastructure/
│       │                           ├── crypto/
│       │                           │   └── BCryptPasswordHasherTest.java
│       │                           ├── persistence/
│       │                           │   ├── mapper/
│       │                           │   │   └── AuthAccountMapperTest.java
│       │                           │   ├── JpaAuthAccountRepositoryAdapterTest.java
│       │                           │   └── JpaRefreshTokenRepositoryAdapterTest.java
│       │                           └── security/
│       │                               ├── JwtAccessTokenIssuerTest.java
│       │                               ├── JwtAuthenticationFilterTest.java
│       │                               ├── JwtTokenParserTest.java
│       │                               └── RefreshTokenHasherTest.java
│       └── resources/
│           ├── acceptance/
│           │   ├── features/
│           │   │   ├── auth/
│           │   │   │   ├── authentication.feature
│           │   │   │   ├── logout.feature
│           │   │   │   ├── refresh.feature
│           │   │   │   └── register.feature
│           │   │   └── actuatorHealth.feature
│           │   └── resources/
│           │       ├── _helpers/
│           │       │   ├── loginHelper.feature
│           │       │   └── registerHelper.feature
│           │       ├── config/
│           │       │   └── karate-config.js
│           │       └── js/
│           │           └── utils.js
│           ├── application-integration.yml
│           ├── application-jpa.yml
│           ├── archunit.properties
│           └── schema.sql
├── .env.prod
├── .gitattributes
├── .gitignore
├── docker-compose.yml
├── Dockerfile
├── LICENSE
└── pom.xml
```
