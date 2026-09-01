from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers import auth,sync,chat
app=FastAPI(title='FarmifyAI Backend',version='1.0.0')
app.add_middleware(CORSMiddleware,allow_origins=['*'],allow_credentials=False,allow_methods=['*'],allow_headers=['*'])
app.include_router(auth.router); app.include_router(sync.router); app.include_router(chat.router)
@app.get('/')
def root(): return {'service':'FarmifyAI Backend','status':'running'}
@app.get('/health')
def health(): return {'status':'healthy'}
