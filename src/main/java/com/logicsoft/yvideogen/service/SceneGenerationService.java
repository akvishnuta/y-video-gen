package com.logicsoft.yvideogen.service;

import com.logicsoft.yvideogen.exception.SceneGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SceneGenerationService {

    private final ChatModel chatModel;

    public List<String> generateScenes(String theme, int numberOfScenes, String userDescription) throws SceneGenerationException {
        try {
            log.info("Generating {} scenes for theme: {} (userDescription: {})", numberOfScenes, theme, userDescription);

            List<String> allScenes = new ArrayList<>();
            int batchSize = 10;
            int totalBatches = (int) Math.ceil((double) numberOfScenes / batchSize);

            for (int batch = 0; batch < totalBatches; batch++) {
                int scenesInThisBatch = Math.min(batchSize, numberOfScenes - (batch * batchSize));
                int sceneNumberStart = (batch * batchSize) + 1;
                
                log.info("Generating batch {} of {} ({} scenes)", batch + 1, totalBatches, scenesInThisBatch);
                
                String prompt = buildPrompt(theme, userDescription, scenesInThisBatch, sceneNumberStart, batch > 0);
                String response = chatModel.call(new Prompt(prompt)).getResult().getOutput().getText();

                List<String> batchScenes = parseScenes(response);
                allScenes.addAll(batchScenes);
                
                log.info("Batch {} completed with {} scenes", batch + 1, batchScenes.size());
            }

            log.info("Successfully generated {} scenes total", allScenes.size());
            return allScenes;
        } catch (Exception e) {
            log.error("Error generating scenes for theme: {}", theme, e);
            throw new SceneGenerationException("Failed to generate scenes: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(String theme, String userDescription, int numberOfScenes, int sceneNumberStart, boolean isContinuation) {
        String continuationContext = isContinuation 
            ? "This is a continuation of a story. Ensure consistency with the previously established character design, environment aesthetics, and narrative flow. "
            : "";
        
        String userDescriptionSection = "";
        if (userDescription != null && !userDescription.trim().isEmpty()) {
            userDescriptionSection = "USER DESCRIPTION (use this to influence scene details):\n" + userDescription.trim() + "\n\n";
        }

        String characterSketchSection = !isContinuation
            ? "CHARACTER SKETCH (FIRST CALL ONLY - ESTABLISH FOR ALL SCENES):\n" +
              "Before generating scenes, create a detailed character sketch including:\n" +
              "- Main character(s) name, age, and appearance (clothing style, hairstyle, build)\n" +
              "- Personality traits and emotional depth\n" +
              "- Consistent physical characteristics that will appear in every scene\n" +
              "- Any supporting characters with similar detail\n" +
              "This character design should be referenced in every scene's image prompt to ensure consistency.\n\n"
            : "";
        
        return String.format(
            "You are creating a calm, emotional, long-form YouTube story video using Ghibli-inspired cinematic visuals. The story idea is: %s\n\n" +
            "%s" +
            "Your task is to create a complete highly detailed AI image prompts and motion guidance that perfectly match a soft, hand-painted Ghibli-style aesthetic suitable for AI image generation and image-to-video conversion.\n\n" +
            "TONE AND STYLE REQUIREMENTS:\n" +
            "- Overall tone must be warm, peaceful, nostalgic, and slow-paced\n" +
            "- Absolutely NO fantasy elements, magic, action, or dramatic intensity\n" +
            "- Focus entirely on everyday life: family moments, home interiors, cooking, quiet travel, seasonal weather, and small human routines\n" +
            "- Characters should appear gentle, realistic, and consistent throughout all scenes\n" +
            "- Simple expressions and natural body language for all characters\n" +
            "- Environments should feel lived-in, cozy, and cinematic\n" +
            "- Soft lighting and painterly backgrounds are essential\n\n" +
            "%s" +
            "SCENE GENERATION INSTRUCTIONS:\n" +
            "%s" +
            "Generate exactly %d scenes for this story, starting from scene number %d.\n" +
            "Write a slow, emotionally grounded narration for each scene using simple language and short sentences.\n" +
            "Include NO dialogue - only reflective narration keeping each scene to two or three lines.\n\n" +
            "FORMAT FOR EACH SCENE:\n" +
            "Scene [number]: [Scene Title]\n" +
            "[Time of day and atmosphere/weather]\n" +
            "[2-3 lines of reflective narration]\n\n" +
            "IMAGE PROMPT FOR SCENE [number]:\n" +
            "[Full paragraph describing a complete Ghibli-inspired image prompt with hand-painted anime style, soft pastel color palette, warm natural lighting, detailed environmental descriptions, consistent character appearance (including clothing, age, and hairstyle), calm facial expressions, cozy and realistic surroundings, and cinematic framing using wide or medium shots with ultra-high-quality storybook aesthetic]\n\n" +
            "IMAGE-TO-VIDEO MOTION GUIDANCE FOR SCENE [number]:\n" +
            "[Describe slow and subtle camera movements for Google Flow/image-to-video conversion: gentle zooms, soft pans, light parallax motion. Scene duration: 4-6 seconds. Maintain calm, steady, peaceful mood.]\n" +
            "[Include necessary ambient sounds and background music cues. AVOID any human voice over.]\n\n" +
            "IMPORTANT CHARACTER CONSISTENCY:\n" +
            "The character design must remain EXACTLY the same across all scenes without variation in appearance, clothing, age, or hairstyle.\n\n" +
            "AUDIO GUIDANCE:\n" +
            "- NO human voice over at any point\n" +
            "- Include necessary sounds of actions (e.g., water pouring, footsteps, door opening)\n" +
            "- Add appropriate background music cues that match the Ghibli aesthetic\n" +
            "- Maintain peaceful, non-intrusive sound design\n\n" +
            "Now generate the following %d scenes:\n",
            theme,
            userDescriptionSection,
            characterSketchSection,
            continuationContext,
            numberOfScenes,
            sceneNumberStart,
            numberOfScenes
        );
    }

    private List<String> parseScenes(String response) {
        List<String> scenes = new ArrayList<>();
        String[] lines = response.split("\n");

        StringBuilder currentScene = new StringBuilder();
        int sceneCount = 0;

        for (String line : lines) {
            String trimmedLine = line.trim();

            if (trimmedLine.isEmpty()) {
                continue;
            }

            // Check if this line starts a new scene
            if (trimmedLine.toLowerCase().matches("^scene\\s+\\d+.*")) {
                if (currentScene.length() > 0) {
                    scenes.add(currentScene.toString().trim());
                    currentScene = new StringBuilder();
                }
                currentScene.append(trimmedLine);
                sceneCount++;
            } else if (sceneCount > 0) {
                if (currentScene.length() > 0) {
                    currentScene.append(" ");
                }
                currentScene.append(trimmedLine);
            }
        }

        // Don't forget the last scene
        if (currentScene.length() > 0) {
            scenes.add(currentScene.toString().trim());
        }

        if (scenes.isEmpty()) {
            // Fallback: split by lines if scene parsing fails
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && trimmed.length() > 10) {
                    scenes.add(trimmed);
                }
            }
        }

        return scenes;
    }
}

