# Spring Boot + Keycloak: JWT Resource Server

A Java 17 sample demonstrating JWT validation and role-based access control with:
- Spring Boot 3.5.16
- Spring Security 6.5
- Gradle 8.14.3
- Keycloak 26.8.1.

Postman Desktop acts as the OAuth 2.0 client and uses Authorization Code + PKCE. Keycloak authenticates users and issues
tokens. Spring Boot is a **Resource Server**.

## Requirements

- Java 17 and Docker Desktop
- Gradle Wrapper (included in the repository)
- Postman Desktop for manual verification

## Start Keycloak (local development)

On macOS, in a zsh terminal, run these commands **only when creating the container for the first time**:

```zsh
read -rs "KC_PASSWORD?Keycloak admin password: "; echo

docker run -d \
  --name keycloak-study \
  -p 127.0.0.1:8080:8080 \
  --memory=1g \
  -v keycloak-study-data:/opt/keycloak/data \
  -e KC_BOOTSTRAP_ADMIN_USERNAME=admin \
  -e KC_BOOTSTRAP_ADMIN_PASSWORD="$KC_PASSWORD" \
  quay.io/keycloak/keycloak:26.8.1 start-dev

unset KC_PASSWORD
```

To restart an existing stopped container, use `docker start keycloak-study`. Open `http://localhost:8080/admin/`.

**Development only:** Never use `start-dev` for production. Use a unique development password; Docker environment
variables can be inspected in the local container configuration.

## Configure Keycloak

1. Create the realm `SpringBootKeycloak`.
2. Create a public OIDC client `springboot-keycloak-client`: enable **Standard Flow** and **PKCE (S256)**; disable
   Client Authentication and Direct Access Grants. Register the exact redirect URI shown by Postman Desktop.
3. Create client roles `user` and `admin`.
4. Create composite realm roles: `app_user` includes client role `user`; `app_admin` includes client role `admin`.
5. Create test users: `user1` has `app_user`; `globaladmin` has `app_admin`; `user2` has both.
6. Add a client-specific **Audience** mapper named `api-audience`; set **Included Custom Audience** to
   `springboot-keycloak-api` and enable **Add to access token**.

Do not commit passwords, access tokens, refresh tokens, or real user data.

## Configure and run Spring Boot

The `src/main/resources/application.yml` file contains:

```yaml
server:
  port: 8081
  servlet:
    context-path: /api
spring:
  application:
    name: springboot-keycloak
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/SpringBootKeycloak
          audiences:
            - springboot-keycloak-api
```

From the project root:

```bash
./gradlew build
./gradlew bootRun
```

The Resource Server checks JWT signatures, timestamps, issuer, and audience. A custom `JwtAuthenticationConverter` keeps
`SCOPE_` authorities and maps Keycloak `resource_access.springboot-keycloak-client.roles` to `ROLE_user` and
`ROLE_admin`.

## Endpoints

| Method | Endpoint              | Required access   |
|--------|-----------------------|-------------------|
| GET    | `/api/test/anonymous` | Public            |
| GET    | `/api/test/user`      | `user` or `admin` |
| GET    | `/api/test/admin`     | `admin`           |

The authenticated endpoints return example `name` and `email` JWT claims. A production API should return personal data
only when required.

## Manual verification with Postman

Obtain access tokens using **OAuth 2.0 -> Authorization Code (With PKCE)** and send them as Bearer tokens.

| Scenario                                                                  | Expected status |
|---------------------------------------------------------------------------|-----------------|
| `/test/anonymous` without a token                                         | 200             |
| `/test/admin` without a token                                             | 401             |
| `/test/admin` with `user1` token                                          | 403             |
| `/test/user` with `user1` token                                           | 200             |
| `/test/admin` with `globaladmin` token                                    | 200             |
| Both protected endpoints with `user2` token                               | 200             |
| Protected endpoint with an otherwise valid token missing the API audience | 401             |