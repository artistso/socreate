# SoCreate ProGuard Rules

# ─── Kotlin Serialization ─────────────────────────────────────────────
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

# ─── Hilt / Dagger ────────────────────────────────────────────────────
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ─── Native Methods ───────────────────────────────────────────────────
-keepclasseswithmembernames class * {
    native <methods>;
}

# ─── Room ─────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# ─── Coroutines ───────────────────────────────────────────────────────
-dontwarn kotlinx.coroutines.**

# ─── Google Play Services / YouTube ───────────────────────────────────
-dontwarn com.google.api.client.**
-dontwarn com.google.http.client.**
-keep class com.google.api.services.youtube.** { *; }

# ─── OkHttp ───────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
