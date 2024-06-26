# SITU-Backend
Backend for SITU-WebApp

## Required
* Java 21
* Docker

## Build
1. Create a `.env` file and see the `.env.txt_file_example_of_.env.txt` for reference.
2. Make sure that the Docker engine is running.
3. Run this command in the root folder of this repository in the terminal:
```bash
    docker-compose down && docker-compose up -d
```
4. Build and run with some IDE (IntelliJ or Visual Studio Code) or use the terminal.

# Additional notes
The `./files/postgres/init.sql` file generates 3 users and then the `./src/main/resources/db/migration/V1_2__update_hashed_passwords.sql` file assigns their password already hardcoded based on the `JWT_SECRET` environment variable, which in this example was used `JWT_SECRET=your_local_secret_key`. So Login/Signup will not work with those users if your variable is different.