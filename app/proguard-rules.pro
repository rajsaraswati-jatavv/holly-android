# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in Android SDK tools.

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Vosk classes
-keep class com.alphacephei.vosk.** { *; }
-keep class org.vosk.** { *; }

# Keep Porcupine classes
-keep class ai.picovoice.porcupine.** { *; }

# Keep Accessibility Service
-keep class com.holly.assistant.service.HollyAccessibilityService { *; }

# Keep LLM JNI methods
-keep class com.holly.assistant.llm.LlamaEngine { *; }

# Keep data classes for Room
-keep class com.holly.assistant.data.model.** { *; }

# Keep all serialization
-keepattributes *Annotation*, Signature, Exceptions

# Optimization settings
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Optimization
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable

# Allow optimization
-allowaccessmodification

# Keep all public classes
-keep public class * {
    public protected *;
}

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
}
