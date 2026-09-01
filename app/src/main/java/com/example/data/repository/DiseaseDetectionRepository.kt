package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.data.local.DiseaseScanDao
import com.example.data.local.DiseaseScanEntity
import com.example.data.model.PlantDiseaseModelEngine
import com.example.data.model.PlantDiseaseResult
import com.example.data.model.SampleDiseaseCase
import com.example.data.model.TFLitePlantClassifier
import com.example.data.remote.SupabaseDataSyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiseaseDetectionRepository(
    private val diseaseScanDao: DiseaseScanDao,
    private val context: Context? = null,
    private val supabaseSync: SupabaseDataSyncService = SupabaseDataSyncService()
) {

    private val tfliteClassifier: TFLitePlantClassifier? by lazy {
        context?.let { ctx ->
            try {
                TFLitePlantClassifier(ctx)
            } catch (e: Exception) {
                null
            }
        }
    }

    val allScans: Flow<List<PlantDiseaseResult>> = diseaseScanDao.getAllScans().map { entities ->
        entities.map { it.toModel() }
    }

    suspend fun saveScan(result: PlantDiseaseResult): Long = withContext(Dispatchers.IO) {
        val entity = DiseaseScanEntity(
            id = result.id,
            cropName = result.cropName,
            diseaseNameEn = result.diseaseNameEn,
            diseaseNameUr = result.diseaseNameUr,
            confidencePercent = result.confidencePercent,
            isHealthy = result.isHealthy,
            severityLevel = result.severityLevel,
            symptoms = result.symptomsEn,
            symptomsUr = result.symptomsUr,
            chemicalTreatment = result.chemicalTreatmentEn,
            chemicalTreatmentUr = result.chemicalTreatmentUr,
            organicPrevention = result.organicPreventionEn,
            organicPreventionUr = result.organicPreventionUr,
            advisoryNote = result.advisoryNoteEn,
            advisoryNoteUr = result.advisoryNoteUr,
            imageUriOrPath = result.imagePathOrUri,
            timestamp = result.timestamp
        )
        val id = diseaseScanDao.insertScan(entity)

        // Asynchronously sync scan diagnostic to Supabase database
        CoroutineScope(Dispatchers.IO).launch {
            try {
                supabaseSync.syncDiseaseDetection(entity.copy(id = id))
            } catch (e: Exception) {
                // Safeguard
            }
        }

        id
    }

    suspend fun deleteScan(id: Long) = withContext(Dispatchers.IO) {
        diseaseScanDao.deleteById(id)
    }

    /**
     * Main disease detection pipeline.
     * Every captured or selected plant image is classified by the bundled
     * TensorFlow Lite model first. Gemini is intentionally NOT used for
     * disease classification, so the prediction shown to the user comes from
     * the project's own trained model and also works offline.
     */
    suspend fun analyzePlantImage(bitmap: Bitmap): PlantDiseaseResult = withContext(Dispatchers.IO) {
        val classifier = tfliteClassifier
            ?: throw IllegalStateException("Plant disease model is not available")

        if (!classifier.isReady()) {
            throw IllegalStateException("Plant disease model could not be loaded")
        }

        classifier.classify(bitmap)
            ?: throw IllegalStateException("Unable to run disease detection on this image")
    }

    fun getSampleDiseaseCases(): List<SampleDiseaseCase> {
        return PlantDiseaseModelEngine.diseaseDataset.map { profile ->
            SampleDiseaseCase(
                id = profile.id,
                cropName = profile.cropName,
                cropNameUr = profile.cropNameUr,
                diseaseNameEn = profile.diseaseNameEn,
                diseaseNameUr = profile.diseaseNameUr,
                drawableResName = "crop_${profile.cropName.lowercase().substringBefore(" ").substringBefore("/")}",
                previewResult = PlantDiseaseResult(
                    cropName = profile.cropName,
                    diseaseNameEn = profile.diseaseNameEn,
                    diseaseNameUr = profile.diseaseNameUr,
                    confidencePercent = if (profile.isHealthy) 98 else 94,
                    isHealthy = profile.isHealthy,
                    severityLevel = profile.defaultSeverity,
                    symptomsEn = profile.symptomsEn,
                    symptomsUr = profile.symptomsUr,
                    chemicalTreatmentEn = profile.chemicalTreatmentEn,
                    chemicalTreatmentUr = profile.chemicalTreatmentUr,
                    organicPreventionEn = profile.organicPreventionEn,
                    organicPreventionUr = profile.organicPreventionUr,
                    advisoryNoteEn = profile.advisoryNoteEn,
                    advisoryNoteUr = profile.advisoryNoteUr
                )
            )
        }
    }

    private fun DiseaseScanEntity.toModel(): PlantDiseaseResult {
        return PlantDiseaseResult(
            id = id,
            cropName = cropName,
            diseaseNameEn = diseaseNameEn,
            diseaseNameUr = diseaseNameUr,
            confidencePercent = confidencePercent,
            isHealthy = isHealthy,
            severityLevel = severityLevel,
            symptomsEn = symptoms,
            symptomsUr = symptomsUr,
            chemicalTreatmentEn = chemicalTreatment,
            chemicalTreatmentUr = chemicalTreatmentUr,
            organicPreventionEn = organicPrevention,
            organicPreventionUr = organicPreventionUr,
            advisoryNoteEn = advisoryNote,
            advisoryNoteUr = advisoryNoteUr,
            imagePathOrUri = imageUriOrPath,
            timestamp = timestamp
        )
    }
}
