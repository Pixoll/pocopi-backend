# PoCoPI backend

This project uses Docker Compose to run an application with Spring Boot, a MySQL database, and an image service with Nginx.

## Application Setup Guide

### Prerequisites

#### For Deployment

- [Docker](https://docs.docker.com/get-docker/) (version 29 or later)
- [Docker Compose](https://docs.docker.com/compose/install/) (version 5 or later)

#### For Local Development

- [Docker](https://docs.docker.com/get-docker/) (version 29 or later)
- [Docker Compose](https://docs.docker.com/compose/install/) (version 5 or later)
- [Java](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html) (version 21 or later)
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) (ideal for frictionless development)

### Setup Instructions

1. **Create environment file**:
   Create the `.env` file by copying `.env.example`.
    ```bash
    cp .env.example .env
    ```

   Edit `.env` and configure the variables. It's recommended you modify the following:
    - `APP_PORT`
    - `NGINX_PORT`
    - `MYSQL_PORT` (only in development)
    - `CORS_ALLOWED_ORIGINS`
    - `MYSQL_ROOT_PASSWORD`
    - `IMAGES_BASE_URL`
    - `JWT_SECRET`
    - `JWT_EXPIRATION`
    - `OLD_CONFIG_PATH` (if you're migrating from the old infrastructure)

2. **Build and start the services**:
    - In production:
   ```bash
   docker compose up -d --build
   ```

    - In development:
   ```bash
   docker compose -f docker-compose.dev.yaml up -d --build
   # Then start up the Spring Boot app from your IDE
   ```

3. **Check service health**:
    - In production:
   ```bash
   docker compose ps
   docker compose logs
   ```

    - In development:
   ```bash
   docker compose -f docker-compose.dev.yaml ps
   docker compose -f docker-compose.dev.yaml logs
   ```

4. **Access the application**:
    - Application: `http://localhost:8080` (or your configured `APP_PORT`)
    - Images service: `http://localhost:8081` (or your configured `NGINX_PORT`)
    - MySQL database: `localhost:33061` (in development with your configured `MYSQL_PORT`)

### Common Docker Commands

> Note: add `-f docker-compose.dev.yaml` right after `docker compose` when in development.

```bash
## Start services
docker compose up -d

## Stop services
docker compose down

## View logs
docker compose logs -f

## View logs for specific service
docker compose logs -f app

## Rebuild services
docker compose up -d --build

## Stop and remove all containers, networks, and volumes
docker compose down -v

## Execute commands in running container
docker compose exec app sh
docker compose exec db mysql -u root -p
```

### Migrate old configuration

Run this command to migrate the old configuration from [PoCoPI](https://github.com/Pixoll/pocopi-frontend)

> Note: add `-f docker-compose.dev.yaml` right after `docker compose` when in development.

```shell
docker compose exec app java -jar app.jar --server.port=9090 --migrate-old-config
```

### Create admin

Run this command to create a new admin user. You need this to access the dashboard and settings panel in the frontend.

> Note: add `-f docker-compose.dev.yaml` right after `docker compose` when in development.

```shell
docker compose exec app java -jar app.jar --server.port=9090 --create-admin
```

### Port Configuration

| Service | Default Port | Environment Variable | Description                        |
|---------|--------------|----------------------|------------------------------------|
| MySQL   | 33061        | `MYSQL_PORT`         | Database (external in development) |
| MySQL   | 3306         | -                    | Database (internal)                |
| App     | 8080         | `APP_PORT`           | Application server                 |
| Nginx   | 8081         | `NGINX_PORT`         | Web server                         |

### Volume Mounts

- `./images` - Application images storage
- `./.old-config` - Legacy configuration files (read-only, inside container)
- `db` - MySQL data persistence

### Troubleshooting

> Note: add `-f docker-compose.dev.yaml` right after `docker compose` when in development.

#### Database connection issues

```bash
## Check if MySQL is running
docker compose ps db

## View database logs
docker compose logs db

## Test database connection
docker compose exec db mysql -u root -p
```

#### Application issues

```bash
## View application logs
docker compose logs app

## Restart application
docker compose restart app
```

#### Permission issues

```bash
## Fix directory permissions
chmod -R 755 images
chown -R $USER images
chown -R $USER:$USER images

chmod -R 755 .old-config
chown -R $USER .old-config
chown -R $USER:$USER .old-config
```
