# SITU-Backend
Backend for SITU-WebApp

## Required
* Java 21
* Postgres database
* Docker (optional for <a href="files/postgres/init.sql">initialization de database</a>)

## Build
1. See the <a href="EnvironmentVariables.md">environment variables</a> section.
2. (Optional) Make sure that the Docker engine is running and run this command in the root folder of this repository in terminal:
    ```bash
    docker-compose down && docker-compose up -d
    ```
3. Build and run with some IDE (IntelliJ or Visual Studio Code) or use the terminal.
