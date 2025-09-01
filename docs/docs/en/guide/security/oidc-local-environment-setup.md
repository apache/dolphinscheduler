# OIDC Local Development Setup (with Keycloak)

If you are developing or testing the OIDC authentication feature, you'll need a local OIDC provider. This guide explains how to set up **Keycloak** using Docker and configure it for DolphinScheduler development.

## Prerequisites

* You have [Docker](https://www.docker.com/products/docker-desktop/) installed and running.
* You have already cloned the DolphinScheduler repository and can build the project.

## Step 1: Start Keycloak with a Pre-configured Realm

For convenience, we provide a pre-configured Keycloak realm export that sets up the necessary client, users, and groups.

1. **Navigate to the API test resources directory** where the Keycloak configuration is located:

```bash
cd dolphinscheduler-api-test/dolphinscheduler-api-test-case/src/test/resources/docker/oidc-login/
```

2. **Start Keycloak using Docker Compose**:
   The provided `docker-compose.yaml` in this directory is configured to start Keycloak and import the realm automatically.

```bash
docker-compose up -d keycloak
```

This command starts a Keycloak container on port `8081` (to avoid conflicts with other services) and imports `realm-export.json`.

## Step 2: Access and Verify Keycloak

1. Open your browser and navigate to the **Keycloak Admin Console**: `http://localhost:8081`.
2. Log in with username `admin` and password `admin`.
3. In the top-left corner, switch from the `master` realm to the `dolphinscheduler` realm.
4. You can explore **Clients** (`dolphinscheduler-client`), **Users** (`admin_user`, `general_user`), and **Groups** (`dolphinscheduler-admins`) to see the imported configuration.

## Step 3: Configure DolphinScheduler API Server

Modify your `dolphinscheduler-api/src/main/resources/application.yaml` to enable OIDC and connect to your local Keycloak instance.

```yaml
security:
    authentication:
        type: OIDC
        oidc:
            enable: true
            providers:
                keycloak:
                    display-name: "Login with Keycloak"
                    # Point to your local Keycloak realm
                    issuer-uri: http://localhost:8080/realms/dolphinscheduler
                    client-id: dolphinscheduler-client
                    client-secret: dolphinscheduler-client-secret
                    scope: openid, profile, email, groups
                    user-name-attribute: preferred_username
                    groups-claim: groups
            user:
                auto-create: true
                default-tenant-code: "default"
                default-queue: "default"
                user-type: "ADMIN_USER"
```

> **Note**:
> 1. Even though the Keycloak container's external port is `8081`, its internal issuer URL is still based on port `8080`. The configuration in `application.yaml` should use `http://localhost:8080` unless you have modified the issuer URL inside Keycloak itself.
> 2. `scope: openid, profile, email, groups`
> - `openid`: Mandatory for OIDC.
> - `profile`: Often provides claims used for the username, like `preferred_username` or `name`.
> - `email`: Provides the `email` claim.
> - `groups`: A common (but sometimes custom) scope needed to retrieve the user's role/group memberships

## Step 4: Start DolphinScheduler Services

Start the backend services in your IDE as described in the "Normal Mode" guide, ensuring you start at least:
* `MasterServer`
* `ApiApplicationServer`

or,

* Start only the `StandaloneServer` (if you prefer standalone mode)

## Step 5: Start Frontend Server

Run the frontend development server:

```bash
cd dolphinscheduler-ui
pnpm install
pnpm run dev
```

You can now access the UI at `http://localhost:5173`, where you will see the "Login with Keycloak" button.

## Developer Recommendations

It is recommended to set up `pre-commit` before pushing your code to GitHub (see `docs/docs/en/contribute/development-environment-setup.md`). If you encounter issues with `pre-commit`, to avoid CI failures and maintain code quality, always perform the following checks manually:

* **Backend Changes**: Run Spotless to format your Java code.

```bash
./mvnw spotless:apply
```

* **Frontend Changes**: Run the linter to format your TypeScript/Vue code.

```bash
cd dolphinscheduler-ui
pnpm run lint
```

* **Security**: It's highly recommended to use a SonarQube plugin in your IDE to scan for potential security vulnerabilities early.

