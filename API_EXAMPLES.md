# API Usage Examples

This file contains example requests and responses for the Y Video Gen API.

## 1. Health Check

### Request
```bash
curl -X GET http://localhost:8080/api/v1/health
```

### Response
```
HTTP/1.1 200 OK
Content-Type: text/plain;charset=UTF-8

Y Video Gen API is running
```

## 2. Generate Scenes (POST - with Google Drive)

Generate video scenes from a theme and optionally save to Google Drive.

### Request
```bash
curl -X POST http://localhost:8080/api/v1/ideas/generate-scenes \
  -H "Content-Type: application/json" \
  -d '{
    "theme": "The future of artificial intelligence in healthcare",
    "numberOfScenes": 5,
    "saveToGoogleDrive": true
  }'
```

### Response
```json
{
  "theme": "The future of artificial intelligence in healthcare",
  "scenes": [
    "Scene 1: Open with a healthcare facility entrance. A voiceover explains how AI is transforming diagnosis. Show medical professionals using AI-powered diagnostic tools. Transition with a modern tech animation.",
    "Scene 2: Interview segment with a healthcare AI specialist discussing current implementations. Show charts and statistics about AI adoption in hospitals. Emphasize improved patient outcomes.",
    "Scene 3: Demonstrate an AI diagnostic system in action. Show the system analyzing medical images quickly and accurately. Compare traditional vs AI-assisted diagnostic speed.",
    "Scene 4: Feature patient testimonials about receiving faster diagnoses. Show before and after treatment scenarios. Highlight the human element - AI supporting doctors, not replacing them.",
    "Scene 5: Conclude with a forward-looking statement about AI's potential in healthcare. Show a montage of hospitals adopting AI systems. End with a call to action for viewers interested in healthcare innovation."
  ],
  "googleDriveFileId": "1a2b3c4d5e6f7g8h9i0j",
  "googleDriveFileLink": "https://drive.google.com/file/d/1a2b3c4d5e6f7g8h9i0j/view",
  "message": "Scenes generated successfully and saved to Google Drive"
}
```

## 3. Generate Scenes (POST - without Google Drive)

Generate scenes without saving to Google Drive.

### Request
```bash
curl -X POST http://localhost:8080/api/v1/ideas/generate-scenes \
  -H "Content-Type: application/json" \
  -d '{
    "theme": "Sustainable energy solutions for the future",
    "numberOfScenes": 3,
    "saveToGoogleDrive": false
  }'
```

### Response
```json
{
  "theme": "Sustainable energy solutions for the future",
  "scenes": [
    "Scene 1: Open with renewable energy landscape shots - solar panels and wind turbines. Aerial view of solar farms and offshore wind installations.",
    "Scene 2: Discuss battery technology and energy storage solutions. Show modern battery manufacturing and grid storage systems in action.",
    "Scene 3: Conclude with vision of 100% renewable energy future. Show communities powered by clean energy with improved quality of life."
  ],
  "googleDriveFileId": null,
  "googleDriveFileLink": null,
  "message": "Scenes generated successfully"
}
```

## 4. Generate Scenes (GET - Simple endpoint)

Use the GET endpoint for quick scene generation with default parameters.

### Request
```bash
curl -X GET "http://localhost:8080/api/v1/ideas/generate-scenes/Space%20exploration%20technologies?numberOfScenes=4"
```

### Response
```json
{
  "theme": "Space exploration technologies",
  "scenes": [
    "Scene 1: Establish the cosmic perspective with stunning space imagery. Show Earth from space, then zoom out to show the solar system.",
    "Scene 2: Discuss cutting-edge spacecraft and propulsion technologies. Feature SpaceX, NASA, and international space programs.",
    "Scene 3: Highlight discoveries from space missions - exoplanets, black holes, cosmic phenomena.",
    "Scene 4: Inspire viewers about humanity's future in space. Show concepts of space colonization and long-term space exploration goals."
  ],
  "googleDriveFileId": null,
  "googleDriveFileLink": null,
  "message": "Scenes generated successfully"
}
```

## 5. Error Handling

### Missing Theme
```bash
curl -X POST http://localhost:8080/api/v1/ideas/generate-scenes \
  -H "Content-Type: application/json" \
  -d '{
    "theme": "",
    "numberOfScenes": 5,
    "saveToGoogleDrive": false
  }'
```

Response:
```json
{
  "theme": null,
  "scenes": null,
  "googleDriveFileId": null,
  "googleDriveFileLink": null,
  "message": "Error: Theme is required"
}
```

### Google Drive Not Configured
```bash
curl -X POST http://localhost:8080/api/v1/ideas/generate-scenes \
  -H "Content-Type: application/json" \
  -d '{
    "theme": "My awesome theme",
    "numberOfScenes": 3,
    "saveToGoogleDrive": true
  }'
```

Response:
```json
{
  "theme": "My awesome theme",
  "scenes": [
    "Scene 1: ...",
    "Scene 2: ...",
    "Scene 3: ..."
  ],
  "googleDriveFileId": null,
  "googleDriveFileLink": null,
  "message": "Scenes generated successfully, but failed to save to Google Drive: Google Drive credentials file path not configured."
}
```

## Using Swagger UI

For an interactive API experience, use the Swagger UI:

1. Start the application: `./gradlew bootRun`
2. Open http://localhost:8080/swagger-ui.html
3. Expand the "Idea Generation" section
4. Click "Try it out" for any endpoint
5. Fill in the parameters and click "Execute"

## PowerShell Examples

For Windows users with PowerShell:

```powershell
# POST request
$body = @{
    theme = "The future of artificial intelligence in healthcare"
    numberOfScenes = 5
    saveToGoogleDrive = $true
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/v1/ideas/generate-scenes" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body $body

# GET request
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/ideas/generate-scenes/AI%20in%20healthcare?numberOfScenes=3" `
  -Method GET
```

## JavaScript/Fetch Examples

```javascript
// POST request with fetch
const response = await fetch('http://localhost:8080/api/v1/ideas/generate-scenes', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    theme: 'The future of artificial intelligence in healthcare',
    numberOfScenes: 5,
    saveToGoogleDrive: true
  })
});

const data = await response.json();
console.log(data);

// GET request
const getResponse = await fetch('http://localhost:8080/api/v1/ideas/generate-scenes/AI in healthcare?numberOfScenes=3');
const getData = await getResponse.json();
console.log(getData);
```

## Configuration Notes

- **numberOfScenes**: defaults to 5 if not provided, min 1, max 20
- **Scenes Quality**: Depends on OpenAI API key permissions and model access
- **Response Time**: Scene generation typically takes 5-15 seconds
- **Google Drive**: Requires proper setup (see GOOGLE_DRIVE_SETUP.md)

