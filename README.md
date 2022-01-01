# Herbarium - API REST

Herbarium is a semi-autonomous greenhouse that can help people growing plants in their house. 

This is an end of study project realized by three student of the ÉTS:

- Charles Chaumienne (electrical engineering)
- Youssef Hammami (software engineering)
- [Xavier Chrétien (software engineering)](https://github.com/apomalyn).

This project is divided in three (3) parts:

- [Embedded software](https://github.com/botand/herbarium-embedded)
- API REST (this repository)
- [Mobile application](https://github.com/botand/herbarium-mobile) 

## Requires

* Kotlin 1.4.32
* Gradle 6.9
* PostgreSQL Database
* A firebase project (with service account file)

## Build

First, create the gradle wrapper script:

```
gradle wrapper
```

Then, run:

```
./gradlew check assemble
```

This runs all tests and packages the library.

## Running

You will need the following environment variables:
- DB_USERNAME: The username needed to connect the service to the database
- DB_PASSWORD: Password linked to the username.
- DB_URL: Url of the database (ex: localhost)
- DB_PORT: Port of the host.
- DB_NAME: Name of the database.
- GOOGLE_APPLICATION_CREDENTIALS: Path where the firebase service account JSON file is stored


The server builds as a fat jar with a main entrypoint. To start the service, run `java -jar ./build/libs/herbarium-api.jar`.

You may also run in docker:

```
docker build -t herbarium-api .
docker run -p 8080:8080 herbarium-api
```

## Features/Implementation Notes

* Supports JSON inputs/outputs, File inputs, and Form inputs (see ktor documentation for more info).
* ~Supports collection formats for query parameters: csv, tsv, ssv, pipes.~
* Some Kotlin and Java types are fully qualified to avoid conflicts with types defined in OpenAPI definitions.

<a name="documentation-for-api-endpoints"></a>
## Documentation for API Endpoints

The documentation of the endpoint is available [here](https://botand.github.io/herbarium-api/#tag--plant)


<a name="documentation-for-authorization"></a>
## Documentation for Authorization

<a name="apiKey"></a>
### apiKey

- **Type**: API key
- **API key parameter name**: X-API-Key
- **Location**: HTTP header

<a name="oauth"></a>
### oauth

- **Type**: HTTP (Bearer token)
- **Bearer format**: JWT

