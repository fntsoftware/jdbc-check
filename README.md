# JDBC check

Tests if a connection to the given oracle or postgres database is possible.

## Parameters
```
-dbName <arg>       Name of the DB
-dbPassword <arg>   The decrypted Password of the Database
-dbType <arg>       Type of the Database (postgres / oracle / oraclesid)
-dbUsername <arg>   Name of the DatabaseUser
-hostName <arg>     Name or URL from the DB host
-jdbcURL <arg>      The wohle JDBC URL connection string (e.g jdbc:postgresql://localhost:22222/testdb)
-port <arg>         Port of the DB instance
-query <arg>        Query String used for checking status of DB
-timeout <arg>      Time before trying is aborted
```

## Usage
``` bash
# oracle (command db)
java -jar jdbcTestDES.jar -dbType oracle -hostName localhost -port 1521 -dbName testDB -dbUsername command -dbPassword command -timeout 300 -query 'select 'X' from stfsys_sw_unit'

# postgres
java -jar jdbcTestDES.jar -dbType postgres -hostName localhost -port 5432 -dbName testDB -dbUsername command -dbPassword command -timeout 300 -query 'select 1'
```

## Releasing a new version
To release a new version of the jdbc checker, you have to push a new git tag using semantic versioning.

``` bash
# create new tag
git tag 1.4.0

# push tag
git push origin 1.4.0
```

## Running the tests locally
``` bash
# start a database
docker run -d --name test_db -p 22222:5432 -e POSTGRES_USER=user -e POSTGRES_PASSWORD=password -e POSTGRES_DB=testdb postgres:16.1

# run the tests
./gradlew testJar
```

## Code formatting
``` bash
# check code format
./gradlew spotlessCheck

# format code
./gradlew spotlessApply
```
