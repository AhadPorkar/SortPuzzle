# --- kotlinx.serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.parsgames.sortpuzzle.** {
    *** Companion;
}
-keepclasseswithmembers class com.parsgames.sortpuzzle.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.parsgames.sortpuzzle.**$$serializer { *; }

# --- Google Play Billing ---
-keep class com.android.billingclient.** { *; }

# --- Google Mobile Ads ---
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.**
