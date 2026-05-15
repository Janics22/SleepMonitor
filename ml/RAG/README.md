# RAG

Esta carpeta queda reservada para almacenar la base de datos RAG y los ejecutables necesarios para crear/usar la base de datos y conectarse a Ollama.

El resumen narrativo de la base de datos debe mantenerse en [RAG.md](../ML_EXPERIMENTS.md).
# RAG

En esta carpeta guardamos scripts para generar la base de datos RAG usada en la app.

## Contenido:
- **consejos.JSON:** Archivo fuente que contiene el conocimiento
- **crear base de datos:** Script encargado de inicializar el cliente de ChromaDB y crear la colección consejos_salud. Se ejecuta una sola vez para preparar la infraestructura local.
- **loadJson:** Automatiza la lectura de consejos.json, carga los documentos con sus metadatos en la base de datos vectorial.
- **modelosOllama:** Utilidad para listar los modelos instalados localmente en el servidor de Ollama. Permite verificar nombres exactos.
- **Rag_server:** Es un servidor FastAPI que actúa como puente. Recibe las métricas de sueño desde la app de Kotlin recupera el contexto relevante de la base de datos y genera una respuesta usando la API de Ollama
- **test_api:** Consulta simple para comprovar conexiones el funcionamiento

> Nota: Rag_server.py al principio tiene campos donde se definem: el nombre del modelo IA a usar de Ollama y el HOST y PUERTO
## Cómo ejecutar:
Desde la carpeta scripts:
1. Asegúrate de tener el entorno virtual activado y las librerias instaladas
2. Ejecuta el script deseado:
   ```powershell
    python ..\RAG\XXXXX 
   ```