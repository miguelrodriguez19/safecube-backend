# Pipeline

## Base Structure

```
.github/
├── workflows/
│   ├── ci-java-reusable.yml           # Reusable workflow: Java CI base (build, test, optional scans)
│   │   ├── on: workflow_call
│   │   ├── inputs:
│   │   │   ├── run-version-check:true      # Version review
│   │   │   ├── run-security-scan:false     # Dependency review
│   │   │   ├── run-code-scan:false         # CodeQL
│   │   │   ├── run-acceptance-tests:true   # Control acceptance tests execution
│   │   │   └── run-mutation-tests:true     # Control mutation test execution
│   │   └── jobs:
│   │       ├── build-test
│   │       │   ├── 1. Checkout
│   │       │   ├── 2. Set up java + cache (composite java-steps)
│   │       │   ├── 3. mvn spotless:check
│   │       │   ├── 4. mvn verify (adds -Pacceptance if run-acceptance-tests)
│   │       │   ├── 5. mvn -Pmutation (if run-mutation-tests)
│   │       │   ├── 6. Upload reports (JaCoCo & Spotless)
│   │       │   └── 7. Upload executable JAR
│   │       ├── version-guard (if run-version-check)
│   │       │   └── ./scripts/check-version.sh
│   │       ├── dependency-scan (if run-security-scan)
│   │       │   └── dependency-review-action
│   │       └── codeql-scan (if run-code-scan)
│   │           └── github/codeql-action (init → autobuild → analyze)
│   │
│   ├── pr-checks.yml             # Pull Request quality gate
│   │   ├── on: pull_request
│   │   │   └── branches: [ main ]
│   │   └── jobs:
│   │       ├── ci
│   │       │   └── uses: ci-java-reusable.yml
│   │       │       └── with:
│   │       │           ├── run-version-check: true
│   │       │           ├── run-security-scan: true
│   │       │           └── run-code-scan: true
│   │       ├── docs-consistency-check
│   │       │   └── ./scripts/check-package-structure.sh
│   │       └── secret-scan
│   │           └── GitHub native secret scanning (repo-level config)
│   │
│   └── release-main.yml            # Release + Container publish + Deploy
│       ├── on: push
│       │   └── branches: [ main ]
│       ├── concurrency: main-release
│       └── jobs:
│           ├── ci-lite
│           │   └── uses: ci-java-reusable.yml
│           │       └── with:
│           │           ├── run-version-check: true
│           │           ├── run-security-scan: false
│           │           ├── run-code-scan: false
│           │           ├── run-acceptance-tests: false
│           │           └── run-mutation-tests: false
│           │
│           ├── release
│           │   ├── needs: ci-lite
│           │   └── steps:
│           │       ├── 1. checkout
│           │       ├── 2. Set up Git identity
│           │       ├── 3. Set up java + cache (composite java-steps)
│           │       ├── 4. Read and clean (-SNAPSHOT) version from pom.xml
│           │       ├── 5. Create Git tag
│           │       ├── 6. Log in to GHCR
│           │       ├── 7. Download executable JAR
│           │       └── 8. Build & push Docker image (ghcr.io/miguelrodriguez19/safecube-backend)
│           │
│           └── deploy-production
│               ├── needs: release
│               ├── environment: production
│               └── steps: Deploy on Koyeb
│                   ├── Install Koyeb CLI
│                   └── Deploy ghcr.io:latest with secrets.KOYEB_API_TOKEN 
│
├── actions/
│   └── java-steps/
│       └── action.yml                 # setup java + ~/.m2 cache
│
└── scripts/
    ├── check-version.sh               # Checks version bump (ignores -SNAPSHOT)
    └── check-package-structure.sh     # Verifies docs/package_structure.md timestamp
```

---

## Notes

### Naming

* Workflows naming has to be **explicit and intention-revealing**.

### Manual Deploy Authorization (Koyeb)

* Deploy job uses **GitHub Environments** with `production`.
* Environment protection rules enforce **manual approval** before deployment.
* This is a deliberate early-stage control mechanism.
* Once MVP is delivered, deploy can be fully automated by removing the approval rule.
