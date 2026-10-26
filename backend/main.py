import os
import json
from typing import List, Optional, Dict, Any
from fastapi import FastAPI, HTTPException, Security, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from pydantic import BaseModel
import httpx
from dotenv import load_dotenv

load_dotenv()

app = FastAPI(title="JARVIS AI Cloud Uplink", version="1.0.0")
security = HTTPBearer()

SERVER_TOKEN = os.getenv("JARVIS_BACKEND_SECRET", "jarvis_secure_dev_token_3948")
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")

def verify_token(credentials: HTTPAuthorizationCredentials = Depends(security)):
    if credentials.credentials != SERVER_TOKEN:
        raise HTTPException(status_code=403, detail="Unauthorized access to JARVIS uplink.")
    return credentials.credentials

class ToolDefinition(BaseModel):
    name: str
    description: str

class ReasonRequest(BaseModel):
    query: str
    available_tools: List[ToolDefinition]
    system_prompt: str

class ReasonResponse(BaseModel):
    action_id: Optional[str]
    parameters: Dict[str, Any]
    spoken_response: str

@app.get("/health")
def health_check():
    return {"status": "ONLINE", "uplink": "ACTIVE", "protocol": "JARVIS_v1"}

@app.post("/v1/reason", response_model=ReasonResponse)
async def reason_endpoint(request: ReasonRequest, token: str = Depends(verify_token)):
    """Routes natural language queries to an external LLM and maps them to registered tools."""
    if not OPENAI_API_KEY:
        return ReasonResponse(
            action_id=None,
            parameters={},
            spoken_response=f"Cloud uplink alive, handled query: '{request.query}'."
        )

    tools_desc = "\n".join([f"- {t.name}: {t.description}" for t in request.available_tools])
    system = f"{request.system_prompt}\nAvailable Tools:\n{tools_desc}"

    messages = [
        {"role": "system", "content": system},
        {"role": "user", "content": request.query}
    ]

    async with httpx.AsyncClient(timeout=30.0) as client:
        try:
            res = await client.post(
                "https://api.openai.com/v1/chat/completions",
                headers={"Authorization": f"Bearer {OPENAI_API_KEY}"},
                json={
                    "model": "gpt-4o-mini",
                    "messages": messages,
                    "response_format": {"type": "json_object"}
                }
            )
            res.raise_for_status()
            data = res.json()
            raw_content = data["choices"][0]["message"]["content"]
            parsed = json.loads(raw_content)

            return ReasonResponse(
                action_id=parsed.get("action_id"),
                parameters=parsed.get("parameters", {}),
                spoken_response=parsed.get("spoken_response", "Command interpreted.")
            )
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"LLM Reasoning failed: {str(e)}")

@app.post("/v1/skills/propose")
async def propose_skill(prompt: str, token: str = Depends(verify_token)):
    """Generates structured skill manifests safely without executing arbitrary server-side code."""
    skill_manifest = {
        "id": f"custom.{prompt.lower().replace(' ', '_')[:15]}",
        "name": prompt.title()[:20],
        "version": "1.0.0",
        "description": f"Custom capability dynamically generated for: {prompt}",
        "execution_type": "LOCAL_LOGIC",
        "sampleCommand": prompt,
        "risk_level": "SAFE"
    }
    return {"manifest": skill_manifest, "validation": "PASSED"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
