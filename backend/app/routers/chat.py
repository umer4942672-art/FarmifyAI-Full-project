from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
import httpx
from app.config import settings
router=APIRouter(prefix='/api/chat',tags=['AI Chat'])
class Chat(BaseModel): message:str; language:str='ur'; history:list[dict]=[]
SYSTEM='''You are Kisan Dost AI, an agricultural assistant for Pakistani farmers. Understand Urdu, English and Roman Urdu. Give practical, concise, safety-conscious farming advice. If recommending pesticides or fertilizer, advise users to follow local labels and agricultural extension guidance.'''
@router.post('')
async def chat(x:Chat):
    if not settings.gemini_api_key: raise HTTPException(503,'Gemini API is not configured')
    contents=x.history[-4:]+[{"role":"user","parts":[{"text":x.message}]}]
    body={"system_instruction":{"parts":[{"text":SYSTEM}]},"contents":contents,"generationConfig":{"temperature":0.3,"maxOutputTokens":1200}}
    url=f"https://generativelanguage.googleapis.com/v1beta/models/{settings.gemini_model}:generateContent?key={settings.gemini_api_key}"
    async with httpx.AsyncClient(timeout=45) as c:
        r=await c.post(url,json=body)
    if r.status_code>=400: raise HTTPException(502,'AI service unavailable')
    data=r.json()
    try: answer=data['candidates'][0]['content']['parts'][0]['text']
    except Exception: raise HTTPException(502,'Empty AI response')
    return {'success':True,'answer':answer}
