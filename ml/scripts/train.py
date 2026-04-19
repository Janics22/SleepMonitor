import pandas as pd
import numpy as np
import tensorflow as tf
import os
from imblearn.over_sampling import SMOTE
from sklearn.preprocessing import StandardScaler

# --- 1. CONFIGURACIÓN DE RUTAS ---
RUTA_PROCESADOS = '../data/processed/'
RUTA_MODELO = '../models/'
os.makedirs(RUTA_MODELO, exist_ok=True)

print("Cargando y equilibrando datos...")

# --- 2. CARGA Y EQUILIBRADO (SMOTE) ---
# Cargamos el train original para equilibrarlo
df_train_raw = pd.read_csv(os.path.join(RUTA_PROCESADOS, 'train.csv'))
X = df_train_raw.drop('target', axis=1)
y = df_train_raw['target']

# Aplicamos SMOTE para que todas las fases tengan el mismo peso
smote = SMOTE(sampling_strategy='not majority', random_state=42)
X_res, y_res = smote.fit_resample(X, y)

print(f"Distribución tras SMOTE: \n{y_res.value_counts()}")

# --- 3. PREPARACIÓN DE DATASETS (TENSORFLOW) ---
# Como ya tenemos los datos en memoria por el SMOTE, es más fácil pasarlos a Dataset así:
train_ds = tf.data.Dataset.from_tensor_slices((X_res.values, y_res.values))
train_ds = train_ds.shuffle(len(X_res)).batch(32)

# Para Val y Test, los cargamos directamente de los CSV
def cargar_csv_a_ds(nombre_archivo):
    df = pd.read_csv(os.path.join(RUTA_PROCESADOS, nombre_archivo))
    X_df = df.drop('target', axis=1).values
    y_df = df['target'].values
    return tf.data.Dataset.from_tensor_slices((X_df, y_df)).batch(32)

val_ds = cargar_csv_a_ds('val.csv')
test_ds = cargar_csv_a_ds('test.csv')

# --- 4. NORMALIZACIÓN ---
# Es vital para que los sensores (0.01 vs 500) no vuelvan loca a la IA
normalizer = tf.keras.layers.Normalization(axis=-1)
# "Entrenamos" al normalizador solo con los datos de entrada
normalizer.adapt(np.array(X_res))

# --- 5. MODELO ---
model = tf.keras.Sequential([
    tf.keras.layers.Input(shape=(4,)), # actigrafia, zcm, vm_mean, vm_std
    normalizer,
    tf.keras.layers.Dense(64, activation='relu'),
    tf.keras.layers.Dropout(0.2), # Evita que la IA "memorice" (overfitting)
    tf.keras.layers.Dense(32, activation='relu'),
    tf.keras.layers.Dense(16, activation='relu'),
    tf.keras.layers.Dense(4, activation='softmax') # 4 fases de sueño
])

model.compile(
    optimizer="adam",
    loss="sparse_categorical_crossentropy",
    metrics=['accuracy']
)

# --- 6. ENTRENAMIENTO ---
print("Iniciando entrenamiento...")
model.fit(
    train_ds,
    validation_data=val_ds,
    epochs=25
)

# --- 7. GUARDADO (Crucial para Android) ---
# Guardamos el modelo normal para pruebas
model.save(os.path.join(RUTA_MODELO, 'model_sleep.h5'))

# Convertimos a TFLite (El formato que entiende el móvil)
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

with open(os.path.join(RUTA_MODELO, 'model.tflite'), 'wb') as f:
    f.write(tflite_model)

print(f"✅ ¡Hecho! Modelo guardado en {RUTA_MODELO}")

# Extraer la media y la varianza calculadas
medias = normalizer.mean.numpy()
varianzas = normalizer.variance.numpy()
desviaciones = np.sqrt(varianzas) # La desviación es la raíz de la varianza

# Crear el archivo de etiquetas automáticamente
labels = ['Despierto', 'Ligero', 'Profundo', 'REM']
with open('../models/labelsIO.txt', 'w') as f:
    for label in labels:
        f.write(label + '\n')

print("\n--- DATOS ---")
print(f"Orden de sensores: ['actigrafia', 'zcm', 'vm_mean', 'vm_std']")
print(f"Medias (mean): {medias}")
print(f"Desviaciones (std): {desviaciones}")

print("----------------------------")
print(df_train_raw.describe()) # Esto te dirá el Máximo y el Mínimo de cada columna

print("\n--- Recuento de Fases antes del SMOTE ---")
print(df_train_raw['target'].value_counts()) # Esto nos dirá si hay demasiados 'Ligeros'
print("----------------------------\n")