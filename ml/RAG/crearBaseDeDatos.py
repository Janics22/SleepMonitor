import chromadb

# 1. Crear el cliente de la base de datos (se guardará en una carpeta local)
client = chromadb.PersistentClient(path="../RAG/mi_base_de_datos")

# 2. Crear una "Colección" (como una tabla)
collection = client.get_or_create_collection(name="consejos_salud")

# 3. Añadir tus consejos
# Nota: ChromaDB por defecto usa un modelo ligero para convertir texto a vectores
collection.add(
    documents=[
        "La temperatura ideal para potenciar el sueño profundo es de 18°C.",
        "Evita las pantallas 1 hora antes de dormir para no bloquear la fase REM.",
        "La consistencia en el horario es clave para regular el ciclo circadiano."
    ],
    metadatas=[
        {"categoria": "profundo", "prioridad": "alta"},
        {"categoria": "rem", "prioridad": "alta"},
        {"categoria": "general", "prioridad": "media"}
    ],
    ids=["p1", "r1", "g1"]
)

print("¡Base de datos lista!")