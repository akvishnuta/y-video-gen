# Y Video Gen - Setup Guide

## Configuring OpenAI API Key

The error you encountered means the OpenAI API key is not configured. Follow one of the methods below to set it up:

### Method 1: Environment Variable (Recommended for Development)

Set the API key as a system environment variable:

```bash
# macOS/Linux
export SPRING_AI_OPENAI_API_KEY=sk-...your-key-here...

# Then run the application
./gradlew bootRun
```

**Windows (PowerShell):**
```powershell
$env:SPRING_AI_OPENAI_API_KEY="sk-...your-key-here..."
./gradlew.bat bootRun
```

### Method 2: Local Configuration File (Recommended for Local Development)

1. Copy the example configuration:
```bash
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

2. Edit `src/main/resources/application-local.properties` and add your OpenAI API key:
```properties
spring.ai.openai.api-key=sk-...your-key-here...
```

3. Run with the local profile:
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Method 3: application.properties (NOT Recommended)

Edit `src/main/resources/application.properties` and replace the placeholder:
```properties
spring.ai.openai.api-key=sk-...your-key-here...
```

⚠️ **WARNING**: Do NOT commit your API key to git! Always use environment variables or local configuration files excluded from git.

### Method 4: Command Line Argument

```bash
./gradlew bootRun --args='--spring.ai.openai.api-key=sk-...your-key-here...'
```

## Getting an OpenAI API Key

1. Go to [OpenAI Platform](https://platform.openai.com/account/api-keys)
2. Sign in or create an account
3. Click "Create new secret key"
4. Copy the key (it will only be shown once)
5. Use one of the methods above to configure it

## Verify Configuration

Once configured, run the application:

```bash
./gradlew bootRun
```

You should see output like:
```
Started YvideogenApplication in 3.5 seconds
```

Access the Swagger UI to verify it's working:
```
http://localhost:8080/swagger-ui.html
```

If you get an error, check:
- ✅ API key is correctly set
- ✅ API key has valid permissions
- ✅ Check your API key has not expired
- ✅ Verify you have API credits available

## Troubleshooting

### Error: "At least one credential source must be specified"
- The API key is not set. Follow Method 1, 2, or 3 above.

### Error: "Invalid API Key"
- Check that your API key is correct (should start with `sk-`)
- Verify the key hasn't been revoked in OpenAI dashboard

### Error: "Rate limit exceeded"
- You may have exceeded your API usage limit
- Check your usage and limits at https://platform.openai.com/account/billing/limits

### Error: "Model not available"
- Ensure you have access to the configured model (default: gpt-4)
- Update `spring.ai.openai.chat.options.model` to use a different model

## Production Deployment

For production, use secure methods:

1. **Environment Variables** (recommended)
   - Set `SPRING_AI_OPENAI_API_KEY` in your deployment environment
   - Most cloud platforms (AWS, Azure, Heroku, Vercel) have secure ways to set env vars

2. **Application Secrets Management**
   - Use AWS Secrets Manager, Azure Key Vault, or similar services
   - Configure Spring Cloud Config

3. **Never hardcode credentials** in your codebase

## Next Steps

Once configured, you can:
- Access Swagger UI: `http://localhost:8080/swagger-ui.html`
- Create video generation endpoints
- Integrate AI features into your controllers
- Test API endpoints via Swagger UI

## Configuring Google Drive Integration (Optional)

To enable automatic saving of generated scenes to Google Drive, follow the setup in [GOOGLE_DRIVE_SETUP.md](./GOOGLE_DRIVE_SETUP.md).

For more details, see the main [README.md](../README.md)

