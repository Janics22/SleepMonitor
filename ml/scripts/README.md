# 🛠️ ML Scripts - Guía de Ejecución

Este directorio contiene los scripts necesarios para transformar los datos crudos, entrenar el modelo de detección de fases del sueño y desplegarlo en la aplicación Android.

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