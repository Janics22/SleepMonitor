import kagglehub
import shutil
import os

def download():
    # 1. Definir rutas (asumiendo que ejecutas desde la carpeta scripts/)
    # Subimos un nivel para llegar a la raíz y bajamos a data/raw
    target_dir = os.path.join('..', 'data', 'raw')

    # Crear la carpeta si no existe
    os.makedirs(target_dir, exist_ok=True)

    print("🔍 Buscando el dataset 'mexwell/polysomnography' en Kaggle...")

    try:
        # 2. Descarga a la caché del sistema
        temp_path = kagglehub.dataset_download("mexwell/polysomnography")
        print(f"✅ Descarga completada en caché temporal.")

        # 3. Mover los archivos a nuestra carpeta local del proyecto
        print(f"📦 Moviendo archivos a: {os.path.abspath(target_dir)}")

        files = os.listdir(temp_path)
        for f in files:
            src = os.path.join(temp_path, f)

            if f == "motion-and-heart-rate-from-a-wrist-worn-wearable-and-labeled-sleep-from-polysomnography-1.0.0": # El nombre original que viene de Kaggle
                nombre_destino = "dataset" # El nombre de la carpeta
            else:
                nombre_destino = f

            dst = os.path.join(target_dir, nombre_destino)

            # Si es una carpeta, la copiamos entera; si es archivo, solo el archivo
            if os.path.isdir(src):
                if os.path.exists(dst):
                    shutil.rmtree(dst) # Limpiar si ya existía
                shutil.copytree(src, dst)
            else:
                shutil.copy2(src, dst)

            print(f"   -> {f} listo.")

        print("\n✨ ¡Proceso terminado con éxito!")
        print(f"Ahora puedes ejecutar: python preprocess.py")

    except Exception as e:
        print(f"❌ Error durante la descarga: {e}")

    # --- SECCIÓN DE LIMPIEZA ---
    print("\n🧹 Eliminando archivos y carpetas innecesarios...")

    # 4. Lista exacta de lo que NO quieres (pon aquí los nombres reales)
    basura = [
        "heart_rate",
        "steps",
        "LICENSE.txt",
        "SHA256SUMS.txt"
    ]

    for item in basura:
        ruta_item = os.path.join(target_dir, 'dataset', item)

        if os.path.exists(ruta_item):
            try:
                if os.path.isdir(ruta_item):
                    shutil.rmtree(ruta_item) # Borra carpeta
                    print(f"   🗑️ Carpeta eliminada: {item}")
                else:
                    os.remove(ruta_item) # Borra archivo
                    print(f"   🗑️ Archivo eliminado: {item}")
            except Exception as e:
                print(f"   ⚠️ No se pudo borrar {item}: {e}")
        else:
            print(f"   ℹ️ No se encontró {item}, saltando...")

    print("\n✨ ¡Limpieza completada! La carpeta raw está lista.")

if __name__ == "__main__":
    download()