# S3 Region Configuration Fix - Summary

## Problem Fixed
**Error**: HTTP 301 Redirect - "The bucket you are attempting to access must be addressed using the specified endpoint"

**Cause**: The S3Client was not being configured with the correct AWS region, causing requests to the wrong endpoint.

## Changes Made

### 1. Updated AwsS3Config.java
**File**: `src/main/java/com/logicsoft/yvideogen/config/AwsS3Config.java`

**What Changed**:
- Added `@Value` annotation to read `aws.s3.region` from properties
- Updated S3Client builder to include `.region(region)` configuration
- Added error handling with fallback to default configuration
- Made region parameter configurable

**Before**:
```java
@Bean
public S3Client s3Client() {
    log.info("Initializing AWS S3 Client");
    return S3Client.builder().build();  // No region specified!
}
```

**After**:
```java
@Bean
public S3Client s3Client() {
    log.info("Initializing AWS S3 Client with region: {}", awsRegion);
    Region region = Region.of(awsRegion);
    return S3Client.builder()
            .region(region)  // Region now specified
            .build();
}
```

### 2. Enhanced S3StorageService.java
**File**: `src/main/java/com/logicsoft/yvideogen/service/S3StorageService.java`

**What Changed**:
- Added specific handling for S3Exception with status code 301
- Added helpful error message when region mismatch is detected
- Added region information to log messages
- Added validation warning for missing region configuration

**Key Improvements**:
```java
} catch (software.amazon.awssdk.services.s3.model.S3Exception e) {
    if (e.statusCode() == 301 || e.awsErrorDetails().errorMessage().contains("endpoint")) {
        throw new RuntimeException(
            "S3 endpoint error: The bucket may be in a different region than configured. " +
            "Set 'aws.s3.region' to the correct region (e.g., us-west-2, eu-west-1). " +
            "Error: " + e.awsErrorDetails().errorMessage(), e);
    }
    throw new RuntimeException("Failed to upload to S3: " + e.getMessage(), e);
}
```

## How to Use the Fix

### Configuration Required

Update your `application-local.properties` or `application.properties`:

```properties
# Enable S3 storage
aws.s3.enabled=true

# Your S3 bucket name
aws.s3.bucket-name=your-bucket-name

# IMPORTANT: Set the region where your bucket is located
aws.s3.region=us-west-2

# AWS credentials (use environment variables)
# export AWS_ACCESS_KEY_ID=your_key
# export AWS_SECRET_ACCESS_KEY=your_secret
```

### Step-by-Step Configuration

1. **Find your bucket's region**:
   ```bash
   aws s3api get-bucket-location --bucket your-bucket-name
   ```

2. **Set the region in configuration**:
   - If region is `null` → use `us-east-1`
   - Otherwise, use the returned region code
   - Format: lowercase with hyphens (e.g., `us-west-2`)

3. **Restart the application**
   - The fix will now use the correct regional endpoint

4. **Verify it works**:
   - Check logs for: `Initializing AWS S3 Client with region: <your-region>`
   - Test with an API call to generate scenes

## Common Region Values

| Location | Region Code |
|----------|-----------|
| US East (N. Virginia) | `us-east-1` |
| US West (Oregon) | `us-west-2` |
| Europe (Ireland) | `eu-west-1` |
| Europe (Frankfurt) | `eu-central-1` |
| Asia Pacific (Tokyo) | `ap-northeast-1` |
| Asia Pacific (Singapore) | `ap-southeast-1` |
| Asia Pacific (Mumbai) | `ap-south-1` |

## Verification

### Check Application Logs
Look for this log message on startup:
```
Initializing AWS S3 Client with region: us-west-2
```

### Test with AWS CLI
```bash
aws s3 ls s3://your-bucket-name --region us-west-2
```

### Test with Application
```bash
curl -X POST http://localhost:8080/api/v1/ideas/generate-scenes \
  -H "Content-Type: application/json" \
  -d '{
    "theme": "Test Theme",
    "numberOfScenes": 2,
    "saveToGoogleDrive": true
  }'
```

## What Still Works

- ✅ Google Drive storage (unchanged)
- ✅ All existing endpoints
- ✅ Automatic fallback to Google Drive if S3 fails
- ✅ Scene generation
- ✅ Configuration-based service selection

## Files Modified

1. **AwsS3Config.java**
   - Reader region from properties
   - Pass region to S3Client builder
   - Added error handling

2. **S3StorageService.java**
   - Better error messages for 301 redirects
   - Region information in log messages
   - Specific exception handling for endpoint errors

## Build Status

✅ **Builds Successfully**
- No compilation errors
- All dependencies properly resolved
- Ready for deployment

## Migration Notes

**If you were already using S3 before this fix**:

1. Check your bucket's actual region
2. Add `aws.s3.region=<actual-region>` to config
3. Restart the application
4. Test with scene generation

The fix is backward compatible - existing Google Drive users won't be affected.

## Troubleshooting

If you still see the 301 error after applying this fix:

1. Verify the region code is correct
   ```bash
   aws s3api get-bucket-location --bucket your-bucket-name
   ```

2. Ensure region format is lowercase with hyphens
   - ✅ Correct: `us-west-2`
   - ❌ Wrong: `US-West-2` or `us_west_2`

3. Check that bucket access works with AWS CLI
   ```bash
   aws s3 ls s3://your-bucket-name --region <region>
   ```

4. Enable debug logging:
   ```properties
   logging.level.software.amazon.awssdk=DEBUG
   ```

## Related Files

- `S3_TROUBLESHOOTING.md` - Complete troubleshooting guide
- `S3_CONFIGURATION_EXAMPLE.properties` - Configuration template
- `STORAGE_CONFIGURATION.md` - Full storage architecture documentation

---

**Status**: ✅ Fixed and Tested
**Build**: Successful
**Deployment**: Ready

