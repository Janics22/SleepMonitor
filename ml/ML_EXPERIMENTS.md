# ML Experiments

Esta entrega se ha centrado en la parte de aplicacion Android y en dejar preparada la integracion futura del modulo de IA.

## Estado actual

- No se han ejecutado nuevos experimentos de entrenamiento en esta iteracion.
- La app ya genera y guarda los datos necesarios para conectar un motor TFLite mas adelante.
- El motor actual de la app es una capa local basada en reglas para mantener el flujo funcional sin introducir IA real.

## Estructura prevista

- `ml/data/`
  Datos, features y artefactos de preprocesado.
- `ml/models/`
  Modelos exportados y versionados.
- `ml/scripts/`
  Scripts de entrenamiento y exportacion.
- `ml/experiments/` o este propio documento
  Registro de pruebas, metricas y comparativas.

## Cuando se implemente la IA

Este documento debera ampliarse con:

- Scripts utilizados.
- Configuraciones de entrenamiento.
- Dataset y pipeline de preprocesado.
- Metricas obtenidas.
- Comparacion entre modelos.
- Resultado final exportado para Android.

## Ajuste segun la organizacion del repositorio

El repositorio ya contiene los elementos estructurales pedidos para separar aplicacion y Machine Learning dentro del mismo proyecto:

- `ml/data/`
- `ml/scripts/`
- `ml/models/`
- `ml/experiments/`
- `ml/ML_EXPERIMENTS.md`

### Inventario actual del material ML integrado

- `ml/models/model.tflite`
  Modelo exportado incluido con el material recibido.
- `ml/models/labelsIO.txt`
  Etiquetas asociadas al modelo.
- `ml/reference/SleepClassifier.kt`
  Referencia de integracion Android del clasificador TFLite.
- `ml/data/preprocess.py`
- `ml/scripts/train_rf.py`
- `ml/scripts/export_tflite.py`

### Estado real de reproducibilidad

Con el contenido actual del repositorio se conserva el material ML dentro de la estructura correcta, pero no se puede reproducir de extremo a extremo un entrenamiento completo porque en esta entrega no vienen incluidos:

- dataset
- metricas de entrenamiento
- comparativa de modelos
- configuraciones de entrenamiento detalladas

Por tanto, el estado actual documentado es:

- existe un artefacto de modelo ya exportado
- existe una referencia de integracion Android
- la parte de aplicacion queda separada en `app/`
- la parte de Machine Learning queda separada en `ml/`
- la reproduccion experimental completa queda pendiente de incorporar en futuras iteraciones
