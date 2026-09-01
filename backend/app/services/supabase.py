import httpx
from app.config import settings

class SupabaseService:
    def __init__(self): self.base = settings.supabase_url.rstrip('/')
    def headers(self, service=False):
        key = settings.supabase_service_role_key if service else settings.supabase_anon_key
        return {"apikey": key, "Authorization": f"Bearer {key}", "Content-Type": "application/json"}
    async def signup(self, payload):
        async with httpx.AsyncClient(timeout=30) as c:
            r=await c.post(f"{self.base}/auth/v1/signup",headers=self.headers(),json=payload); return r.status_code,r.json()
    async def login(self, email,password):
        async with httpx.AsyncClient(timeout=30) as c:
            r=await c.post(f"{self.base}/auth/v1/token?grant_type=password",headers=self.headers(),json={"email":email,"password":password}); return r.status_code,r.json()
    async def recover(self,email):
        async with httpx.AsyncClient(timeout=30) as c:
            r=await c.post(f"{self.base}/auth/v1/recover",headers=self.headers(),json={"email":email}); return r.status_code,r.json() if r.content else {}
    async def upsert(self, table, payload):
        h=self.headers(service=True); h["Prefer"]="resolution=merge-duplicates,return=minimal"
        async with httpx.AsyncClient(timeout=30) as c:
            r=await c.post(f"{self.base}/rest/v1/{table}",headers=h,json=payload); return r.status_code,r.text
supabase=SupabaseService()
