package com.example.data.model

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * TensorFlow Lite On-Device Inference Engine for Wheat & Plant Disease Detection.
 * Loaded from assets/model/plant_disease.tflite with labels:
 * 0: Healthy
 * 1: septoria
 * 2: stripe_rust
 */
class TFLitePlantClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var isModelLoaded: Boolean = false
    private val labels: List<String> by lazy {
        try {
            context.assets.open("model/labels.txt").bufferedReader().useLines { lines ->
                lines.map { it.trim() }.filter { it.isNotEmpty() }.toList()
            }.ifEmpty { listOf("Healthy", "septoria", "stripe_rust") }
        } catch (e: Exception) {
            listOf("Healthy", "septoria", "stripe_rust")
        }
    }
    private val inputImageSize = 224 // standard vision classifier input (e.g. MobileNet / EfficientNet)

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            val possiblePaths = listOf(
                "model/plant_disease.tflite",
                "plant_disease.tflite"
            )
            for (path in possiblePaths) {
                try {
                    val assetFileDescriptor = context.assets.openFd(path)
                    val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
                    val fileChannel = fileInputStream.channel
                    val startOffset = assetFileDescriptor.startOffset
                    val declaredLength = assetFileDescriptor.declaredLength
                    val buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

                    val options = Interpreter.Options().apply {
                        setNumThreads(4)
                    }
                    interpreter = Interpreter(buffer, options)
                    isModelLoaded = true
                    Log.d("TFLitePlantClassifier", "Successfully loaded TFLite model from asset: $path")
                    break
                } catch (e: Exception) {
                    // Try next path
                }
            }
        } catch (e: Exception) {
            Log.w("TFLitePlantClassifier", "Could not initialize TFLite model: ${e.message}")
            isModelLoaded = false
        }
    }

    fun isReady(): Boolean = isModelLoaded && interpreter != null

    /**
     * Runs inference on the given plant leaf bitmap
     */
    fun classify(bitmap: Bitmap): PlantDiseaseResult? {
        val tflite = interpreter
        if (tflite == null) return null

        try {
            // Resize bitmap to model's input dimension
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputImageSize, inputImageSize, true)

            // Prepare float32 ByteBuffer (1 * 224 * 224 * 3 * 4 bytes)
            val byteBuffer = ByteBuffer.allocateDirect(1 * inputImageSize * inputImageSize * 3 * 4)
            byteBuffer.order(ByteOrder.nativeOrder())

            val intValues = IntArray(inputImageSize * inputImageSize)
            resizedBitmap.getPixels(intValues, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)

            var pixel = 0
            for (i in 0 until inputImageSize) {
                for (j in 0 until inputImageSize) {
                    val `val` = intValues[pixel++]
                    // Normalize RGB values to [0, 1]
                    val r = ((`val` shr 16) and 0xFF) / 255.0f
                    val g = ((`val` shr 8) and 0xFF) / 255.0f
                    val b = (`val` and 0xFF) / 255.0f

                    byteBuffer.putFloat(r)
                    byteBuffer.putFloat(g)
                    byteBuffer.putFloat(b)
                }
            }

            if (resizedBitmap != bitmap) {
                resizedBitmap.recycle()
            }

            // Output array: [1, 3] for 3 classes
            val output = Array(1) { FloatArray(labels.size) }
            tflite.run(byteBuffer, output)

            val probabilities = output[0]
            var maxIndex = 0
            var maxProb = probabilities[0]
            for (k in 1 until probabilities.size) {
                if (probabilities[k] > maxProb) {
                    maxProb = probabilities[k]
                    maxIndex = k
                }
            }

            val detectedLabel = labels.getOrElse(maxIndex) { "Healthy" }
            // Keep the model's real confidence instead of artificially forcing it
            // into a high 75-99% range.
            val confidence = (maxProb * 100f).toInt().coerceIn(0, 100)

            return mapClassToResult(detectedLabel, confidence)
        } catch (e: Exception) {
            Log.e("TFLitePlantClassifier", "Inference error: ${e.message}", e)
            return null
        }
    }

    /**
     * Maps model labels (Healthy, septoria, stripe_rust) to detailed agronomic diagnosis and treatment.
     */
    fun mapClassToResult(label: String, confidence: Int): PlantDiseaseResult {
        return when (label.lowercase()) {
            "septoria" -> {
                PlantDiseaseResult(
                    cropName = "Wheat / گندم",
                    diseaseNameEn = "Septoria Leaf Blotch (Septoria tritici)",
                    diseaseNameUr = "سیپٹوریا پتوں کا جھلساؤ (سیپٹوریا بلاچ)",
                    confidencePercent = confidence,
                    isHealthy = false,
                    severityLevel = if (confidence > 88) "High" else "Moderate",
                    symptomsEn = "Irregular oval to rectangular light brown necrotic lesions bounded by leaf veins with characteristic tiny black fungal specks (pycnidia) inside spots.",
                    symptomsUr = "پتوں پر بیضوی اور مستطیل بھورے سوکھے دھبے جن کے اندر باریک سیاہ تل نما دانے بنتے ہیں اور نچلے پتے سوکھ جاتے ہیں۔",
                    chemicalTreatmentEn = "Spray Tilt (Propiconazole 25% EC) @ 200ml/acre or Nativo (Tebuconazole + Trifloxystrobin) @ 65g/acre or Amistar Top @ 200ml/acre in 100L water.",
                    chemicalTreatmentUr = "ٹلٹ (سنجینٹا) 200 ملی لیٹر فی ایکڑ یا نیٹیوو 65 گرام یا ایمسٹار ٹاپ 200 ملی لیٹر 100 لیٹر پانی میں ملا کر سپرے کریں۔",
                    organicPreventionEn = "Ensure 2-year crop rotation with non-cereal crops, destroy infected straw debris, avoid excessive dense planting.",
                    organicPreventionUr = "فصلوں کا ہیر پھیر کریں، پرانی گندم کی باقیات زمین میں دبا دیں اور بیج کو فنجی سائیڈ لگا کر کاشت کریں۔",
                    advisoryNoteEn = "Spreads upward via rain splashes. Spray early before infection reaches the vital flag leaf (which builds 70% of grain yield).",
                    advisoryNoteUr = "بارش کے قطروں سے بیماری اوپر چڑھتی ہے، جھنڈا پتا نکلنے سے پہلے سپرے مکمل کریں تاکہ پیداوار محفوظ رہے۔"
                )
            }
            "stripe_rust" -> {
                PlantDiseaseResult(
                    cropName = "Wheat / گندم",
                    diseaseNameEn = "Stripe Rust / Yellow Rust (Puccinia striiformis)",
                    diseaseNameUr = "گندم کی زرد کنگی (سٹرائپ رسٹ)",
                    confidencePercent = confidence,
                    isHealthy = false,
                    severityLevel = "Critical",
                    symptomsEn = "Bright yellow to orange powdery pustules aligned in distinctive linear stripes parallel to leaf veins. Yellow fungal spores rub off on fingertips.",
                    symptomsUr = "پتوں کی رگوں کے ساتھ متوازی قطاروں میں پیلی اور نارنجی رنگ کی لکیریں بنتی ہیں جن پر پاؤڈر لگا ہوتا ہے جو انگلی پر لگ جاتا ہے۔",
                    chemicalTreatmentEn = "Immediate foliar spray: Nativo (Bayer) @ 65g/acre or Tilt 250 EC (Propiconazole) @ 200ml/acre or Folicur (Tebuconazole) @ 200ml/acre.",
                    chemicalTreatmentUr = "فوری سپرے: نیٹیوو (بائر) 65 گرام فی ایکڑ یا ٹلٹ 200 ملی لیٹر یا فولیکر 200 ملی لیٹر 100 لیٹر پانی میں سپرے کریں۔",
                    organicPreventionEn = "Sow certified resistant varieties (Akbar-19, Dilkash-20, Subhani-21, Urooj-22). Apply balanced Potash (SOP/MOP) to boost leaf immunity.",
                    organicPreventionUr = "ہمیشہ منظور شدہ اقسام (اکبر-19، دلکش-20، عروج-22) کاشت کریں اور پوٹاش کھاد کا استعمال کریں۔",
                    advisoryNoteEn = "Airborne fungal spores spread rapidly in cool moist weather (10-20°C). Can cause 50%+ grain shriveling if left untreated.",
                    advisoryNoteUr = "ٹھنڈی ہواؤں میں یہ بیماری بہت تیزی سے پھیلتی ہے، 50 فیصد سے زائد پیداوار کم ہو سکتی ہے، بلا تاخیر سپرے کریں۔"
                )
            }
            else -> { // Healthy
                PlantDiseaseResult(
                    cropName = "Wheat / General Crop",
                    diseaseNameEn = "Healthy & Disease-Free Wheat Leaf",
                    diseaseNameUr = "صحت مند پودا (بیماری سے پاک)",
                    confidencePercent = confidence,
                    isHealthy = true,
                    severityLevel = "None",
                    symptomsEn = "Vibrant deep green foliage, crisp intact leaf margins, active photosynthesis, no rust stripes, spots, or fungal lesions detected.",
                    symptomsUr = "پتے گہرے سبز، تروتازہ اور زرد کنگی یا دھبوں سے بالکل پاک ہیں۔ پودے کی صحت اور بڑھوتری بہترین ہے۔",
                    chemicalTreatmentEn = "No chemical pesticide or fungicide needed. Maintain regular NPK fertigation and watering schedule.",
                    chemicalTreatmentUr = "کسی زہر یا فنجی سائیڈ سپرے کی ضرورت نہیں ہے۔ شیڈول کے مطابق کھاد اور پانی دیں۔",
                    organicPreventionEn = "Apply micronutrients (Zinc, Boron, Potassium) and bio-stimulant foliar spray to maximize grain weight.",
                    organicPreventionUr = "پودے کی مضبوطی اور دانے کا وزن بڑھانے کے لیے زنک، بوران اور پوٹاش کا استعمال جاری رکھیں۔",
                    advisoryNoteEn = "Crop is in optimal health. Re-inspect after high humidity or heavy rainfall.",
                    advisoryNoteUr = "فصل بہترین حالت میں ہے۔ بارش یا زیادہ نمی کے بعد دوبارہ معائنہ کریں۔"
                )
            }
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
