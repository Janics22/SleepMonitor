# Carpeta de Modelos (SleepMonitor ML)

Este directorio contiene los modelos entrenados para la detección de fases del sueño.

## Modelo Actual: `modelo_sueño.tflite`
- **Tipo:** Red Neuronal (TensorFlow Lite)
- **Clases (Salida):** 
    - 0: Despierto (Wake)
    - 1: Sueño Ligero
    - 2: Sueño Profundo
    - 3: REM

## Especificaciones de Entrada
El modelo espera un vector de tamaño **4** con el siguiente orden:
1. `actigrafia`: Suma de diferencias de magnitud.
2. `zcm`: Cruces por cero de la señal.
3. `vm_mean`: Media de la magnitud vectorial.
4. `vm_std`: Desviación estándar de la magnitud.