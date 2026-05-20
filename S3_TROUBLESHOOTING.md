# S3 Storage Troubleshooting Guide

## Issue: S3 Endpoint Error (Status Code 301)

### Error Message
```
software.amazon.awssdk.services.s3.model.S3Exception: 
The bucket you are attempting to access must be addressed using 
the specified endpoint. Please send all future requests to this endpoint.
(Service: S3, Status Code: 301, ...)
```

### Root Cause
This error occurs when the **AWS region configured in your application doesn't match the region where your S3 bucket actually exists**. AWS S3 redirects the request (HTTP 301 redirect) to the correct regional endpoint.

---

## Solution: Configure the Correct Region

### Step 1: Identify Your Bucket's Region

First, determine which region your S3 bucket is in:

#### Option A: Using AWS Console
1. Go to [AWS S3 Console](https://s3.console.aws.amazon.com/s3/buckets)
2. Click on your bucket name
3. Go to the "Properties" tab
4. Look for "AWS Region" at the top

#### Option B: Using AWS CLI
```bash
aws s3api get-bucket-location --bucket your-bucket-name
```

Output example:
```json
{
    "LocationConstraint": "us-west-2"
}
```

If `LocationConstraint` is `null`, the bucket is in **us-east-1**

#### Option C: Using AWS SDK (Java)
```java
HeadBucketResponse response = s3Client.headBucket(
    HeadBucketRequest.builder().bucket(bucketName).build()
);
String region = response.responseMetadata().httpHeaders().get("x-amz-bucket-region");
```

### Step 2: Update Your Application Configuration

Add the correct region to your `application.properties` or `application-local.properties`:

```properties
# Set aws.s3.region to match your bucket's region
aws.s3.enabled=true
aws.s3.bucket-name=your-bucket-name
aws.s3.region=us-west-2
```

### Common AWS Regions

| Region Code | Region Name | Best For |
|----------|-----------|----------|
| `us-east-1` | N. Virginia | US East Coast, default region |
| `us-west-2` | Oregon | US West Coast |
| `eu-west-1` | Ireland | Europe |
| `eu-central-1` | Frankfurt | Central Europe |
| `ap-southeast-1` | Singapore | Southeast Asia |
| `ap-northeast-1` | Tokyo | Japan |
| `ap-south-1` | Mumbai | India |
| `ca-central-1` | Canada | Canada |

---

## Complete Configuration Examples

### Example 1: Bucket in us-west-2
```properties
aws.s3.enabled=true
aws.s3.bucket-name=my-video-scenes-bucket
aws.s3.region=us-west-2

# AWS credentials (use environment variables instead)
# export AWS_ACCESS_KEY_ID=your_key
# export AWS_SECRET_ACCESS_KEY=your_secret
```

### Example 2: Bucket in eu-west-1
```properties
aws.s3.enabled=true
aws.s3.bucket-name=my-video-bucket-eu
aws.s3.region=eu-west-1
```

### Example 3: Bucket in us-east-1 (default region)
```properties
aws.s3.enabled=true
aws.s3.bucket-name=my-video-bucket
aws.s3.region=us-east-1
```

---

## Verification Steps

### Step 1: Check Configuration
Add these properties and verify they're loaded:
```bash
# Check application logs during startup
grep -i "Initializing AWS S3 Client" logs.txt
```

You should see:
```
INFO  com.logicsoft.yvideogen.config.AwsS3Config - 
      Initializing AWS S3 Client with region: us-west-2
```

### Step 2: Test Connectivity
```bash
# Use AWS CLI to test bucket access
aws s3 ls s3://your-bucket-name --region us-west-2
```

If successful, you'll see the bucket contents.

### Step 3: Test Application
```bash
curl -X POST http://localhost:8080/api/v1/ideas/generate-scenes \
  -H "Content-Type: application/json" \
  -d '{
    "theme": "Test Theme",
    "numberOfScenes": 2,
    "saveToGoogleDrive": true
  }'
```

Check the logs for:
```
INFO  com.logicsoft.yvideogen.service.S3StorageService - 
      Uploading content to S3 bucket: my-bucket in region: us-west-2
```

---

## Debugging Tips

### Enable Debug Logging

Add to `application.properties`:
```properties
logging.level.software.amazon.awssdk=DEBUG
logging.level.com.logicsoft.yvideogen=DEBUG
```

This will show detailed AWS SDK logs including:
- Request endpoints
- Region resolution
- HTTP redirects

### Check Region Configuration at Runtime

Add this temporary test endpoint:

```java
@GetMapping("/debug/s3-config")
public ResponseEntity<Map<String, String>> getS3Config() {
    return ResponseEntity.ok(Map.of(
        "s3Enabled", String.valueOf(s3Enabled),
        "bucketName", bucketName != null ? bucketName : "not-set",
        "region", region != null ? region : "not-set",
        "s3ClientPresent", String.valueOf(s3Client.isPresent())
    ));
}
```

Test it:
```bash
curl http://localhost:8080/debug/s3-config
```

### Common Configuration Mistakes

❌ **Wrong**: Region not specified
```properties
aws.s3.enabled=true
aws.s3.bucket-name=my-bucket
# Missing: aws.s3.region=us-west-2
```
**Fix**: Always specify the region explicitly

❌ **Wrong**: Region format incorrect
```properties
aws.s3.region=US-West-2  # Case sensitive!
aws.s3.region=us_west_2  # Use hyphens, not underscores
```
**Fix**: Use lowercase with hyphens (e.g., `us-west-2`)

❌ **Wrong**: Bucket name has special characters
```properties
aws.s3.bucket-name=My-Bucket  # Bucket names should be lowercase
```
**Fix**: Use lowercase bucket names

---

## Advanced: Multi-Region Setup

If you need to support multiple regions, you can:

### Option 1: Environment-Specific Configuration
```properties
# application-dev.properties
aws.s3.region=us-east-1
aws.s3.bucket-name=dev-bucket

# application-prod.properties
aws.s3.region=us-west-2
aws.s3.bucket-name=prod-bucket
```

Run with: `java -jar app.jar --spring.profiles.active=prod`

### Option 2: Dynamic Region Resolution
Modify `AwsS3Config`:
```java
@Bean
public S3Client s3Client() {
    String inlineRegion = System.getenv("AWS_REGION");
    if (inlineRegion == null) {
        inlineRegion = awsRegion;
    }
    log.info("Using region: {}", inlineRegion);
    return S3Client.builder()
            .region(Region.of(inlineRegion))
            .build();
}
```

Then set: `export AWS_REGION=us-west-2`

---

## Fallback to Google Drive

If S3 configuration keeps failing, the application will automatically fall back to Google Drive:

```properties
# S3 will be attempted first if configured
aws.s3.enabled=true
aws.s3.bucket-name=my-bucket
aws.s3.region=us-west-2

# If S3 fails during use, Google Drive will be the fallback
google.drive.credentials.file=/path/to/drive_creds.json
```

Check logs to see which service is being used:
```
Using S3 storage service
# or
Using Google Drive storage service
```

---

## Related Documentation

- **AWS SDK Region Documentation**: https://docs.aws.amazon.com/general/latest/gr/rande.html
- **S3 API Reference**: https://docs.aws.amazon.com/s3/latest/API/Welcome.html
- **AWS CLI S3 Commands**: https://docs.aws.amazon.com/cli/latest/userguide/cli-services-s3.html

---

## Quick Checklist

- [ ] Identified correct region for your bucket
- [ ] Added `aws.s3.region=<correct-region>` to properties
- [ ] Verified region format is lowercase with hyphens (e.g., `us-west-2`)
- [ ] Verified `aws.s3.bucket-name` matches exactly (case-sensitive)
- [ ] Restarted the application
- [ ] Tested with AWS CLI: `aws s3 ls s3://bucket-name --region region-code`
- [ ] Checked application logs for region confirmation
- [ ] Tested endpoint: POST `/api/v1/ideas/generate-scenes`

---

## Still Having Issues?

If you're still experiencing problems:

1. **Check AWS credentials** - Ensure AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY are set
2. **Verify bucket exists** - Confirm bucket exists in the specified region
3. **Check IAM permissions** - Ensure credentials have `s3:PutObject` permission
4. **Enable debug logging** - Set `logging.level.software.amazon.awssdk=DEBUG`
5. **Check firewall/proxy** - Ensure outbound HTTPS to S3 endpoint is allowed
6. **Verify bucket name** - Bucket names are case-sensitive and must be valid

### Error Message Examples and Solutions

| Error | Likely Cause | Solution |
|-------|------------|----------|
| Status Code 301 | Wrong region | Set `aws.s3.region` correctly |
| 403 Forbidden | Wrong credentials or permissions | Check IAM permissions |
| 404 NoSuchBucket | Bucket doesn't exist or wrong name | Verify bucket name and region |
| Connection timeout | Firewall/network issue | Check outbound HTTPS connectivity |
| InvalidBucketName | Bucket name invalid | Use lowercase, alphanumeric + hyphens |

---

## Updated Configuration Files

Make sure your `application-local.properties` includes:

```properties
#=======================================
# AWS S3 Configuration
#=======================================
aws.s3.enabled=true
aws.s3.bucket-name=your-bucket-name-here
aws.s3.region=your-bucket-region-here

# AWS credentials configured via:
# 1. Environment variables: AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY
# 2. ~/.aws/credentials file
# 3. IAM role (if on AWS)

#=======================================
# Google Drive Configuration (Fallback)
#=======================================
google.drive.credentials.file=/path/to/drive_creds.json
google.drive.folder.id=optional_folder_id
```

---

**Last Updated**: May 20, 2026
**Version**: 1.0
**Status**: ✅ S3 Configuration Fix Applied

