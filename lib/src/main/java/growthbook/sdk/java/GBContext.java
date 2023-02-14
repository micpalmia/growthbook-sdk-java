package growthbook.sdk.java;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Context object passed into the GrowthBook constructor.
 * The {@link GBContext#builder()} is recommended for constructing a Context.
 * Alternatively, you can use the class's constructor.
 */
@Data
public class GBContext {

    /**
     * The {@link GBContextBuilder} is recommended for constructing a Context.
     * Alternatively, you can use this static method instead of the builder.
     * @param attributesJson User attributes as JSON string
     * @param featuresJson Features response as JSON string, or the encrypted payload. Encrypted payload requires `encryptionKey`
     * @param encryptionKey Optional encryption key. If this is not null, featuresJson should be an encrypted payload.
     * @param enabled Whether globally all experiments are enabled. Defaults to true.
     * @param isQaMode If true, random assignment is disabled and only explicitly forced variations are used.
     * @param url A URL string
     * @param forcedVariationsMap Force specific experiments to always assign a specific variation (used for QA)
     * @param trackingCallback A function that takes {@link Experiment} and {@link ExperimentResult} as arguments.
     */
    @Builder
    public GBContext(
            @Nullable String attributesJson,
            @Nullable String featuresJson,
            @Nullable String encryptionKey,
            @Nullable Boolean enabled,
            Boolean isQaMode,
            @Nullable String url,
            @Nullable Map<String, Integer> forcedVariationsMap,
            @Nullable TrackingCallback trackingCallback
    ) {
        this.encryptionKey = encryptionKey;

        this.attributesJson = attributesJson == null ? "{}" : attributesJson;

        if (featuresJson == null) {
            this.featuresJson = "{}";
        } else if (encryptionKey != null) {
            this.featuresJson = DecryptionUtils.decrypt(featuresJson, encryptionKey).trim();
        } else {
            this.featuresJson = featuresJson;
        }

        this.enabled = enabled == null ? true : enabled;
        this.isQaMode = isQaMode == null ? false : isQaMode;
        this.url = url;
        this.forcedVariationsMap = forcedVariationsMap == null ? new HashMap<>() : forcedVariationsMap;
        this.trackingCallback = trackingCallback;
    }

    @Nullable
    @Getter(AccessLevel.PACKAGE)
    private JsonObject features;

    private void setFeatures(@Nullable JsonObject features) {
        this.features = features;
    }

    @Nullable
    private Boolean enabled;

    @Nullable
    private String url;

    private Boolean isQaMode;

    @Nullable
    private TrackingCallback trackingCallback;

    @Nullable
    private String attributesJson;

    /**
     * You can update the attributes JSON with new user attributes to evaluate against.
     * @param attributesJson updated user attributes
     */
    public void setAttributesJson(String attributesJson) {
        this.attributesJson = attributesJson;
        if (attributesJson != null) {
            this.setAttributes(GBContext.transformAttributes(attributesJson));
        }
    }

    @Nullable
    @Getter(AccessLevel.PACKAGE)
    private JsonObject attributes;

    private void setAttributes(@Nullable JsonObject attributes) {
        this.attributes = attributes;
    }

    @Nullable
    private String featuresJson;

    @Nullable
    private String encryptionKey;

    /**
     * You can update the features JSON with new features to evaluate against.
     * @param featuresJson updated features
     */

    public void setFeaturesJson(String featuresJson) {
        this.featuresJson = featuresJson;
        if (featuresJson != null) {
            this.setFeatures(GBContext.transformFeatures(featuresJson));
        }
    }

    @Nullable
    private Map<String, Integer> forcedVariationsMap;

    /**
     * The builder class to help create a context. You can use {@link #builder()} or the {@link GBContext} constructor
     */
    public static class GBContextBuilder {} // This stub is required for JavaDoc and is filled by Lombuk

    /**
     * The builder class to help create a context. You can use this builder or the constructor
     * @return {@link CustomGBContextBuilder}
     */
    public static GBContextBuilder builder() {
        return new CustomGBContextBuilder();
    }

    static class CustomGBContextBuilder extends GBContextBuilder {
        @Override
        public GBContext build() {
            GBContext context = super.build();

            if (context.featuresJson != null) {
                context.setFeatures(GBContext.transformFeatures(context.featuresJson));
            }

            context.setAttributesJson(context.attributesJson);

            return context;
        }
    }

    @Nullable
    private static JsonObject transformFeatures(String featuresJsonString) {
        try {
            return GrowthBookJsonUtils.getInstance().gson.fromJson(featuresJsonString, JsonObject.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JsonObject transformAttributes(@Nullable String attributesJsonString) {
        try {
            if (attributesJsonString == null) {
                return new JsonObject();
            }

            JsonElement element = GrowthBookJsonUtils.getInstance().gson.fromJson(attributesJsonString, JsonElement.class);
            if (element == null || element.isJsonNull()) {
                return new JsonObject();
            }

            return GrowthBookJsonUtils.getInstance().gson.fromJson(attributesJsonString, JsonObject.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonObject();
        }
    }
}
