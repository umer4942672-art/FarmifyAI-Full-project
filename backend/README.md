# FarmifyAI Backend

This is the server-side API for FarmifyAI. It keeps Supabase service credentials and the Gemini API key outside the Android APK.

## Run locally

```bash
cd backend
python -m venv .venv
# activate the environment
pip install -r requirements.txt
cp .env.example .env
uvicorn app.main:app --reload
```

Open `/docs` for Swagger API documentation.

## Deploy on Render

1. Push the project to GitHub.
2. Create a new Web Service on Render.
3. Set the root directory to `backend`.
4. Add all values from `.env.example` as Render environment variables.
5. Start command: `uvicorn app.main:app --host 0.0.0.0 --port $PORT`.
6. Copy the deployed URL into the Android app's `ApiConfig.kt`.

Never commit `.env` or service-role keys.
