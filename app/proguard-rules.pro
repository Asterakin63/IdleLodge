# kotlinx.serialization — keep all @Serializable classes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.idlelodge.**$$serializer { *; }
-keepclassmembers class com.idlelodge.** {
    *** Companion;
}
-keepclasseswithmembers class com.idlelodge.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room — keep generated DAOs
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Hilt — keep generated components
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class *

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keepclassmembers class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
