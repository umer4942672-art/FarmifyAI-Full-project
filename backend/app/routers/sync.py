from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Any
from app.services.supabase import supabase
router=APIRouter(prefix='/api',tags=['Sync'])
class Payload(BaseModel): data: dict[str,Any]
@router.post('/sync/profile')
async def profile(x:Payload):
    code,text=await supabase.upsert('profiles',x.data)
    if code>=400: raise HTTPException(code,text)
    return {'success':True}
@router.post('/sync/khata')
async def khata(x:Payload):
    code,text=await supabase.upsert('khata_entries',x.data)
    if code>=400: raise HTTPException(code,text)
    return {'success':True}
@router.post('/sync/disease')
async def disease(x:Payload):
    code,text=await supabase.upsert('disease_detections',x.data)
    if code>=400: raise HTTPException(code,text)
    return {'success':True}
