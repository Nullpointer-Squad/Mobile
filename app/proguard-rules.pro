# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepclassmembers class **$WhenMappings {
    <fields>;
}
-dontwarn kotlin.**
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }

#remote repository entities
-keep class com.nullpointer.squad.domain.model.** { *; }

#hilt
-keep class dagger.hilt.** { *; }
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel
-keepclassmembers class * {
    @javax.inject.* *;
    @dagger.* *;
    <init>(...);
}

#navigation
-keep class androidx.navigation.fragment.NavHostFragment
-keep class navigation.** { *; }
-keep class com.google.gson.** { *; }

# Keep class members that Gson uses for type information
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Specifically keep the type information for List<String>
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken

#retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Keep all public classes that extend Activity, Fragment, Service, etc., and their public methods
        -keep public class * extends android.app.Activity
        -keep public class * extends androidx.fragment.app.Fragment
        -keep public class * extends android.app.Service
        # ... and so on for other Android components

        # Keep custom views and their constructors
        -keep public class * extends android.view.View {
            public <init>(android.content.Context);
            public <init>(android.content.Context, android.util.AttributeSet);
            public <init>(android.content.Context, android.util.AttributeSet, int);
            public void set*(...);
        }

        # Keep parcelable classes
        -keep class * implements android.os.Parcelable {
          public static final android.os.Parcelable$Creator *;
        }

        # Keep enums used in Animators
        -keepclassmembers enum * {
            public static **[] values();
            public static ** valueOf(java.lang.String);
        }

        # Keep R class members (though usually handled by AAPT)
        -keepclassmembers class **.R$* {
            public static <fields>;
        }
