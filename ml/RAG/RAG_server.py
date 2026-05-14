from fastapi import FastAPI
from pydantic import BaseModel
import chromadb
import ollama
import uvicorn

MODELO_ACTUAL = "llama3.1:latest"

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
async def generar_respuesta(datos: DatosSueno):
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
    El usuario ha dormido {datos.total}h (Profundo: {datos.profundo}h, REM: {datos.rem}h).
    Usa este consejo experto: "{contexto}"
    Redacta una respuesta empática, muy breve y directa para el usuario.
    """
    try:
        response = ollama.generate(model=MODELO_ACTUAL, prompt=prompt_final)
        resultado_ia = response['response']
    except Exception as e:
        # Si Ollama no está abierto o el modelo no existe, entrará aquí
        print(f"⚠️ Error de conexión con Ollama: {e}")
        resultado_ia = 'No se ha conseguido conectar con Ollama.'

    return {"consejo": resultado_ia}

if __name__ == "__main__":
    # Arrancamos el servidor en el puerto 8000
    uvicorn.run(app, host="0.0.0.0", port=8000)