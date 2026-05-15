import os
import sys
from datetime import datetime

carpeta_actual = os.path.dirname(os.path.abspath(__file__))
if carpeta_actual not in sys.path:
    sys.path.append(carpeta_actual)
from ollamaConnection import connection

def sendData(datos_prueba: dict, nombre_modelo: str = "Modelo_No_Especificado"):
    """
    Recibe datos de prueba, invoca la comunicación, los muestra en consola
    y los añade de forma ordenada a un archivo .txt para comparar modelos.
    """
    print(f"🚀 Enviando datos al servidor usando [{nombre_modelo}]: {datos_prueba}...")

    # Llamamos a tu función de comunicación (usa 'connection' o 'ollamaConnection' según tu import)
    consejo_ia = connection(datos_prueba, nombre_modelo)

    # 1. Mostrar el resultado formateado en consola
    print("\n--- RESPUESTA DEL RAG ---")
    print(f"🤖 Modelo: {nombre_modelo}")
    print(f"IA: {consejo_ia}")
    print("--------------------------\n")

    # 2. GUARDAR EN EL ARCHIVO .TXT
    nombre_archivo = "comparacion_modelos.txt"
    ruta_completa_archivo = os.path.join(carpeta_actual, nombre_archivo)
    fecha_hora = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    try:
        # Abrimos en modo 'a' (append) para añadir al final del archivo sin sobrescribir
        with open(ruta_completa_archivo, "a", encoding="utf-8") as f:
            f.write("======================================================================\n")
            f.write(f"📅 FECHA Y HORA:  {fecha_hora}\n")
            f.write(f"🤖 MODELO EVALUADO: {nombre_modelo}\n")
            f.write(f"📥 DATOS ENTRADA: Total: {datos_prueba.get('total')}h | Profundo: {datos_prueba.get('profundo')}h | REM: {datos_prueba.get('rem')}h | Ligero: {datos_prueba.get('ligero')}h\n")
            f.write("----------------------------------------------------------------------\n")
            f.write(f"💡 RESPUESTA GENERADA:\n{consejo_ia}\n")
            f.write("======================================================================\n\n")

        print(f"💾 Experimento guardado correctamente en '{nombre_archivo}'\n")

    except Exception as e:
        print(f"⚠️ No se pudo guardar el archivo de texto: {e}")


# Bloque de ejecución principal
if __name__ == "__main__":
    # Definimos los datos de prueba aquí
    datos_ejemplo = {
        "total": 6.5,
        "rem": 0.8,
        "profundo": 1.2,
        "ligero": 4.5
    }

    # Ejecutamos la prueba llamando a la segunda función
    sendData(datos_ejemplo)