# SITU-Backend
Backend for [SITU-WebApp](https://github.com/IgnacioVeiga/SITU-WebApp)

## About SITU
/// To be redacted ///

## Required
* Java 20+
* Postgres database
* Docker (Optional to initialize the <a href="docker-compose.yml">container</a> with the <a href="files/postgres/init.sql">database</a>)

## Build
1. See the <a href="EnvironmentVariables.md">environment variables</a> section.
2. (Optional) Make sure that the Docker engine is running and run this command in the root folder of this repository in terminal:
    ```bash
    docker-compose down && docker-compose up -d
    ```
3. Build and run with some IDE (IntelliJ or Visual Studio Code) or use the terminal.
