# PoCoPI backend

This project uses Docker Compose to run a full-stack application with MySQL database, Spring Boot, and Nginx.

## Application Setup Guide

### Prerequisites

#### For Docker (Recommended)

- [Docker](https://docs.docker.com/get-docker/) (version 20.10 or later)
- [Docker Compose](https://docs.docker.com/compose/install/) (version 2.0 or later)

#### For Local Development (Without Docker)

- [MySQL](https://dev.mysql.com/downloads/mysql/) (version 9.4.0 or compatible)
- [Nginx](https://nginx.org/en/download.html) (version 1.29.2 or compatible)
- [Java](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html) (version 21 or later)

### Setup Instructions

1. **Create environment file**:
    ```bash
    cp .env.example .env
    ```

   Edit `.env` and configure the following required variables:
    ```env
    MYSQL_DATABASE=pocopi
    MYSQL_ROOT_PASSWORD=....
    
    SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/pocopi
    
    IMAGES_BASE_PATH=./images
    IMAGES_BASE_URL=http://localhost:8081
    
    JWT_SECRET=secret_token
    JWT_EXPIRATION=21600000
    
    OLD_CONFIG_PATH=/path/to/old/PoCoPI/config
    ```

2. **Create required directories**:
   ```bash
   mkdir -p images
   ```

3. **Build and start the services**:
   ```bash
   docker compose up -d
   ```

4. **Check service health**:
   ```bash
   docker compose ps
   docker compose logs
   ```

5. **Access the application**:
    - Application: `http://localhost:8080` (or your configured `APP_PORT`)
    - Nginx: `http://localhost:8081` (or your configured `NGINX_PORT`)
    - MySQL: `localhost:33061` (might be removed in the future)

### Common Docker Commands

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

```shell
docker compose exec app java -jar app.jar --server.port=9090 --migrate-old-config
```

### Port Configuration

| Service | Default Port | Environment Variable | Description         |
|---------|--------------|----------------------|---------------------|
| MySQL   | 33061        | -                    | Database (external) |
| MySQL   | 3306         | -                    | Database (internal) |
| App     | 8080         | `APP_PORT`           | Application server  |
| Nginx   | 8081         | `NGINX_PORT`         | Web server          |

### Volume Mounts

- `./images` - Application images storage
- `./.old-config` - Legacy configuration files (read-only, inside container)
- `db` - MySQL data persistence

### Troubleshooting

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
