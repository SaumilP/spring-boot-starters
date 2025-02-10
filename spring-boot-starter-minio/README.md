# spring-boot-starter-minio

[Spring Boot] Starter for [MinIO].

[Spring Boot]: https://spring.io/projects/spring-boot
[MinIO]: https://min.io/docs/minio/linux/developers/java/minio-java.html

## Features

- Creates managed-bean from setup application properties and makes it injectable into consuming application code.
- Provides interface to consume the `StorageService`.

## Supported versions

"spring-boot-starter-minio" supports the following versions.
Other versions might also work, but I have not tested it.

- Java 21
- Spring Boot 3.3.1
- MinIO 8.4.6

## Usage

### Adding the dependency

"spring-boot-starter-minio" is not published anywhere, so best to have the code checked out and installed locally before using it as dependency.
If you are using Maven, add the following dependency.

```xml
<dependency>
    <groupId>org.sandcastle.starter-apps</groupId>
    <artifactId>spring-boot-starter-minio</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Injecting the `StorageService`

The `StorageService` (Storage interface) is managed by the application context.
Inject the `StorageService` into your code.

For example:

```java
@Autowired
private StorageService minioStorageService;
```

### Usage

Map your consuming class using the `StorageService`.

For example:

```java
var imgPath = Paths.get("/classifier/image-1.jpg");
InputStream storedObject = minioStorageService.get(imgPath);
```

See also the MinIO official documents:

- [MinIO Java SDK]

[MinIO Java SDK]: https://min.io/docs/minio/linux/developers/java/minio-java.html

## Customizing

## Configuration properties

"spring-boot-starter-minio" provides the following configuration properties.
These can be configure by your "application.yml" / "application.properties".

```yml
spring:
  minio:
    # Endpoint where MinIO server is accessible from.
    url: http://localhost:9000
    # Issued access-key to access MinIO server bucket
    access-key: <ACCESS-KEY>
    # Issued secret-key to access MinIO server bucket
    secret-key: <SECRET-KEY>
    # Indicates if endpoint is secure
    # Defaults to false.
    secure: false
    # Configurable bucket where upload/download/list to be actioned
    bucket: test-bucket
    # Defaults to minio.storage.
    metric-name: minio.storage
    # Defaults to 10 seconds
    connect-timeout: 10
    # Defaults to 60 seconds
    write-timeout: 60
    # Defaults to 10 seconds
    read-timeout: 10
    # Defaults to 30 seconds
    expire: 30
```

## Contributing

Bug reports and pull requests are welcome :)

## Building and testing

To build and test, you can run:

```sh
$ cd orika-spring-boot-starter
$ ./mvnw clean install
```

## License

Licensed under the [Apache License, Version 2.0].

[Apache License, Version 2.0]: LICENSE.txt
