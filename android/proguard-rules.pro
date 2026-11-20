# ---- libGDX: native code calls back into these classes by name ----
-keep class com.badlogic.gdx.** { *; }
-dontwarn com.badlogic.gdx.**
-keepclassmembers class * implements com.badlogic.gdx.utils.Json$Serializable { *; }

# ---- WebSocket client (+ optional slf4j logging it references) ----
-keep class org.java_websocket.** { *; }
-dontwarn org.java_websocket.**
-dontwarn org.slf4j.**

# ---- Cafe Bazaar billing (Poolakey + Bazaar's AIDL interface) ----
-keep class ir.cafebazaar.poolakey.** { *; }
-keep class com.farsitel.bazaar.** { *; }
-dontwarn ir.cafebazaar.poolakey.**

# Keep line numbers so crash reports from the store are readable.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
