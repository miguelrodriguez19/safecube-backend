# Package Structure
Updated: 07-01-2026 08:28:44

```
safecube-backend/
├── .github/
│   ├── actions/
│   │   └── java-steps/
│   │       └── java-setup.yml
│   ├── scripts/
│   │   ├── check-package-structure.sh
│   │   └── check-version.sh
│   └── workflows/
│       ├── ci-java-reusable.yml
│       ├── pr_checks.yml
│       └── release-main.yml
├── .run/
│   ├── [CI] clean verify (acceptance, mutation).run.xml
│   ├── [LOCAL] clean verify (acceptance).run.xml
│   ├── [LOCAL] clean verify.run.xml
│   └── [LOCAL] verify mutationTests (piTest).run.xml
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
│   │   │               └── SafeCubeBackendApplication.java
│   │   └── resources/
│   │       ├── static/
│   │       ├── templates/
│   │       └── application.yaml
│   └── test/
│       ├── java/
│       │   ├── acceptance/
│       │   │   └── RunAcceptanceTest.java
│       │   ├── achitecture/
│       │   │   └── .gitkeep
│       │   ├── integration/
│       │   │   ├── annotation/
│       │   │   │   ├── support/
│       │   │   │   │   └── PostgreSQLInitializer.java
│       │   │   │   └── IntegrationTest.java
│       │   │   └── com/
│       │   │       └── miguelrodriguez19/
│       │   │           └── safecube/
│       │   │               └── SafeCubeBackendApplicationIntegrationTest.java
│       │   └── unit/
│       │       └── annotation/
│       │           └── UnitTest.java
│       └── resources/
│           ├── acceptance/
│           │   ├── features/
│           │   │   └── actuatorHealth.feature
│           │   └── resources/
│           │       └── config/
│           │           └── karate-config.js
│           ├── application-integration.yml
│           └── application-jpa.yml
├── .gitattributes
├── .gitignore
├── docker-compose.yml
├── Dockerfile
├── LICENSE
└── pom.xml
```
