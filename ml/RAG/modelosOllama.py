import ollama

def listar_modelos():
    try:
        # Obtenemos la respuesta del servidor
        respuesta = ollama.list()

        print("\n--- MODELOS DISPONIBLES EN OLLAMA ---")

        # En las versiones nuevas, 'models' es una lista de objetos
        # El nombre del modelo ahora se encuentra en el atributo '.model'
        for modelo in respuesta.models:
            print(f"-> {modelo.model}")

        print("--------------------------------------\n")

    except Exception as e:
        print(f"Error detectado: {e}")
        print("Asegúrate de que Ollama esté abierto y ejecutándose.")

if __name__ == "__main__":
    listar_modelos()