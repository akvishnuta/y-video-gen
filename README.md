# Y Video Gen

An AI-powered video generation application that transforms your ideas into professional videos using Spring AI and OpenAI integration.

## Overview

Y Video Gen is a Spring Boot application that leverages artificial intelligence to generate video content based on user inputs. The application integrates with OpenAI models to provide intelligent video generation capabilities.

## Features

- 🤖 AI-powered video generation using Spring AI
- 🎬 OpenAI integration for intelligent content creation
- 📚 RESTful API with comprehensive documentation
- 📋 Swagger/OpenAPI UI for interactive API exploration
- 🔄 Spring Boot 4.0.6 framework
- ☕ Java 21 support
- 📦 Gradle build system

## Prerequisites

- **Java 21** or higher
- **Gradle** (or use the included `gradlew` wrapper)
- **OpenAI API Key** for AI model integration
- **Spring Boot 4.0.6**

## Installation

### 1. Clone the repository

```bash
git clone https://github.com/logicsoft/y-video-gen.git
cd y-video-gen
```

### 2. Configure Environment Variables

Create a `.env` file in the project root or configure your system environment:

```bash
export SPRING_AI_OPENAI_API_KEY=your_openai_api_key_here
```

Alternatively, update `application.properties`:

```properties
spring.ai.openai.api-key=your_openai_api_key_here
```

### 3. Build the Project

```bash
./gradlew clean build
```

## Running the Application

Start the Spring Boot application:

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

## API Documentation

### Swagger UI

Access the interactive API documentation at:

```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON

View the raw OpenAPI specification at:

```
http://localhost:8080/api-docs
```

### Available Endpoints

#### Health Check
- **GET** `/api/v1/health` - Check if the API is running

## Project Structure

```
src/
├── main/
│   ├── java/com/logicsoft/yvideogen/
│   │   ├── YvideogenApplication.java       # Main Spring Boot application
│   │   ├── config/
│   │   │   └── SwaggerConfig.java          # Swagger/OpenAPI configuration
│   │   └── controller/
│   │       └── HealthController.java       # Health check endpoint
│   └── resources/
│       └── application.properties          # Application configuration
└── test/
    └── java/com/logicsoft/yvideogen/
        └── YvideogenApplicationTests.java  # Integration tests
```

## Technology Stack

- **Framework**: Spring Boot 4.0.6
- **Language**: Java 21
- **AI Integration**: Spring AI 2.0.0-M6
- **AI Model**: OpenAI
- **Build Tool**: Gradle 8.x
- **API Documentation**: Springdoc OpenAPI 2.3.0
- **Utilities**: Lombok

## Configuration

### Application Properties

Key configuration in `src/main/resources/application.properties`:

```properties
# Application Name
spring.application.name=yvideogen

# Swagger/OpenAPI Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true

# Spring AI OpenAI Configuration
spring.ai.openai.api-key=<your-api-key>
spring.ai.openai.chat.options.model=gpt-4
```

## Testing

Run the test suite:

```bash
./gradlew test
```

## Development

### Adding New Endpoints

1. Create a new controller in `src/main/java/com/logicsoft/yvideogen/controller/`
2. Add `@RestController` and `@RequestMapping` annotations
3. Use `@Operation` and `@Tag` annotations for Swagger documentation
4. Access API docs at http://localhost:8080/swagger-ui.html to verify

### Example Controller

```java
@RestController
@RequestMapping("/api/v1/videos")
@Tag(name = "Videos", description = "Video generation endpoints")
public class VideoController {
    
    @PostMapping("/generate")
    @Operation(summary = "Generate video", description = "Create a new video from input")
    public ResponseEntity<VideoResponse> generateVideo(@RequestBody VideoRequest request) {
        // Implementation
        return ResponseEntity.ok(new VideoResponse());
    }
}
```

## Dependencies

- `spring-boot-starter-webmvc` - Web MVC support
- `spring-ai-starter-model-openai` - OpenAI integration
- `springdoc-openapi-starter-webmvc-ui` - Swagger UI
- `lombok` - Code generation utility

## Building for Production

```bash
./gradlew build -x test
```

This creates an executable JAR in `build/libs/`:

```bash
java -jar build/libs/yvideogen-0.0.1-SNAPSHOT.jar
```

## Environment-Specific Configuration

Create environment-specific property files:

- `application-dev.properties` - Development configuration
- `application-prod.properties` - Production configuration

Run with specific profile:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## Logging

Configure logging in `application.properties`:

```properties
logging.level.root=INFO
logging.level.com.logicsoft.yvideogen=DEBUG
logging.file.name=logs/application.log
```

## Troubleshooting

### Issue: OpenAI API Key not recognized
- Ensure the API key is correctly set in environment variables or application properties
- Verify the API key has appropriate permissions in OpenAI dashboard

### Issue: Swagger UI not loading
- Check that `springdoc-openapi-starter-webmvc-ui` is in dependencies
- Ensure the application is running on the correct port
- Verify `springdoc.swagger-ui.enabled=true` in properties

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## Contact

- **Organization**: LogicSoft
- **Website**: https://logicsoft.com

## Roadmap

- [ ] Advanced video customization options
- [ ] Video generation job queue
- [ ] Webhook notifications for completed videos
- [ ] Multi-language support
- [ ] Video analytics and metrics
- [ ] Admin dashboard

---

**Last Updated**: May 2026
