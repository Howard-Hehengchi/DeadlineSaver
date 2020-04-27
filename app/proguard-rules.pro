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

-keep class org.litepal.** {
    *;
}

-keep class * extends org.litepal.crud.DataSupport {
    *;
}

-keep class * extends org.litepal.crud.LitePalSupport {
    *;
}

-keep class com.google.gson.** {*;}
-keep class com.google.**{*;}
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.deadlinesaver.android.gson.** { *; }

-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *;}
-dontwarn okio.**

-keep class com.sun.activation.registries.**{ *;}
-keep class javax.activation.**{ *;}
-keep class com.sun.mail.auth.**{ *;}
-keep class com.sun.mail.handlers.**{ *;}
-keep class com.sun.mail.iap.**{ *;}
-keep class com.sun.mail.imap.**{ *;}
-keep class com.sun.mail.imap.protocol.**{ *;}
-keep class com.sun.mail.pop3.**{ *;}
-keep class com.sun.mail.smtp.**{ *;}
-keep class com.sun.mail.util.**{ *;}
-keep class com.sun.mail.util.logging.**{ *;}
-keep class javax.mail.**{ *;}
-keep class javax.mail.internet.**{ *;}
-keep class javax.mail.event.**{ *;}
-keep class javax.search.**{ *;}
-keep class javax.util.**{ *;}