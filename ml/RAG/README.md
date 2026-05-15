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

## ⚖️ Comparativa de modelos LLM usando RAG

Para seleccionar el modelo de lenguaje más adecuado para la aplicación, se diseñó un banco de pruebas automatizado utilizando un conjunto reducido de perfiles de sueño definidos previamente. Estos perfiles representaban distintos escenarios de descanso, combinando duración total, fase REM, sueño ligero y sueño profundo.

Se evaluaron tres modelos locales ejecutados mediante Ollama. Todos recibieron las mismas consultas y el mismo contexto recuperado desde la base de conocimiento en formato `.json`. La comparación se realizó atendiendo a tres criterios principales: latencia aproximada, adherencia al contexto proporcionado y calidad de redacción de la respuesta.

### 📊 Tabla comparativa de resultados

| Modelo evaluado | Latencia aproximada | Apego al contexto JSON | Calidad de redacción y formato | Decisión final |
| :--- | :---: | :--- | :--- | :--- |
| **Qwen 2.5 7B** | 4-5s | **Excelente.** Se mantiene muy cerca de la información proporcionada. | Tono profesional, prudente y directo. Cumple bien la brevedad exigida. | 🏆 **Modelo seleccionado** |
| **Llama 3.1 8B** | 4-5s | **Bueno.** Tiende ocasionalmente a ampliar las recomendaciones con información no presente en el contexto. | Respuestas naturales y fluidas, pero a veces demasiado extensas. | Descartado por menor control del contexto |
| **Phi-3 Mini 3.8B** | 4-5s | **Regular.** Puede resultar demasiado literal o rígido al interpretar las métricas. | Estructura aceptable, pero tono más robótico y algunos problemas de formato. | Descartado por menor calidad en español |

### 🔍 Conclusiones del análisis técnico

1. **Rendimiento local:** En el entorno de pruebas utilizado, los tres modelos presentaron una latencia similar, situada aproximadamente entre 4 y 5 segundos por respuesta. Por este motivo, la velocidad no fue el factor principal de decisión.

2. **Seguimiento de instrucciones:** Llama 3.1 y Phi-3 Mini tendían en algunos casos a iniciar las respuestas con frases conversacionales o a extenderse más de lo necesario. Qwen 2.5 ofreció respuestas más directas y adecuadas para mostrarse dentro de una tarjeta de recomendación en la interfaz de la aplicación.

3. **Adherencia al contexto:** En una aplicación relacionada con el descanso y hábitos de sueño, es importante evitar recomendaciones no justificadas por los datos disponibles. Qwen 2.5 mostró la mejor capacidad para reformular el consejo recuperado mediante RAG sin añadir información externa innecesaria.

**Justificación final:** Se seleccionó **Qwen 2.5 7B** como modelo principal para el sistema RAG por ofrecer el mejor equilibrio entre seguimiento de instrucciones, adherencia al contexto, claridad en español y tono adecuado para el usuario final.


# 🛠️ RAG Scripts

Este directorio contiene los scripts necesarios para transformar los datos crudos en una base de datos que el servidor RAG pueda usar para las consultas recbidas.

## 📂 Componentes Principales del Sistema (Core RAG)

Esta sección describe los archivos que componen el núcleo de la arquitectura del sistema, encargados del almacenamiento, indexación y procesamiento de las solicitudes de los usuarios empleando la metodología RAG.

### 🛠️ Descripción de los Archivos

* **`consejos.json`**: Nuestra base de conocimiento estática. Contiene un listado estructurado de recomendaciones médicas y de higiene del sueño, clasificadas previamente por categorías (profundo, rem, ligero, general) y niveles de prioridad.
* **`crearBaseDeDatos.py`**: El script de inicialización del almacenamiento. Se encarga de configurar la instancia local de nuestra base de datos vectorial (ChromaDB) y de definir la colección donde se guardará el contexto indexado.
* **`loadJSON.py`**: El puente de migración de datos. Lee la información estructurada del archivo `consejos.json`, procesa los elementos y los inserta de manera persistente en las colecciones vectoriales de ChromaDB para permitir futuras búsquedas semánticas.
* **`RAG_server.py`**: El motor del servicio backend desarrollado con **FastAPI**. Expone el endpoint público `/obtener_consejo` que recibe las métricas de la aplicación, ejecuta la lógica matemática de diagnóstico, consulta el contexto más relevante en ChromaDB y orquesta la generación final del texto libre de alucinaciones con **Ollama**.

---

### 🔄 Flujo de Trabajo del Backend

1. Se ejecuta `crearBaseDeDatos.py` para levantar el entorno de persistencia vectorial.
2. Se corre `loadJSON.py` para poblar la base de datos con el conocimiento experto latente en `consejos.json`.
3. Se mantiene encendido `RAG_server.py` respondiendo peticiones en tiempo real y comunicándose de manera local con los LLMs administrados por Ollama.

**Selecion Modelo Ollama:** Al principio de `RAG_server.py` se puede especificar el nombre del Ollama a usar


## 📂 Estructura del Banco de Pruebas (`/experiments`)

Esta carpeta contiene el entorno automatizado para realizar pruebas de rendimiento, validación lógica y comparativas entre diferentes modelos locales de Ollama.

### 🛠️ Descripción de los Scripts (.py)

* **`modelosOllama.py`**: Se conecta con el servicio local de Ollama para consultar qué modelos de lenguaje están instalados en la máquina. Al ejecutarse, actualiza y genera automáticamente el archivo `modelos_disponibles.txt` con la lista limpia de nombres.
* **`datasetToTest.py`**: Funciona como nuestra base de datos de pruebas (*Golden Dataset*). Almacena un conjunto fijo de perfiles de sueño simulados con métricas controladas (horas totales, REM, profundo, ligero) diseñadas para forzar y validar cada una de las bifurcaciones lógicas de la API.
* **`ollamaConnection.py`**: El módulo de comunicación de bajo nivel. Se encarga de formatear la URL e inyectar el modelo seleccionado como parámetro de consulta (*Query Parameter*), enviando las métricas mediante peticiones HTTP `POST` a nuestro backend FastAPI.
* **`sendData.py`**: El nodo intermedio de ejecución. Recibe un caso de prueba del dataset, invoca a `ollamaConnection.py` para obtener la respuesta de la IA, la imprime formateada en la consola y la añade cronológicamente en el registro histórico `comparacion_modelos.txt`.
* **`ejecutarPruebas.py`**: El orquestador principal del experimento (*Matrix Testing*). Lee los modelos listados en `modelos_disponibles.txt` y, mediante bucles anidados, ejecuta todo el *Golden Dataset* con cada uno de ellos de forma 100% automatizada, calculando además la latencia (tiempo de respuesta) por cada interacción.

> 📝 **Nota sobre los archivos de texto:** Los archivos `.txt` (`modelos_disponibles.txt` y `comparacion_modelos.txt`) son generados, leídos y actualizados dinámicamente por los scripts mencionados arriba, sirviendo como registro persistente de nuestros experimentos de IA.
## Cómo ejecutar los ejecutables:
Desde la carpeta scripts:
1. Asegúrate de tener el entorno virtual activado y las librerias instaladas
2. Ejecuta el script deseado:
   ```powershell
   python ../experiments/nombre_del_experimento.py
   ```
