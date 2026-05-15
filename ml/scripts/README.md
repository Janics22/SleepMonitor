# 🛠️ RAG

## 📄 Descripción del Problema

El objetivo principal de esta funcionalidad era ir más allá de la simple recolección de métricas y ofrecer valor real al usuario mediante **consejos de sueño personalizados**.

Inicialmente, contábamos con una Inteligencia Artificial capaz de analizar los datos del usuario para generar un resumen de sus sueños y detallar las horas dormidas en cada fase del ciclo de sueño. Sin embargo, nos enfrentamos a un doble desafío técnico y lógico:

* **Traducción de métricas a recomendaciones:** Existía una brecha entre tener un informe técnico (horas y fases) y entregar un consejo práctico y estructurado que el usuario pudiera aplicar para mejorar su descanso.
* **Complejidad de entrenamiento:** La primera aproximación fue considerar el entrenamiento (o *fine-tuning*) de un modelo de IA secundario dedicado exclusivamente a redactar estos consejos. Esta ruta se descartó por su alta complejidad y la gran dificultad de encontrar un *dataset* acceptable.

---

## 🛠️ Abordaje y Solución (Implementación de RAG)

Para superar el obstáculo del entrenamiento y la limitación de los datos, decidimos pivotar hacia una arquitectura basada en **RAG (Retrieval-Augmented Generation)** y el uso de **Ollama**.

En lugar de intentar que la IA "aprendiera" todos los consejos médicos desde cero mediante entrenamiento, le proporcionamos la información exacta en el momento adecuado. El flujo de la solución es el siguiente:

**Decisión de Arquitectura: Estructura JSON vs. Chunking**
A diferencia de las implementaciones RAG tradicionales que dividen documentos extensos en pequeños fragmentos (*chunks*) y los almacenan en bases de datos vectoriales, nosotros optamos por un enfoque más pragmático. Clasificamos y estructuramos nuestros consejos de sueño directamente en un archivo `.json`. Dado que la información ya estaba pre-categorizada y era muy específica, implementar un sistema de *chunking* solo habría añadido una capa de complejidad técnica y procesamiento innecesario.

1. **Recuperación de Contexto Directa (Retrieval):** Implementamos un sistema que lee y filtra directamente desde nuestro archivo `.json` para encontrar los consejos de sueño más relevantes, basándose en las métricas (horas y fases) detectadas en el usuario.
2. **Inyección en el Prompt (Augmented):** Integramos esos consejos precisos como contexto dentro de un *prompt* estructurado.
3. **Generación con Ollama (Generation):** Enviamos este *prompt* enriquecido a Ollama. El modelo de lenguaje utiliza su capacidad natural de redacción y comprensión para fusionar las métricas del usuario con el contexto inyectado.

**Resultado:** Logramos generar consejos de sueño altamente precisos



# 🛠️ RAG Scripts - Guía de Ejecución

Este directorio contiene los scripts necesarios para transformar los datos crudos en una base de datos que el servidor RAG pueda usar para las consultas recbidas.

## ⚙️ Configuración del Entorno

Es fundamental crear un entorno virtual para aislar las dependencias y asegurar que el código funcione correctamente.

1. **Crear el entorno virtual:**
   ```powershell
   python -m venv env
   ```
2. **activar entorno virtual:**
    ```powershell
    .\env\Scripts\activate
    ```
3. **Instalar dependencias:**
   ```powershell
   pip install -r requirements.txt
   ```
   
# 🔄 Scripts
1. **Descargar el dataset:**
    ```powershell
   python downloadDataset.py
   ```
2. **Procesar el dataset:**
   pasa a .csv los datos del dataset
    ```powershell
   python preprocess.py
   ```
2. **Entrenar a la IA con el dataset:**
   Usando los datos de data/processed <br>Entrena el modelo y genera model.tflite en la carpeta model/
    ```powershell
   python train.py
   ```
3. **Mandar a la app el .tflite:**
   Manda a la carpeta de la app la IA<br>En caso de haber una, la sustituye
    ```powershell
   python export_tflite.py
    ```