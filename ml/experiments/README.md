# Experiments

Esta carpeta queda reservada para almacenar salidas de experimentos, comparativas, tablas de metricas y cualquier artefacto auxiliar generado durante el trabajo de Machine Learning.

El resumen narrativo de esos experimentos debe mantenerse en [ML_EXPERIMENTS.md](../ML_EXPERIMENTS.md).
# 🧪 Experiments

En esta carpeta guardamos scripts para visualizar la precision de los modelos entrenados.

## Contenido:
- **Análisis de señales:** Visualización de la relación entre sensores y fases de sueño.
- **Matriz de confusion:** Comparativa entre resultados esperados y obtenidos segun el dataset de validacion y la IA (.h5) dentro de ml/model.

> Nota: Los archivos aquí contenidos no son necesarios para el funcionamiento de la App, son puramente informativos y de investigación.
## Cómo ejecutar los experimentos:
Desde la carpeta scripts:
1. Asegúrate de tener el entorno virtual activado y las librerias instaladas
2. Ejecuta el script deseado:
   ```powershell
   python ../experiments/nombre_del_experimento.py
   ```