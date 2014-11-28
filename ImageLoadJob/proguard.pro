-injars 'libs\imageloadjob.jar'
-outjars 'libs\imagejob.jar'

-libraryjars 'F:\Dev\android-sdk-windows\platforms\android-19\android.jar'



-optimizationpasses 5
-dontusemixedcaseclassnames
-ignorewarning
-dontskipnonpubliclibraryclasses
#-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-dontwarn android.support.v4.** 
-dontwarn **CompatHoneycomb
-dontwarn **CompatHoneycombMR2
-dontwarn **CompatCreatorHoneycombMR2

-keep interface android.support.v4.app.** { *; }
-keep class android.support.v4.** { *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment
-keep class android.app.** { *; }

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    public static <ObtainJob>(...);
}
