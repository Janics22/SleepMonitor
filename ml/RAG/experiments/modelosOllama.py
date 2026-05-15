import os
import sys

ruta_experimentos = os.path.abspath(os.path.join(os.path.dirname(__file__)))
if ruta_experimentos not in sys.path:
    sys.path.append(ruta_experimentos)

import ollama

def listar_modelos():
    nombre_archivo = "modelos_disponibles.txt"

    try:
        # Obtenemos la respuesta del servidor
        respuesta = ollama.list()

        print("\n--- MODELOS DISPONIBLES EN OLLAMA ---")

        ruta_completa = os.path.join(ruta_experimentos, "modelos_disponibles.txt")

        # Abrimos el archivo en modo 'w' (write) para tener siempre la lista fresca
        with open(ruta_completa, "w", encoding="utf-8") as f:
            # En las versiones nuevas, 'models' es una lista de objetos
            # El nombre del modelo ahora se encuentra en el atributo '.model'
            for modelo in respuesta.models:
                print(f"-> {modelo.model}")
                # Escribimos el nombre del modelo y un salto de línea
                f.write(f"{modelo.model}\n")

        print("--------------------------------------")
        print(f"💾 Lista guardada correctamente en '{nombre_archivo}'\n")

    except Exception as e:
        print(f"Error detectado: {e}")
        print("Asegúrate de que Ollama esté abierto y ejecutándose.")

if __name__ == "__main__":
    listar_modelos()