# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Basic optimization settings
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# Keep line numbers for debugging crashes
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep your main classes
-keep public class com.mnazar.eyedropreminder.MainActivity
-keep public class com.mnazar.eyedropreminder.AlarmActivity
-keep public class com.mnazar.eyedropreminder.ReminderReceiver

# Keep all classes that extend Activity
-keep public class * extends android.app.Activity
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends androidx.appcompat.app.AppCompatActivity

# Keep Android system callbacks
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

-keepclassmembers class * extends android.content.BroadcastReceiver {
    public void onReceive(...);
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable classes
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep MediaPlayer related classes
-keep class android.media.** { *; }
-keep class android.media.MediaPlayer { *; }
-keep class android.media.RingtoneManager { *; }

# Keep Vibrator classes
-keep class android.os.Vibrator { *; }

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Keep AndroidX components
-keep class androidx.** { *; }
-dontwarn androidx.**

# Keep Google Material Design components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# General Android optimizations
-dontwarn android.support.**
-dontwarn org.apache.http.**
-dontwarn android.net.http.AndroidHttpClient
-dontwarn com.android.volley.toolbox.**