# SoCreate ProGuard Rules

# Keep all serializable models (kotlinx.serialization)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.socreate.app.core.model.**$$serializer { *; }
-keepclassmembers class com.socreate.app.core.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.socreate.app.core.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Disable warning for kotlinx.coroutines
-dontwarn kotlinx.coroutines.**
