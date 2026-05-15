# =====================================================================
# ⚙️ CONFIGURACIÓN: Modifica esto cuando cambies de modelo en Ollama
# =====================================================================
MODELO_A_PROBAR = "Llama3-8B"  # <--- Cambia esto por "Mistral-7B", "Gemma", etc.

# =====================================================================
# 🏆 GOLDEN DATASET (Casos de prueba controlados)
# =====================================================================

from datasetToTest import GOLDEN_DATASET

# =====================================================================
# 🚀 SCRIPT DE EJECUCIÓN
# =====================================================================
import os
import sys

ruta_experimentos = os.path.abspath(os.path.dirname(__file__))
if ruta_experimentos not in sys.path:
    sys.path.append(ruta_experimentos)

from sendData import sendData
import time
import datetime

def correr_banco_de_pruebas():
    # 1. Construimos la ruta exacta al archivo de modelos
    ruta_archivo_modelos = os.path.join(ruta_experimentos, "modelos_disponibles.txt")

    # Comprobamos si el archivo realmente existe antes de abrirlo
    if not os.path.exists(ruta_archivo_modelos):
        print(f"❌ Error: No se encontró el archivo de modelos en: {ruta_archivo_modelos}")
        return

    # 2. Leemos el archivo y guardamos los nombres en una lista
    with open(ruta_archivo_modelos, "r", encoding="utf-8") as f:
        # .strip() elimina espacios y saltos de línea (\n). El 'if' evita líneas vacías.
        modelos_a_probar = [linea.strip() for linea in f if linea.strip()]

    if not modelos_a_probar:
        print("⚠️ El archivo 'modelos_disponibles.txt' está vacío. No hay modelos para evaluar.")
        return

    print("=" * 60)
    print(f"🚀 BANCO DE PRUEBAS AUTOMÁTICO INICIADO")
    print(f"🤖 Modelos detectados en el archivo: {modelos_a_probar}")
    print(f"📊 Casos del Golden Dataset por modelo: {len(GOLDEN_DATASET)}")
    print("=" * 60 + "\n")

    # 3. BUCLE EXTERNO: Recorre cada modelo del archivo .txt
    for modelo_actual in modelos_a_probar:
        print("=" * 60)
        print(f"🎬 INICIANDO EVALUACIÓN PARA EL MODELO: [{modelo_actual}]")
        print("=" * 60 + "\n")

        # 4. BUCLE INTERNO: Ejecuta todos los casos de prueba para el modelo actual
        for caso in GOLDEN_DATASET:
            print(f"🔹 [{modelo_actual}] -> [CASO {caso['id']}]: {caso['descripcion']}")

            # Guardamos el tiempo de inicio para saber cuánto tarda este modelo por caso
            tiempo_inicio = time.time()

            # Enviamos los datos pasándole el modelo del bucle actual
            sendData(caso["datos"], nombre_modelo=modelo_actual)

            tiempo_fin = time.time()
            latencia = tiempo_fin - tiempo_inicio
            print(f"⏱️ Tiempo de respuesta: {latencia:.2f} segundos")
            print("-" * 60 + "\n")

            # Un pequeño respiro de 1 segundo entre peticiones para no saturar Ollama
            time.sleep(1)

        print(f"✅ Evaluación del modelo [{modelo_actual}] completada.\n")
        # Dejamos 3 segundos de pausa antes de pasar al siguiente modelo para limpiar memoria de Ollama
        time.sleep(3)

    print("🏁 ¡Banco de pruebas FINAlIZADO para todos los modelos!")
    print("📝 Revisa el archivo 'comparacion_modelos.txt' para analizar y comparar todas las respuestas generadas.")

if __name__ == "__main__":
    correr_banco_de_pruebas()