# Google Drive API Setup Guide

This guide explains how to set up Google Drive integration for the Y Video Gen API.

## Prerequisites

1. A Google Cloud Project with the Google Drive API enabled
2. A service account JSON credentials file

## Step 1: Create a Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the Google Drive API:
   - Go to "APIs & Services" > "Library"
   - Search for "Google Drive API"
   - Click on it and press "Enable"

## Step 2: Create Service Account Credentials

1. Go to "APIs & Services" > "Credentials"
2. Click "Create Credentials" > "Service Account"
3. Fill in the service account details:
   - Service account name: `y-video-gen`
   - Click "Create and Continue"
4. Grant the service account basic permissions (optional):
   - Project Editor role (for this app)
5. Click "Continue" and then "Done"

## Step 3: Create and Download JSON Key

1. Click on the newly created service account
2. Go to the "Keys" tab
3. Click "Add Key" > "Create new key"
4. Select "JSON" as the key type
5. Click "Create" - this will download your credentials file

**Important**: Keep this JSON file secure. It contains sensitive credentials.

## Step 4: Configure the Application

### Option A: Environment Variable (Recommended)

```bash
export SPRING_AI_OPENAI_API_KEY=your_openai_api_key
export GOOGLE_DRIVE_CREDENTIALS_FILE=/path/to/your/credentials.json
```

Then optionally set the parent folder ID:
```bash
export GOOGLE_DRIVE_FOLDER_ID=your-google-drive-folder-id
```

### Option B: Properties File

Edit `src/main/resources/application-local.properties`:

```properties
google.drive.credentials.file=/path/to/your/credentials.json
google.drive.folder.id=your-google-drive-folder-id
```

Then run with:
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

## Step 5: Share a Google Drive Folder (Optional)

If you want the generated files to be stored in a specific folder:

1. Create a folder in your Google Drive
2. Share it with the service account email (found in the JSON credentials file)
3. Copy the folder ID from the URL
4. Set `GOOGLE_DRIVE_FOLDER_ID` or `google.drive.folder.id`

## API Usage

### Endpoint 1: Generate Scenes (with optional Google Drive save)

**POST** `/api/v1/ideas/generate-scenes`

Request body:
```json
{
  "theme": "The future of artificial intelligence in healthcare",
  "numberOfScenes": 5,
  "saveToGoogleDrive": true
}
```

Response:
```json
{
  "theme": "The future of artificial intelligence in healthcare",
  "scenes": [
    "Scene 1: Open with a statistic showing AI adoption...",
    "Scene 2: Interview with healthcare professionals..."
  ],
  "googleDriveFileId": "1a2b3c4d5e6f7g8h9i0j",
  "googleDriveFileLink": "https://drive.google.com/file/d/1a2b3c4d5e6f7g8h9i0j/view",
  "message": "Scenes generated successfully and saved to Google Drive"
}
```

### Endpoint 2: Generate Scenes (GET - Simple)

**GET** `/api/v1/ideas/generate-scenes/{theme}?numberOfScenes=5`

Example:
```
GET /api/v1/ideas/generate-scenes/The%20future%20of%20AI?numberOfScenes=5
```

## Error Handling

- If Google Drive credentials are not configured, the endpoint will still generate scenes but fail to save to Drive
- Check the error message in the response for details
- View logs for detailed error information

## Testing

You can test the endpoints using:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **curl**:
  ```bash
  curl -X POST http://localhost:8080/api/v1/ideas/generate-scenes \
    -H "Content-Type: application/json" \
    -d '{
      "theme": "The future of AI in healthcare",
      "numberOfScenes": 5,
      "saveToGoogleDrive": false
    }'
  ```

## Troubleshooting

### "Google Drive credentials file not found"
- Make sure the file path is correct
- Use absolute paths for the credentials file

### "Failed to initialize Google Drive service"
- Verify the JSON credentials file is valid
- Check that the service account has Google Drive API access

### "Failed to upload to Google Drive"
- Ensure the service account email has access to the target folder
- Check that the folder ID (if provided) is valid
- Verify the Google Drive API is enabled in the Google Cloud Console

## Security Notes

- Never commit credentials files to version control
- Use environment variables in production
- Restrict the service account permissions to only what's needed
- Regularly audit service account access

