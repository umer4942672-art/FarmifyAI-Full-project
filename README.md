# FarmifyAI

FarmifyAI is an Android application designed to support farmers with crop guidance, disease detection, farm records, weather information and an agricultural AI assistant.

## Project architecture

- **Android app:** Kotlin + Jetpack Compose
- **Backend API:** Python + FastAPI
- **Cloud database & authentication:** Supabase
- **Offline database:** Room
- **On-device disease model:** TensorFlow Lite
- **AI assistant:** Gemini, accessed securely through the backend
- **Backend deployment:** vercel

The Android application does not contain Supabase service-role credentials or the Gemini API key. Sensitive cloud operations are routed through the FastAPI backend.

## Repository structure

```text
app/        Android application
backend/    FastAPI server
assets/     ML model files and labels
supabase/   Database schema
```

## Running the backend

See `backend/README.md`. Configure `backend/.env` from `.env.example`, then run FastAPI locally or deploy the `backend` directory to Render.

## Android configuration

After deployment, update:

`app/src/main/java/com/example/data/remote/ApiConfig.kt`

with your Vercel backend URL.

## Development notes

The app keeps Room for offline functionality. TensorFlow Lite remains on the device for fast disease detection, while cloud synchronization, authentication and Gemini requests are handled by the backend.

## Disease Detection Pipeline
Plant disease classification is performed on-device using the project's bundled TensorFlow Lite model (`plant_disease.tflite`). Every image captured from the camera or selected from storage is passed to the model after preprocessing. The predicted class and confidence score are returned directly from the model. Gemini is not used to classify plant diseases; it can remain available separately for general agricultural guidance or chat.
