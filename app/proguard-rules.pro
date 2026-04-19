# Keep source file names and line numbers for crash logs
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin metadata (required for default parameter constructors)
-keepattributes RuntimeVisibleAnnotations
-keep class kotlin.Metadata { *; }
-keep class kotlin.jvm.internal.DefaultConstructorMarker

# Moshi: keep all @JsonClass-annotated classes and their members
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**