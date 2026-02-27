# Contributing to Nicked

Thanks for your interest in contributing! Here's everything you need to know.

---

## Reporting Bugs

Use the [Bug Report](https://github.com/PharoGames/Nicked/issues/new?template=bug_report.yml) issue template.

Before opening a bug report:

- Check the [documentation](https://pharogames.github.io/Nicked) — particularly the [Getting Started](https://pharogames.github.io/Nicked/getting-started/) and [Troubleshooting](https://pharogames.github.io/Nicked/getting-started/#troubleshooting) sections.
- Search [existing issues](https://github.com/PharoGames/Nicked/issues) to see if it has already been reported.

## Suggesting Features

Use the [Feature Request](https://github.com/PharoGames/Nicked/issues/new?template=feature_request.yml) issue template. Describe the use case clearly — feature requests with a clear motivation are much more likely to be picked up.

## Reporting Security Vulnerabilities

Do **not** open a public issue for security vulnerabilities. Use [GitHub's private vulnerability reporting](https://github.com/PharoGames/Nicked/security/advisories/new) instead. See [SECURITY.md](../SECURITY.md) for details.

---

## Pull Requests

Pull requests are welcome. Maintainers have final say on what gets merged, but good contributions will be reviewed and considered.

### Before You Start

- Open an issue first for non-trivial changes so we can discuss the approach before you invest time coding it.
- For small fixes (typos, obvious bugs), a PR without a prior issue is fine.

### Setup

1. Fork the repo and clone your fork.
2. Make sure you have **Java 21** installed.
3. Build the project to confirm everything works:
   ```bash
   ./gradlew build
   ```

### Code Style

- Follow the existing code structure and naming conventions.
- Keep methods short and focused — one method, one responsibility.
- Use `Objects.requireNonNull()` for null checks on parameters.
- Avoid empty catch blocks; log and rethrow or handle explicitly.
- No magic numbers — use named constants.
- Add Javadoc to any public API methods you introduce.

### Submitting

1. Create a branch with a descriptive name (e.g. `fix/skin-not-updating`, `feat/nick-cooldown`).
2. Make your changes and ensure `./gradlew build` passes.
3. Open a pull request against the `main` branch.
4. Fill in the pull request template completely.

---

## Code of Conduct

By participating in this project, you agree to abide by the [Code of Conduct](../CODE_OF_CONDUCT.md).
