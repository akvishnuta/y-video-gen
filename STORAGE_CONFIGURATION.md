# Storage Service Refactoring Documentation

## Overview
The project has been refactored to support multiple storage implementations through a common `StorageService` interface. This allows seamless switching between Google Drive and AWS S3 storage services.

## New Architecture

### 1. StorageService Interface
**File**: `src/main/java/com/logicsoft/yvideogen/service/StorageService.java`

A unified interface for all storage implementations with two main methods:
- `uploadContent(String content, String fileName)` - Upload generic content
- `uploadScenes(String theme, List<String> scenes)` - Upload generated video scenes

### 2. UploadResult Class
**File**: `src/main/java/com/logicsoft/yvideogen/service/UploadResult.java`

A common data transfer object for upload responses with fields:
- `fileId` - Unique identifier of the uploaded file
- `fileLink` - URL or path to the uploaded file
- `fileName` - Name of the uploaded file
- `storageType` - Type of storage (e.g., "GoogleDrive", "S3")
- `message` - Additional status message

### 3. GoogleDriveService (Refactored)
**File**: `src/main/java/com/logicsoft/yvideogen/service/GoogleDriveService.java`

Now implements `StorageService` interface while maintaining backward compatibility:
- Implements `uploadContent()` and `uploadScenes()` methods
- Returns `UploadResult` from new interface methods
- Maintains deprecated `GoogleDriveUploadResult` class for backward compatibility
- Keeps `uploadScenesToGoogleDrive()` method for legacy code

### 4. S3StorageService (New)
**File**: `src/main/java/com/logicsoft/yvideogen/service/S3StorageService.java`

New implementation using AWS S3:
- Implements `StorageService` interface
- Uploads content to AWS S3 bucket
- Only activates when S3 is properly configured
- Uses AWS SDK v2.28.3

### 5. AwsS3Config (New)
**File**: `src/main/java/com/logicsoft/yvideogen/config/AwsS3Config.java`

Spring configuration for AWS S3:
- Conditionally creates S3Client bean
- Only active when `aws.s3.enabled=true`
- Handles AWS credentials automatically

## Configuration

### Google Drive Configuration
Add to `application.properties` or `application-local.properties`:
```properties
google.drive.credentials.file=/path/to/drive_creds.json
google.drive.folder.id=optional_folder_id
```

### S3 Configuration
Add to `application.properties` or `application-local.properties`:
```properties
# Enable S3 storage
aws.s3.enabled=true

# S3 bucket name (required if S3 is enabled)
aws.s3.bucket-name=your-bucket-name

# AWS region (optional, defaults to us-east-1)
aws.s3.region=us-east-1

# AWS credentials should be configured via:
# 1. Environment variables: AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY
# 2. AWS credentials file: ~/.aws/credentials
# 3. IAM roles (if running on EC2)
```

## Request/Response Flow

### IdeaGenController Updates
**File**: `src/main/java/com/logicsoft/yvideogen/controller/IdeaGenController.java`

- Now supports both Google Drive and S3 storage
- Automatically selects the appropriate storage service
- Selection logic:
  1. If S3 is properly configured → Use S3StorageService
  2. Otherwise → Use GoogleDriveService (default)

### Example Request
```bash
curl -X POST http://localhost:8080/api/v1/ideas/generate-scenes \
  -H "Content-Type: application/json" \
  -d '{
    "theme": "Space Exploration",
    "numberOfScenes": 5,
    "saveToGoogleDrive": true
  }'
```

### Example Response
```json
{
  "theme": "Space Exploration",
  "scenes": [
    "Scene 1: A rocket launching...",
    "Scene 2: Astronauts in zero gravity...",
    ...
  ],
  "googleDriveFileId": "1a2b3c4d5e6f7g8h9i0j",
  "googleDriveFileLink": "https://drive.google.com/file/d/1a2b3c4d5e6f7g8h9i0j/view",
  "message": "Scenes generated successfully and saved to GoogleDrive"
}
```

## Backward Compatibility

### GoogleDriveTestController
Updated to use the new `UploadResult` class while maintaining all existing functionality. The deprecated `GoogleDriveUploadResult` class is still available but marked as `@Deprecated` for legacy code.

### Existing Code
- Old code using `GoogleDriveService.uploadScenesToGoogleDrive()` continues to work
- The method now returns `UploadResult` instead of deprecated `GoogleDriveUploadResult`

## Dependencies Added

### AWS SDK for S3
Added to `build.gradle`:
```gradle
implementation 'software.amazon.awssdk:s3:2.28.3'
implementation 'software.amazon.awssdk:aws-core:2.28.3'
```

## Usage Examples

### Using Google Drive (Default)
```java
StorageService storageService = googleDriveService;
UploadResult result = storageService.uploadScenes("My Theme", scenesList);
System.out.println("File ID: " + result.getFileId());
System.out.println("File Link: " + result.getFileLink());
```

### Using S3
```java
StorageService storageService = s3StorageService;
UploadResult result = storageService.uploadScenes("My Theme", scenesList);
System.out.println("S3 URL: " + result.getFileLink());
```

### Automatic Selection (Recommended)
```java
// IdeaGenController automatically selects the best storage service
private StorageService selectStorageService(SceneGenerationRequest request) {
    if (s3StorageService.isConfigured()) {
        return s3StorageService;
    } else {
        return googleDriveService;
    }
}
```

## Testing

### Test Google Drive Connection
```bash
curl http://localhost:8080/api/v1/google-drive-test/upload-simple
```

### Test S3 Connection
Before testing S3, ensure:
1. AWS credentials are configured
2. `aws.s3.enabled=true` is set
3. S3 bucket exists and is accessible

## Error Handling

### Google Drive Errors
- Missing credentials file → 500 error with detailed message
- Credentials not found → 500 error
- Upload failure → Message logged, response formatted properly

### S3 Errors
- S3 disabled → RuntimeException with clear message
- Missing bucket name → RuntimeException
- S3 client unavailable → RuntimeException
- AWS authentication failure → Exception with AWS error details

## Future Extensions

This architecture supports adding more storage implementations:

1. Create a new class implementing `StorageService`
2. Implement `uploadContent()` and `uploadScenes()` methods
3. Add configuration class if needed
4. Update `IdeaGenController.selectStorageService()` with logic for the new service

Example implementations could include:
- Azure Blob Storage
- DigitalOcean Spaces
- MinIO (self-hosted S3-compatible storage)
- Local file system storage

## Files Modified/Created

### New Files
- `src/main/java/com/logicsoft/yvideogen/service/StorageService.java`
- `src/main/java/com/logicsoft/yvideogen/service/UploadResult.java`
- `src/main/java/com/logicsoft/yvideogen/service/S3StorageService.java`
- `src/main/java/com/logicsoft/yvideogen/config/AwsS3Config.java`
- `STORAGE_CONFIGURATION.md` (this file)

### Modified Files
- `src/main/java/com/logicsoft/yvideogen/service/GoogleDriveService.java`
- `src/main/java/com/logicsoft/yvideogen/controller/IdeaGenController.java`
- `src/main/java/com/logicsoft/yvideogen/controller/GoogleDriveTestController.java`
- `build.gradle`

### Preserved Files (No Removal)
- `GoogleDriveService.java` - Still functional with new interface
- `GoogleDriveUploadResult` - Marked as deprecated, still available
- All existing controllers and services - Unchanged functionality

## Notes

- The refactoring maintains 100% backward compatibility
- All existing endpoints work as before
- The old `GoogleDriveUploadResult` class is preserved for legacy code
- Google Drive remains the default storage if S3 is not configured
- AWS SDK dependencies are only used if S3 is enabled

