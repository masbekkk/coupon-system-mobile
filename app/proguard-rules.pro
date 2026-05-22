# ──────────────────────────────────────────────────────────────
# Coupon System — ProGuard / R8 Rules for minimum APK size
# ──────────────────────────────────────────────────────────────

# ── General ──
-optimizationpasses 5
-dontusemixedcaseclassnames
-verbose
-allowaccessmodification
-repackageclasses ''

# ── Kotlin ──
-dontwarn kotlin.**
-dontwarn kotlinx.**
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod

# ── Retrofit + OkHttp ──
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keepattributes Exceptions

# ── Gson ──
-dontwarn com.google.gson.**
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep all API model data classes (Gson needs field names for serialization)
-keep class com.masbek.couponsystem.data.model.** { *; }

# ── Hilt / Dagger ──
-dontwarn dagger.**
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ── AndroidX Navigation ──
-keepnames class androidx.navigation.fragment.NavHostFragment
-keep class * extends androidx.fragment.app.Fragment { *; }

# ── AndroidX Security (EncryptedSharedPreferences) ──
-keep class androidx.security.crypto.** { *; }

# ── ViewBinding ──
-keep class * implements androidx.viewbinding.ViewBinding {
    public static * inflate(android.view.LayoutInflater);
    public static * inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
}

# ── Enum ──
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Parcelable ──
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ── Serializable ──
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ── Remove logging in release ──
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}