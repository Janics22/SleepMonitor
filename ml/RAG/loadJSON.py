import chromadb
import json
import uuid
import os

def cargar_datos_desde_json():
    # 1. Configuración de rutas
    ruta_db = "../RAG/mi_base_de_datos"
    ruta_json = "../RAG/consejos.json"

    # Verificar si el archivo JSON existe
    if not os.path.exists(ruta_json):
        print(f"❌ Error: No se encuentra el archivo {ruta_json}")
        return

    # 2. Conexión a ChromaDB
    client = chromadb.PersistentClient(path=ruta_db)
    collection = client.get_collection(name="consejos_salud")

    # 3. Leer el archivo JSON
    with open(ruta_json, 'r', encoding='utf-8') as f:
        datos = json.load(f)

    # 4. Preparar las listas para ChromaDB
    documentos = []
    metadatos = []
    ids = []

    for item in datos:
        documentos.append(item["texto"])
        metadatos.append({
            "categoria": item["categoria"],
            "prioridad": item["prioridad"]
        })
        ids.append(str(uuid.uuid4()))

    # 5. Insertar en la base de datos
    if documentos:
        collection.add(
            documents=documentos,
            metadatas=metadatos,
            ids=ids
        )
        print(f"🚀 ¡Éxito! Se han cargado {len(documentos)} consejos desde el JSON.")
    else:
        print("⚠️ El archivo JSON está vacío.")

if __name__ == "__main__":
    cargar_datos_desde_json()