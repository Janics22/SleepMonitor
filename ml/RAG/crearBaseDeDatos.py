import chromadb

def inicializar_db():
    # 1. Definimos la ruta (asegúrate de que sea la misma en ambos scripts)
    path_db = "../RAG/mi_base_de_datos"

    client = chromadb.PersistentClient(path=path_db)

    # 2. Creamos la colección.
    # get_or_create_collection evita errores si el script se corre dos veces.
    collection = client.get_or_create_collection(name="consejos_salud")

    print(f"✅ Base de datos inicializada en: {path_db}")
    print(f"✅ Colección '{collection.name}' lista para recibir datos.")

if __name__ == "__main__":
    inicializar_db()