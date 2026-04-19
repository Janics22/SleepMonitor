import tensorflow as tf
import pandas as pd
import numpy as np
import seaborn as sns
import matplotlib.pyplot as plt
from sklearn.metrics import confusion_matrix
import os

# --- 1. CONFIGURACIÓN DE RUTAS ---
# Ajustamos las rutas porque estamos dentro de la carpeta 'experiments'
PATH_MODELO = '../models/model_sleep.h5'
PATH_TEST_CSV = '../data/processed/test.csv'

# --- 2. CARGAR EL MODELO ---
print("Cargando el cerebro de la IA...")
# Cargamos el archivo .h5 (el modelo completo de Keras)
model = tf.keras.models.load_model(PATH_MODELO)

# --- 3. PREPARAR LOS DATOS DE TEST ---
print("Preparando datos de prueba...")
df_test = pd.read_csv(PATH_TEST_CSV)
X_test = df_test.drop('target', axis=1).values
y_true = df_test['target'].values

# --- 4. OBTENER PREDICCIONES ---
print("La IA está analizando los datos de test...")
y_pred_prob = model.predict(X_test)
y_pred_classes = np.argmax(y_pred_prob, axis=1)

# --- 5. CREAR Y GRAFICAR LA MATRIZ DE CONFUSIÓN ---
# Definimos los nombres de las fases para que la gráfica se entienda
fases = ['Wake', 'Light', 'Deep', 'REM']

cm = confusion_matrix(y_true, y_pred_classes)

plt.figure(figsize=(10, 8))
sns.heatmap(cm, annot=True, fmt='d', cmap='Blues',
            xticklabels=fases, yticklabels=fases)

plt.title('Matriz de Confusión: ¿Qué tan bien duerme la IA?')
plt.xlabel('Predicción de la IA')
plt.ylabel('Realidad (Label)')
plt.show()