# Keep Room entities and domain enums intact if R8 minification is enabled for Play Store release builds.
-keep class com.example.data.TallyEntryEntity { *; }
-keep enum com.example.model.WoodMode { *; }
-keep enum com.example.model.RoundLogFormula { *; }
