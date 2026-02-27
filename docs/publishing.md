# Publishing to Modrinth

Nicked uses [Minotaur](https://github.com/modrinth/minotaur), Modrinth's official Gradle plugin, to automate version publishing. Releases are published automatically via GitHub Actions whenever a `v*` tag is pushed.

---

## How It Works

```
git tag v1.2.3
git push origin v1.2.3
```

This triggers the `Build` workflow, which:

1. Builds and tests the project.
2. Creates a GitHub Release with the compiled JAR.
3. Publishes the version to Modrinth (name, version number, game versions, loaders, changelog, and project body are all set automatically).

The Modrinth project body is synced from [`MODRINTH.md`](../MODRINTH.md) on every publish.

---

## One-Time Modrinth Setup

Before the first automated publish you must create the Modrinth project manually and configure the required secrets.

### 1. Create the Modrinth project

Go to [modrinth.com/mod/create](https://modrinth.com/mod/create) and fill in:

| Field | Value |
|---|---|
| Name | `Nicked` |
| Project type | `Plugin` |
| Summary | `Packet-level nickname plugin with skin changing, developer API, and PlaceholderAPI support.` |
| License | `MIT` |
| Client-side | `Unsupported` |
| Server-side | `Required` |

### 2. Upload the icon

Use `images/logo.png` as the project icon.

### 3. Add a gallery image

Upload `images/banner.png` as the featured gallery image (shown at the top of the project page).

### 4. Set project links

In the project settings under **Links**, set:

| Link | URL |
|---|---|
| Source | `https://github.com/PharoGames/Nicked` |
| Issues | `https://github.com/PharoGames/Nicked/issues` |
| Wiki / Docs | `https://pharogames.github.io/Nicked` |
| Discord | `https://discord.gg/7eQt8sQ8at` |

### 5. Set categories / tags

Select the following tags on the project page:

- **Utility**
- **Management**

### 6. Create a Personal Access Token

Go to [modrinth.com/settings/pats](https://modrinth.com/settings/pats) and create a token with:

- `CREATE_VERSION` scope
- `PROJECT_WRITE` scope

Copy the token — you will not see it again.

### 7. Add the token as a GitHub secret

In your GitHub repository go to **Settings → Secrets and variables → Actions → New repository secret**:

| Name | Value |
|---|---|
| `MODRINTH_TOKEN` | The PAT from step 6 |

### 8. Set the project ID in `build.gradle`

After creating the project, copy the project ID from Modrinth (project page → ⋮ → Copy ID) and replace the placeholder in `build.gradle`:

```groovy
modrinth {
    projectId = "YOUR_PROJECT_ID"   // replace PLACEHOLDER
    // ...
}
```

Commit and push this change before tagging your first release.

---

## Local Testing (Debug Mode)

To preview what Minotaur would upload without actually publishing:

```bash
MODRINTH_TOKEN=mrp_yourtoken ./gradlew modrinth --debug
```

Or enable `debugMode` temporarily in `build.gradle`:

```groovy
modrinth {
    debugMode = true
    // ...
}
```

This prints the full payload to the console without making any API calls.
