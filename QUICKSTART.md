# Quick Start Guide

## 1. Prerequisites
- Java 21+
- Gradle 8.x (or use `./gradlew`)
- OpenAI API key (required)
- Google Drive credentials (optional)

## 2. Configure OpenAI (Required)

```bash
export SPRING_AI_OPENAI_API_KEY=sk-your-key-here
```

## 3. Configure Google Drive (Optional)

1. Create a Google Cloud Project with Google Drive API enabled
2. Create a service account and download the JSON credentials file
3. Set environment variable:
```bash
export GOOGLE_DRIVE_CREDENTIALS_FILE=/path/to/credentials.json
```

Detailed guide: See [GOOGLE_DRIVE_SETUP.md](./GOOGLE_DRIVE_SETUP.md)

## 4. Build the Project

```bash
./gradlew clean build -x test
```

## 5. Run the Application

```bash
./gradlew bootRun
```

Application will start at: `http://localhost:8080`

## 6. Test the API

### Option A: Swagger UI (Recommended)
Open: http://localhost:8080/swagger-ui.html
- Expand "Idea Generation" section
- Click "Try it out"
- Enter your theme and click "Execute"

### Option B: curl
```bash
curl -X POST http://localhost:8080/api/v1/ideas/generate-scenes \
  -H "Content-Type: application/json" \
  -d '{
    "theme": "The future of AI in healthcare",
    "numberOfScenes": 5,
    "saveToGoogleDrive": false
  }'
```

### Option C: GET endpoint
```bash
curl "http://localhost:8080/api/v1/ideas/generate-scenes/AI%20in%20healthcare?numberOfScenes=3"
```

## 7. Sample Response

```json
{
  "theme": "The future of AI in healthcare",
  "scenes": [
    "Scene 1: Open with healthcare facility entrance...",
    "Scene 2: Interview segment with healthcare AI specialist...",
    "Scene 3: Demonstrate an AI diagnostic system..."
  ],
  "googleDriveFileId": null,
  "googleDriveFileLink": null,
  "message": "Scenes generated successfully"
}
```

## Common Tasks

### View API Documentation
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

### Check API Health
```bash
curl http://localhost:8080/api/v1/health
```

### Generate Scenes and Save to Drive
Make sure Google Drive is configured, then:
```bash
curl -X POST http://localhost:8080/api/v1/ideas/generate-scenes \
  -H "Content-Type: application/json" \
  -d '{
    "theme": "Your theme here",
    "numberOfScenes": 5,
    "saveToGoogleDrive": true
  }'
```

### View Logs
```bash
# macOS/Linux
tail -f logs/application.log

# Or from gradle output during bootRun
# Logs will be printed to console
```

## Troubleshooting

### Error: "OpenAI API key not configured"
✅ Solution: `export SPRING_AI_OPENAI_API_KEY=your-key`

### Error: "Model not available"
✅ Solution: Check you have access to GPT-4 model in your OpenAI account

### Error: "Google Drive credentials file not found"
✅ Solution: 
- Make sure file exists at the specified path
- Use absolute path in environment variable
- Google Drive is optional, scenes will still be generated

### Swagger UI won't load
✅ Solution:
- Verify app is running: http://localhost:8080/api-docs
- Check port 8080 is available
- Try browser refresh or clear cache

## Development

### Project Structure
```
src/main/java/com/logicsoft/yvideogen/
├── controller/      # REST endpoints
├── service/         # Business logic
├── dto/            # Request/Response models
└── exception/      # Custom exceptions
```

### Adding New Endpoints
1. Create controller method in IdeaGenController or new controller
2. Add `@PostMapping`, `@GetMapping` annotations
3. Add `@Operation` annotation for Swagger docs
4. Test via Swagger UI

### Key Files to Modify
- `IdeaGenController.java` - Add new REST endpoints
- `SceneGenerationService.java` - Modify scene generation logic
- `GoogleDriveService.java` - Modify Google Drive behavior
- `application.properties` - Add new configuration

## Performance Tips

1. **Scene Generation**: Typically takes 5-15 seconds per request
2. **Batch Operations**: Use numberOfScenes parameter for efficiency
3. **Caching**: Consider caching scenes for repeated themes
4. **Concurrency**: The API supports concurrent requests

## Next Steps

1. ✅ Run the application
2. ✅ Test endpoints via Swagger UI
3. ✅ Integrate into your workflow
4. ✅ Set up Google Drive (optional) for persistence
5. ✅ Customize scene generation prompts as needed

## Documentation

- [README.md](./README.md) - Full project documentation
- [SETUP.md](./SETUP.md) - Detailed setup instructions
- [GOOGLE_DRIVE_SETUP.md](./GOOGLE_DRIVE_SETUP.md) - Google Drive guide
- [API_EXAMPLES.md](./API_EXAMPLES.md) - API usage examples
- [IMPLEMENTATION_SUMMARY.md](./IMPLEMENTATION_SUMMARY.md) - Technical details

## Support

- Check the documentation files
- Review Swagger UI for API details
- Check application logs for errors
- See troubleshooting section above

---

**Happy scene generation!** 🎬✨

