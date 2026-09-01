package com.example.data.model

data class PlantDiseaseResult(
    val id: Long = 0,
    val cropName: String,
    val diseaseNameEn: String,
    val diseaseNameUr: String,
    val confidencePercent: Int,
    val isHealthy: Boolean,
    val severityLevel: String, // "Low", "Moderate", "High", "Critical"
    val symptomsEn: String,
    val symptomsUr: String,
    val chemicalTreatmentEn: String,
    val chemicalTreatmentUr: String,
    val organicPreventionEn: String,
    val organicPreventionUr: String,
    val advisoryNoteEn: String,
    val advisoryNoteUr: String,
    val imagePathOrUri: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class SampleDiseaseCase(
    val id: String,
    val cropName: String,
    val cropNameUr: String,
    val diseaseNameEn: String,
    val diseaseNameUr: String,
    val drawableResName: String,
    val previewResult: PlantDiseaseResult
)
