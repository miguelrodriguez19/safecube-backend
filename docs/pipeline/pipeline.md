# Pipeline

## Base Structure (Updated)

```
.github/
├── workflows/
│   ├── ci-java-reusable.yml        # Reusable workflow: Java CI base (build, test, optional scans)
│   │   ├── on: workflow_call
│   │   ├── inputs:
│   │   │   ├── run-version-check   # Version review
│   │   │   ├── run-security-scan   # Dependency review
│   │   │   └── run-code-scan       # CodeQL
│   │   └── jobs:
│   │       ├── build-test
│   │       │   ├── 1. Checkout
│   │       │   ├── 2. setup-java + cache (composite java-steps)
│   │       │   ├── 3. mvn spotless:check
│   │       │   └── 4. mvn verify (includes unit, application mutation and acceptance tests)
│   │       ├── version-guard (if run-version-check)
│   │       │   └── ./scripts/check-version.sh
│   │       ├── dependency-scan (if run-security-scan)
│   │       │   └── dependency-review-action
│   │       └── codeql-scan (if run-code-scan)
│   │           └── github/codeql-action (init → autobuild → analyze)
│   │
│   ├── pr-checks.yml               # Pull Request quality gate
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
│           │           └── run-code-scan: false
│           │
│           ├── release
│           │   ├── needs: ci-lite
│           │   ├── checkout
│           │   ├── java-steps
│           │   ├── mvn -B release:prepare -DskipTests
│           │   ├── mvn -B release:perform -DskipTests
│           │   ├── docker build (tags: vX.Y.Z, latest)
│           │   └── docker push ghcr.io/<owner>/safecube-backend
│           │
│           └── deploy-render
│               ├── needs: release
│               ├── environment: production   # Manual approval required
│               └── curl -X POST ${{ secrets.RENDER_DEPLOY_HOOK }}
│
├── actions/
│   └── java-steps/
│       └── java-setup.yml              # setup java + ~/.m2 cache
│
└── scripts/
    ├── check-version.sh               # Checks version bump (ignores -SNAPSHOT)
    └── check-package-structure.sh     # Verifies docs/package_structure.md timestamp
```

---

## Design Decisions

### Naming

* Workflows naming has to be **explicit and intention-revealing**.

### Manual Deploy Authorization (Render)

* Deploy job uses **GitHub Environments** with `production`.
* Environment protection rules enforce **manual approval** before deployment.
* This is a deliberate early-stage control mechanism.
* Once MVP is delivered, deploy can be fully automated by removing the approval rule.

### Container Registry (GHCR)

* GitHub Container Registry is used instead of Docker Hub.
* GHCR is **free** for public and private repositories.
* Authentication handled via `GITHUB_TOKEN`.
* Improves integration and reduces external dependencies.

### Documentation Consistency Check

* A PR check ensures `package-structure.txt` is up to date.
* File includes a `last-updated:` timestamp.
* Script fails if timestamp or structure is outdated.
* Prevents silent documentation drift without auto-committing from CI.

---

## Notes

* Automated documentation commits are intentionally excluded in this phase.
* CI is executed again on `main` (lite version) to avoid trusting PR state.
* Security and CodeQL scans are considered mandatory only at PR level.
* Pipeline aligns with GitHub Flow and supports future self-hosted deployments.
