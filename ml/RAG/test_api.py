import requests

def probar_consejo():
    # 1. Configuración de la URL y los datos
    url = "http://localhost:8000/obtener_consejo"

    datos_prueba = {
        "total": 6.5,
        "rem": 0.8,
        "profundo": 1.2,
        "ligero": 4.5
    }

    try:
        # 2. Enviar la petición POST
        # Usamos 'json=' para que requests configure automáticamente el Content-Type
        print(f"🚀 Enviando datos al servidor: {datos_prueba}...")
        respuesta = requests.post(url, json=datos_prueba)

        # 3. Comprobar si todo ha ido bien (Status 200)
        respuesta.raise_for_status()

        # 4. Mostrar el resultado
        resultado = respuesta.json()
        print("\n--- RESPUESTA DEL RAG ---")
        print(f"IA: {resultado.get('consejo', 'No se recibió consejo')}")
        print("--------------------------\n")

    except requests.exceptions.ConnectionError:
        print("❌ Error: No se pudo conectar con el servidor. ¿Está el RAG_server.py corriendo?")
    except Exception as e:
        print(f"❌ Ocurrió un error inesperado: {e}")

if __name__ == "__main__":
    probar_consejo()