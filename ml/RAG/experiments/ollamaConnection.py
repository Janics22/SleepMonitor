import requests

def connection(datos: dict, modelo: str) -> str:
    """
    Envía las métricas de sueño al endpoint del servidor RAG
    y devuelve la respuesta de la IA.
    """
    url = f"http://localhost:8000/obtener_consejo?modelo={modelo}"

    try:
        respuesta = requests.post(url, json=datos)
        respuesta.raise_for_status()
        resultado = respuesta.json()
        return resultado.get('consejo', 'No se recibió consejo')

    except requests.exceptions.ConnectionError:
        return "❌ Error: No se pudo conectar con el servidor. ¿Está el RAG_server.py corriendo?"
    except Exception as e:
        return f"❌ Ocurrió un error inesperado: {e}"