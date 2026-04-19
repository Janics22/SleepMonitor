import shutil
import os

# Rutas (ajústalas a tu estructura real)
origen = '../models/model.tflite'
destino = '../../app/src/main/assets/model.tflite'

# Crear la carpeta assets si no existe
os.makedirs(os.path.dirname(destino), exist_ok=True)

# Mover las etiquetas
shutil.copy2('../models/labelsIO.txt', '../../app/src/main/assets/labelsIO.txt')

try:
    shutil.copy2(origen, destino)
    print(f"✅ Modelo copiado con éxito a: {destino}")
except FileNotFoundError:
    print("❌ Error: No se encontró el archivo .tflite.")