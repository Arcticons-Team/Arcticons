# ProGuard rules for Arcticons

# Room & WorkManager (Fixes crash on startup with AGP 9.0+ optimization)
-keep class * extends androidx.room.RoomDatabase
-keep class androidx.work.impl.WorkDatabase_Impl {
    public <init>(android.content.Context);
    public <init>();
}

# Keep Room generated classes
-keep class androidx.room.RoomDatabase {
    protected <init>();
}
-keep class * extends androidx.room.RoomDatabase
-keep class * implements androidx.room.RoomOpenHelper

# CandyBar Library
-keep class candybar.lib.** { *; }

# Keep all drawable resources for reflection (Essential for icon packs)
-keep class com.donnnno.arcticons.R$drawable { *; }

# General AGP 9.0 compatibility: keep constructors for classes that might be instantiated via reflection
-keepclassmembers class * {
    @androidx.room.Database *;
}
