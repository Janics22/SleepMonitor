from fastapi import FastAPI
from pydantic import BaseModel
import chromadb
import ollama
import uvicorn

MODELO_ACTUAL = "qwen2.5:latest"
HOST = "0.0.0.0"
PORT = 8000

app = FastAPI()

# Definimos qué datos esperamos recibir del móvil
class DatosSueno(BaseModel):
    total: float
    rem: float
    profundo: float
    ligero: float

# Conexión persistente a la DB (se hace una sola vez al arrancar)
client = chromadb.PersistentClient(path="../RAG/mi_base_de_datos")
collection = client.get_collection(name="consejos_salud")

@app.post("/obtener_consejo")
async def generar_respuesta(datos: DatosSueno, modelo: str = MODELO_ACTUAL):
    # 1. Lógica de decisión
    pct_profundo = (datos.profundo / datos.total) * 100
    categoria = "general"

    if pct_profundo < 15:
        categoria = "profundo"
        query_busqueda = "consejos para mejorar el sueño profundo"
    elif (datos.rem / datos.total) * 100 < 20:
        categoria = "rem"
        query_busqueda = "cómo aumentar la fase rem"
    else:
        query_busqueda = "higiene del sueño general"

    # 2. RAG: Recuperar contexto de ChromaDB
    res = collection.query(
        query_texts=[query_busqueda],
        where={"categoria": categoria},
        n_results=1
    )
    contexto = res['documents'][0][0] if res['documents'] else "Duerme en un lugar oscuro y fresco."

    # 3. Generar respuesta con Ollama
    prompt_final = f"""
    Eres un sistema automático de notificaciones de salud. Tu tarea es redactar una recomendación directa para la tarjeta de la aplicación del usuario.
    
    [INFORMACIÓN DE CONTEXTO]
    - Métricas de hoy: Total: {datos.total}h (Profundo: {datos.profundo}h, REM: {datos.rem}h)
    - Consejo médico base: "{contexto}"
    
    [REGLAS DE ORO OBLIGATORIAS]
    1. NO uses saludos, ni introducciones, ni frases de cortesía (PROHIBIDO empezar con: "¡Claro!", "Por supuesto!", "Hola", "Aquí tienes", "Basado en tus datos...").
    2. Ve DIRECTO al grano. La primera palabra de tu respuesta debe ser ya parte del consejo o de la observación empática.
    3. No repitas las horas de forma robótica (evita frases como "subir a 0.4 horas"). Usa los datos de manera humana (ej: "He notado que tu fase [X] ha sido algo corta..." o "Tus métricas indican que...").
    4. Sé extremadamente breve y directo (máximo 2 o 3 líneas).
    5. Devuelve ÚNICAMENTE el texto final del consejo. No añadas introducciones ni explicaciones fuera del mensaje.
    """
    try:
        response = ollama.generate(model=modelo, prompt=prompt_final)
        resultado_ia = response['response']
    except Exception as e:
        # Si Ollama no está abierto o el modelo no existe, entrará aquí
        print(f"⚠️ Error de conexión con Ollama: {e}")
        resultado_ia = 'No se ha conseguido conectar con Ollama.'

    return {"consejo": resultado_ia}

if __name__ == "__main__":
    uvicorn.run(app, host=HOST, port=PORT)