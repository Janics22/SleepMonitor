import chromadb
import uuid

def cargar_nuevos_consejos():
    # 1. Conexión a la base de datos
    # Asegúrate de que la ruta coincide con la de tu RAG_server.py
    client = chromadb.PersistentClient(path="./mi_base_de_datos")
    collection = client.get_or_create_collection(name="consejos_salud")

    # 2. Define aquí tus nuevos consejos
    # Solo tienes que rellenar esta lista:
    nuevos_datos = [
        {
            "texto": "Evita comidas pesadas 3 horas antes de acostarte para no interrumpir el sueño profundo.",
            "categoria": "profundo",
            "prioridad": "media"
        },
        {
            "texto": "Practicar meditación o respiración profunda ayuda a entrar antes en la fase REM.",
            "categoria": "rem",
            "prioridad": "alta"
        },
        {
            "texto": "Si no puedes dormir tras 20 minutos, sal de la cama y haz algo relajante.",
            "categoria": "general",
            "prioridad": "baja"
        }
    ]

    # 3. Procesar e insertar
    documentos = []
    metadatos = []
    ids = []

    for item in nuevos_datos:
        documentos.append(item["texto"])
        metadatos.append({
            "categoria": item["categoria"],
            "prioridad": item["prioridad"]
        })
        # Generamos un ID único basado en el texto para evitar duplicados exactos
        # o un uuid aleatorio si prefieres:
        ids.append(str(uuid.uuid4())[:8])

    if documentos:
        collection.add(
            documents=documentos,
            metadatas=metadatos,
            ids=ids
        )
        print(f"✅ ¡Éxito! Se han añadido {len(documentos)} nuevos consejos.")
    else:
        print("⚠️ No hay consejos nuevos para añadir.")

if __name__ == "__main__":
    cargar_nuevos_consejos()