# Add project specific ProGuard rules here.
# Keep domain models and Room entities safe during R8 shrinking.
-keep class com.virtue.habittracker.domain.model.** { *; }
-keep class com.virtue.habittracker.data.local.** { *; }

# Keep Firebase Auth and Firestore classes used via reflection or callbacks.
-keepnames class com.google.firebase.** { *; }
-dontwarn com.google.errorprone.annotations.**
