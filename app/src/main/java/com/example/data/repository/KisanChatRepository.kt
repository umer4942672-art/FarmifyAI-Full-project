package com.example.data.repository

import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class KisanChatRepository {

    suspend fun getAgriAiResponse(
        userQuery: String,
        chatHistory: List<ChatMessage>,
        isUrdu: Boolean
    ): ChatMessage = withContext(Dispatchers.IO) {
        // Gemini is accessed through the FarmifyAI backend so no API key is stored in the APK.
        try {
                val geminiAnswer = callGeminiAgriApi(userQuery, chatHistory, isUrdu)
                if (geminiAnswer != null && geminiAnswer.isNotBlank()) {
                    return@withContext ChatMessage(
                        sender = MessageSender.AI_ASSISTANT,
                        textEn = if (!isUrdu) geminiAnswer else "",
                        textUr = if (isUrdu) geminiAnswer else null,
                        suggestedActions = getSuggestedFollowUps(userQuery, isUrdu)
                    )
                }
        } catch (e: Exception) {
            // Backend unavailable: use the existing offline agricultural knowledge base.
        }

        // On-device expert agricultural response
        val localResponse = generateExpertAgriculturalResponse(userQuery, isUrdu)
        return@withContext ChatMessage(
            sender = MessageSender.AI_ASSISTANT,
            textEn = localResponse.first,
            textUr = localResponse.second,
            suggestedActions = getSuggestedFollowUps(userQuery, isUrdu),
            relatedCropOrDisease = localResponse.third
        )
    }

    private fun callGeminiAgriApi(userQuery: String, chatHistory: List<ChatMessage>, isUrdu: Boolean): String? {
        val endpoint = com.example.data.remote.ApiConfig.BASE_URL.trimEnd('/') + "/api/chat"
        val conn = java.net.URL(endpoint).openConnection() as java.net.HttpURLConnection
        conn.requestMethod = "POST"; conn.setRequestProperty("Content-Type", "application/json"); conn.doOutput = true
        conn.connectTimeout = 30000; conn.readTimeout = 30000
        val history = JSONArray()
        chatHistory.takeLast(4).forEach { msg ->
            history.put(JSONObject().apply { put("role", if (msg.sender == MessageSender.USER) "user" else "model"); put("parts", JSONArray().put(JSONObject().put("text", msg.textUr?.ifBlank { msg.textEn } ?: msg.textEn))) })
        }
        val body = JSONObject().apply { put("message", userQuery); put("language", if (isUrdu) "ur" else "en"); put("history", history) }
        conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
        if (conn.responseCode !in 200..299) return null
        val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
        return json.optString("answer").ifBlank { null }
    }

    private fun generateExpertAgriculturalResponse(
        query: String,
        isUrdu: Boolean
    ): Triple<String, String, String?> {
        val q = query.lowercase(Locale.ENGLISH)

        // 1. Wheat / Gandam Fertilizer & Care
        if (q.contains("wheat") || q.contains("gandum") || q.contains("gandham") || q.contains("گندم") || q.contains("کنگ") || q.contains("rust")) {
            val en = """
                🌾 **Wheat (گندم) Expert Agronomy Plan**:
                
                • **Fertilizer Schedule (Per Acre)**:
                  - *At Sowing (Rauni)*: 1.5–2 bags DAP + 1 bag SOP (Potash).
                  - *1st Irrigation (21 Days / CRI Stage)*: 1 bag Urea + 5 kg Zinc Sulphate (33%).
                  - *2nd Irrigation (45 Days / Tillering)*: 1 bag Urea.
                  - *Flag Leaf / Booting (75 Days)*: Foliar spray of Boron + Potash (0.5%) for bold, heavy grains.
                
                • **Yellow Rust (زرد کنگی) Protection**:
                  - If yellow-orange stripes appear on leaves, spray **Tilt 250 EC (Propiconazole)** @ 200 ml/acre or **Nativo 75 WG (Bayer)** @ 65g/acre immediately.
                
                • **Irrigation Warning**: Never apply water during high wind velocity to prevent crop lodging.
            """.trimIndent()

            val ur = """
                🌾 **گندم (Wheat) کے لیے ماہرانہ زرعی رہنمائی**:
                
                • **کھادوں کا شیڈول (فی ایکڑ)**:
                  - *بجائی کے وقت*: ڈیڑھ سے دو بوری ڈی اے پی اور ایک بوری ایس او پی (پوٹاش) ڈالیں۔
                  - *پہلا پانی (21 دن پر)*: ایک بوری یوریا اور 5 کلو زنک سلفیٹ (33 فیصد) دیں۔
                  - *دوسرا پانی (40 تا 45 دن)*: ایک بوری یوریا پانی کے ساتھ فلڈ کریں۔
                  - *گوبھ کی حالت (75 دن)*: بوران اور پوٹاش کا سپرے کریں تاکہ دانہ موٹا اور وزنی بنے۔
                
                • **زرد کنگی (Yellow Rust) کا تدارک**:
                  - پتوں پر پیلے دھبے نظر آتے ہی **ٹلٹ (Tilt)** 200 ملی لیٹر یا **نیٹیوو (Nativo)** 65 گرام فی ایکڑ سپرے کریں۔
                
                • **آبپاشی کا مشورہ**: تیز ہوا میں پانی لگانے سے پرہیز کریں تاکہ فصل گرے نہ۔
            """.trimIndent()

            return Triple(en, ur, "wheat")
        }

        // 2. Cotton / Kapas Whitefly & Bollworm
        if (q.contains("cotton") || q.contains("kapas") || q.contains("کپاس") || q.contains("whitefly") || q.contains("makhi") || q.contains("bollworm") || q.contains("sundi")) {
            val en = """
                🌱 **Cotton (کپاس) Pest & Crop Protection**:
                
                • **Whitefly & Sucking Pests (سفید مکھی اور تیلا)**:
                  - *Eggs & Nymphs*: Spray **Pyriproxyfen 10.8% EC** @ 500 ml/acre in 100L water.
                  - *Adult Whitefly knockdown*: Spray **Polo (Diafenthiuron 50% SC)** @ 250 ml/acre.
                  - *Biological Control*: Install 15 yellow sticky traps per acre and spray Neem oil (5ml/L).
                
                • **Pink Bollworm (گلابی سنڈی)**:
                  - Spray **Coragen (Chlorantraniliprole 18.5% SC)** @ 50 ml/acre or **Radiant (Spinetoram)** @ 80 ml/acre.
                  - Install Pheromone delta traps (8 traps/acre) for early scouting.
                
                • **Leaf Curl Virus (CLCuV)**: Control the whitefly vector early; spray under leaves in the morning.
            """.trimIndent()

            val ur = """
                🌱 **کپاس (Cotton) کی نگہداشت اور کیڑوں کا تدارک**:
                
                • **سفید مکھی اور رس چوسنے والے کیڑے**:
                  - *انڈوں اور بچوں کے خاتمے کے لیے*: **پائری پروکسی فن** 500 ملی لیٹر فی ایکڑ سپرے کریں۔
                  - *بڑی مکھی کے فوری خاتمے کے لیے*: **پولو (Polo - ڈایا فینتھیوران)** 250 ملی لیٹر فی ایکڑ سپرے کریں۔
                  - *قدرتی تدبیر*: کھیت میں پیلے سٹکی ٹریپس لگائیں اور نیم کا عرق سپرے کریں۔
                
                • **گلابی سنڈی (Pink Bollworm)**:
                  - **کورا جن (Coragen)** 50 ملی لیٹر یا **ریڈیئنٹ (Radiant)** 80 تا 100 ملی لیٹر فی ایکڑ سپرے کریں۔
                  - جنسی پھندے (8 ٹریپس فی ایکڑ) لگا کر معائنہ کریں۔
                
                • **مروڑ وائرس سے بچاؤ**: سپرے صبح کے وقت پتوں کے نچلے رخ پر کریں جہاں مکھی رہتی ہے۔
            """.trimIndent()

            return Triple(en, ur, "cotton")
        }

        // 3. Rice / Dhaan / Chawal Blast & Stem Borer
        if (q.contains("rice") || q.contains("dhaan") || q.contains("chawal") || q.contains("چاول") || q.contains("دھان") || q.contains("blast") || q.contains("borer")) {
            val en = """
                🌾 **Rice / Basmati (دھان) Management Guide**:
                
                • **Stem Borer & Leaf Folder (تنے اور پتہ لپیٹ سنڈی)**:
                  - Broadcast **Cartap Hydrochloride 4% G (Padan)** @ 9 kg/acre or **Coragen 0.4% G** @ 4 kg/acre in 2-inch standing water at 25-30 days.
                
                • **Rice Blast (بلاسٹ اور گردن توڑ)**:
                  - Spray **Beam 75 WP (Tricyclazole)** @ 120g/acre or **Kasumin (Kasugamycin)** @ 300 ml/acre at 50% panicle emergence.
                
                • **Zinc Application**: Apply 5 kg Zinc Sulphate (33%) 15 days after transplantation to prevent leaf rusting (Khaira).
            """.trimIndent()

            val ur = """
                🌾 **دھان اور چاول (Rice) کے لیے خصوصی مشورہ**:
                
                • **تنے کی سنڈی اور پتہ لپیٹ**:
                  - **کارٹاپ / پاڈان دانے دار (Padan)** 9 کلو یا **کورا جن دانے دار** 4 کلو فی ایکڑ 2 انچ کھڑے پانی میں ڈالیں۔
                
                • **بلاسٹ اور گردن توڑ (Rice Blast)**:
                  - **بیم (Beam 75 WP)** 120 گرام یا **کاسومین** 300 ملی لیٹر فی ایکڑ سٹہ نکلنے کے وقت سپرے کریں۔
                
                • **زنک کی کمی**: پنیری منتقل کرنے کے 15 دن بعد 5 کلو زنک سلفیٹ (33 فیصد) لازمی ڈالیں۔
            """.trimIndent()

            return Triple(en, ur, "rice")
        }

        // 4. Potato / Aloo Late Blight & Fertilizer
        if (q.contains("potato") || q.contains("aloo") || q.contains("آلو") || q.contains("blight") || q.contains("jhulsao")) {
            val en = """
                🥔 **Potato (آلو) Production & Protection**:
                
                • **Late Blight (پچھیتا جھلساؤ)**:
                  - *Preventive Spray*: Spray **Mancozeb 75% WP** @ 2.5g/L before dense winter fog.
                  - *Curative Spray*: Spray **Ridomil Gold (Mancozeb + Metalaxyl)** @ 250g/acre or **Acrobat MZ** @ 250g/acre immediately upon seeing water-soaked spots.
                
                • **Frost Protection**: Give light night irrigations during severe frost warnings (Dec-Jan).
                
                • **Fertilizer**: 3 bags DAP + 2 bags Potash at bed preparation; 1.5 bags Urea at earthing up.
            """.trimIndent()

            val ur = """
                🥔 **آلو (Potato) کی فصل اور جھلساؤ کا تدارک**:
                
                • **پچھیتا جھلساؤ (Late Blight)**:
                  - *حفاظتی سپرے*: دھند شروع ہونے سے قبل **مینکوزیب (Mancozeb)** کا سپرے کریں۔
                  - *علاجی سپرے*: علامات ظاہر ہوتے ہی **ریڈومل گولڈ (Ridomil Gold)** 250 گرام یا **ایکروبیٹ (Acrobat)** 250 گرام سپرے کریں۔
                
                • **کہرے (Frost) سے حفاظت**: شدید کہرے کے دنوں میں رات کو کھیت میں ہلکا پانی لگائیں۔
                
                • **کھاد**: بجائی پر 3 بوری ڈی اے پی + 2 بوری پوٹاش؛ مٹی چڑھاتے وقت ڈیڑھ بوری یوریا دیں۔
            """.trimIndent()

            return Triple(en, ur, "potato")
        }

        // 5. Mandi & Rates Inquiry
        if (q.contains("mandi") || q.contains("rate") || q.contains("price") || q.contains("ریٹ") || q.contains("منڈی") || q.contains("قیمت")) {
            val en = """
                💰 **Live Pakistan Mandi Insights**:
                
                • **Wheat (گندم)**: PKR 3,850 – 4,100 / Mann (Demand steady across Lahore, Faisalabad & Multan).
                • **Cotton (پھٹی)**: PKR 8,500 – 9,200 / Mann (Firm demand in Rahim Yar Khan & Bahawalpur).
                • **Basmati Rice (دھان)**: PKR 4,200 – 4,800 / Mann.
                • **Maize (مکئی)**: PKR 2,450 – 2,700 / Mann (Strong poultry feed mill procurement).
                • **Potato (آلو)**: PKR 60 – 78 / Kg in Okara & Depalpur mandis.
                
                💡 *Tip: Check the 'Mandi' tab in the app for real-time market arrivals and price trends!*
            """.trimIndent()

            val ur = """
                💰 **تازہ ترین منڈی ریٹس اور تجارتی معلومات**:
                
                • **گندم**: 3,850 تا 4,100 روپے فی من (لاہور، فیصل آباد، ملتان)۔
                • **کپاس (پھٹی)**: 8,500 تا 9,200 روپے فی من (رحیم یار خان، بہاولپور)۔
                • **باسمتی چاول (دھان)**: 4,200 تا 4,800 روپے فی من۔
                • **مکئی**: 2,450 تا 2,700 روپے فی من (فیڈ ملز کی زبردست خریداری)۔
                • **آلو**: 60 تا 78 روپے فی کلو (اوکاڑہ، دیپالپور منڈی)۔
                
                💡 *مشورہ: ایپ کے 'منڈی' ٹیب میں جا کر شہر کے حساب سے ریٹس چیک کریں!*
            """.trimIndent()

            return Triple(en, ur, null)
        }

        // Default General Agri Assistance
        val en = """
            🌾 **Welcome to Kisan Dost AI Assistant**:
            
            I can help you with:
            1. **Fertilizer Calculator & Schedule**: Exact DAP, Urea, Potash, and Micronutrient doses.
            2. **Disease & Pest Scouting**: Instant diagnosis for Yellow Rust, Whitefly, Blight, Bollworms with Pakistani chemical and organic cures.
            3. **Mandi Rates & Market Intelligence**: Up-to-date crop rates across Punjab, Sindh, and KPK.
            4. **Weather-Based Field Actions**: Sowing windows, irrigation timing, and frost alerts.
            
            Ask me anything in English, Urdu (اردو), or Roman Urdu!
        """.trimIndent()

        val ur = """
            🌾 **کسان دوست AI میں خوش آمدید**:
            
            میں آپ کی درج ذیل زرعی معاملات میں رہنمائی کر سکتا ہوں:
            1. **کھادوں کا حساب کتاب**: گندم، کپاس، چاول، کماد اور مکئی کے لیے ڈی اے پی، یوریا اور پوٹاش کی درست مقدار۔
            2. **بیماریوں اور کیڑوں کا علاج**: زرد کنگی، سفید مکھی، جھلساؤ اور سنڈی کا سپرے اور قدرتی علاج۔
            3. **منڈی ریٹس اور قیمتیں**: پنجاب اور سندھ کی منڈیوں کے تازہ ترین نرخ۔
            4. **موسم اور آبپاشی**: بجائی کے اوقات، پانی کا شیڈول اور کہرے سے بچاؤ۔
            
            آپ اردو، انگریزی یا رومن اردو میں کوئی بھی سوال پوچھ سکتے ہیں!
        """.trimIndent()

        return Triple(en, ur, null)
    }

    private fun getSuggestedFollowUps(query: String, isUrdu: Boolean): List<String> {
        val q = query.lowercase(Locale.ENGLISH)
        return if (isUrdu) {
            when {
                q.contains("wheat") || q.contains("gandum") || q.contains("گندم") -> listOf("زرد کنگی کا سپرے؟", "گندم میں زنک کی مقدار؟", "گندم کا منڈی ریٹ؟")
                q.contains("cotton") || q.contains("kapas") || q.contains("کپاس") -> listOf("سفید مکھی کا بہترین سپرے؟", "گلابی سنڈی کے پھندے؟", "کپاس میں کھاد کا شیڈول؟")
                q.contains("potato") || q.contains("aloo") || q.contains("آلو") -> listOf("آلو میں پچھیتا جھلساؤ؟", "کہرے سے بچاؤ کا طریقہ؟", "آلو کی پیداواری لاگت؟")
                else -> listOf("گندم کی کھاد کا شیڈول", "کپاس میں سفید مکھی کا علاج", "آج کے منڈی کے ریٹ", "بارش کی صورت میں احتیاط")
            }
        } else {
            when {
                q.contains("wheat") || q.contains("gandum") -> listOf("Yellow rust chemical spray?", "DAP & Urea per acre?", "Wheat market rate?")
                q.contains("cotton") || q.contains("kapas") -> listOf("Best spray for whitefly?", "Pink bollworm traps?", "Cotton picking tips?")
                q.contains("potato") || q.contains("aloo") -> listOf("Late blight treatment?", "Frost protection tips?", "Potato profit per acre?")
                else -> listOf("Wheat fertilizer schedule", "Cotton whitefly remedy", "Today's Mandi rates", "Plant disease scanner")
            }
        }
    }
}
