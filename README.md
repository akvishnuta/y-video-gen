# Y Video Gen

An AI-powered video generation application that transforms your ideas into professional videos using Spring AI and OpenAI integration.

## Overview

Y Video Gen is a Spring Boot application that leverages artificial intelligence to generate video content based on user inputs. The application integrates with OpenAI models to provide intelligent video generation capabilities.

## Features

- 🤖 AI-powered video scene generation using Spring AI
- 🎬 OpenAI integration for intelligent content creation
- 📝 Generate video scripts and scenes from abstract themes
- 💾 Automatic saving to Google Drive
- 📚 RESTful API with comprehensive documentation
- 📋 Swagger/OpenAPI UI for interactive API exploration
- 🔄 Spring Boot 4.0.6 framework
- ☕ Java 21 support
- 📦 Gradle build system

## Prerequisites

- **Java 21** or higher
- **Gradle** (or use the included `gradlew` wrapper)
- **OpenAI API Key** for AI model integration
- **Google Cloud Project** (optional, for Google Drive integration)
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

#### Idea Generation
- **POST** `/api/v1/ideas/generate-scenes` - Generate video scenes from a theme
  - Request body:
    ```json
    {
      "theme": "Your video theme",
      "numberOfScenes": 5,
      "saveToGoogleDrive": true
    }
    ```
  - Response includes generated scenes and optional Google Drive file link

- **GET** `/api/v1/ideas/generate-scenes/{theme}` - Generate scenes (simple GET endpoint)
  - Parameters: `numberOfScenes` (default: 5)

## Project Structure

```
src/
├── main/
│   ├── java/com/logicsoft/yvideogen/
│   │   ├── YvideogenApplication.java           # Main Spring Boot application
│   │   ├── config/
│   │   │   └── SwaggerConfig.java              # Swagger/OpenAPI configuration
│   │   ├── controller/
│   │   │   ├── HealthController.java           # Health check endpoint
│   │   │   └── IdeaGenController.java          # Video idea generation endpoints
│   │   ├── dto/
│   │   │   ├── SceneGenerationRequest.java     # Request DTO for scene generation
│   │   │   └── SceneGenerationResponse.java    # Response DTO for generated scenes
│   │   ├── exception/
│   │   │   ├── SceneGenerationException.java   # Scene generation errors
│   │   │   └── GoogleDriveException.java       # Google Drive errors
│   │   └── service/
│   │       ├── SceneGenerationService.java     # AI-powered scene generation
│   │       └── GoogleDriveService.java         # Google Drive integration
│   └── resources/
│       ├── application.properties              # Application configuration
│       └── application-local.properties        # Local development configuration
└── test/
    └── java/com/logicsoft/yvideogen/          # Integration tests
```

## Technology Stack

- **Framework**: Spring Boot 4.0.6
- **Language**: Java 21
- **AI Integration**: Spring AI 2.0.0-M6 with OpenAI
- **Cloud Integration**: Google Drive API v3
- **Build Tool**: Gradle 8.x
- **API Documentation**: Springdoc OpenAPI 2.3.0
- **Authentication**: Google OAuth2 (for Drive API)
- **JSON Processing**: Gson
- **Utilities**: Lombok, SLF4J

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
- `google-api-services-drive` - Google Drive API
- `google-auth-library-oauth2-http` - Google OAuth2 authentication
- `google-oauth-client-jetty` - OAuth2 client
- `google-api-client` - Google API client
- `gson` - JSON processing
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

### Issue: Google Drive upload fails
- Verify Google Drive credentials file path is set in environment variables
- Ensure the service account has Google Drive API permissions
- Check that the credentials JSON file is valid
- For detailed Google Drive setup, see [GOOGLE_DRIVE_SETUP.md](./GOOGLE_DRIVE_SETUP.md)

## Using Idea Generation with Google Drive Integration

### Generate Video Scenes

The application can generate video scenes from abstract themes using OpenAI's GPT-4 model:

```bash
curl -X POST http://localhost:8080/api/v1/ideas/generate-scenes \
  -H "Content-Type: application/json" \
  -d '{
    "theme": "The future of artificial intelligence in healthcare",
    "numberOfScenes": 5,
    "saveToGoogleDrive": true
  }'
```

### Save to Google Drive

To enable Google Drive integration:

1. Follow the setup guide in [GOOGLE_DRIVE_SETUP.md](./GOOGLE_DRIVE_SETUP.md)
2. Set the `GOOGLE_DRIVE_CREDENTIALS_FILE` environment variable
3. Optionally set `GOOGLE_DRIVE_FOLDER_ID` for a specific destination folder

The generated scenes will be automatically saved as a text file in Google Drive and a shareable link will be returned.

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

- [x] Scene generation from abstract themes
- [x] Google Drive integration for saving generated content
- [ ] Advanced video customization options
- [ ] Video generation job queue
- [ ] Webhook notifications for completed videos
- [ ] Multi-language support
- [ ] Video analytics and metrics
- [ ] Admin dashboard
- [ ] Support for multiple AI models
- [ ] Batch scene generation
- [ ] Template-based scene generation

---

**Last Updated**: May 2026
