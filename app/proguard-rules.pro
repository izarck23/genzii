# Add project specific ProGuard rules here.
-dontwarn **
-dontoptimize
-dontobfuscate
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Compose
-keep class androidx.compose.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class androidx.room.** { *; }
-keep class com.example.data.local.** { *; }

# Models
-keep class com.example.data.model.** { *; }

# Moshi & Retrofit
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}
-keep class com.squareup.moshi.** { *; }
-keep class retrofit2.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Firebase & Play Services
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

