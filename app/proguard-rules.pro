# Changjie IME ProGuard rules

# Keep the IME service entry point
-keep class com.wanleung.android.Changjie.ChangjieIME { *; }

# Keep all public Activity classes
-keep class com.wanleung.android.Changjie.MainActivity { *; }
-keep class com.wanleung.android.Changjie.SettingsActivity { *; }

# Keep custom views (referenced by name in XML layouts)
-keep class com.wanleung.android.Changjie.CandidateView { *; }
-keep class com.wanleung.android.Changjie.ChangjieKeyboardView { *; }

# Keep Preference fragment (referenced by name in XML)
-keep class com.wanleung.android.Changjie.SettingsFragment { *; }

# Keep serializable phrase dictionary (loaded via ObjectInputStream)
-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
