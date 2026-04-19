import os

# Definir la ruta de salida
ruta_salida = '../Data/processed/'

# Crear la carpeta si no existe (no hace nada si ya existe)
os.makedirs(ruta_salida, exist_ok=True)

# Separar en Train Val i Test
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split

FOLDER_LABELS = '../data/raw/dataset/labels/'
FOLDER_MOTION = '../data/raw/dataset/motion/'


# 1. Identificar IDs
archivos_labels = [f for f in os.listdir(FOLDER_LABELS) if f.endswith('.txt')]
ids_unicos = list(set([f.split('_')[0] for f in archivos_labels]))

# 2. Dividir IDs (Sujetos)
train_ids, temp_ids = train_test_split(ids_unicos, test_size=0.30, random_state=42)
val_ids, test_ids = train_test_split(temp_ids, test_size=0.50, random_state=42)

def procesar_reducido(lista_ids):
    dataset = []
    for id_prefix in lista_ids:
        f_label = [f for f in os.listdir(FOLDER_LABELS) if f.startswith(id_prefix)][0]
        f_motion = [f for f in os.listdir(FOLDER_MOTION) if f.startswith(id_prefix)][0]

        #print(f_label)
        #print(f_motion)

        labels_data = np.loadtxt(os.path.join(FOLDER_LABELS, f_label))
        motion_data = np.loadtxt(os.path.join(FOLDER_MOTION, f_motion))

        samples_per_epoch_real = len(motion_data) // len(labels_data)

        for i, row in enumerate(labels_data):
            fase = int(row[1])
            if fase == -1: continue

            inicio = i * samples_per_epoch_real
            fin = (i + 1) * samples_per_epoch_real

            # Extraemos el bloque
            bloque = motion_data[inicio:fin, 1:4]

            vms = np.sqrt(np.sum(bloque**2, axis=1))

            # --- LAS 4 COLUMNAS ---
            actigrafia = np.sum(np.abs(np.diff(vms)))
            vm_mean = np.mean(vms) # Dice la inclinación (Gravedad)
            vm_std = np.std(vms)   # Dice si el movimiento es caótico o suave
            zcm = ((vms[:-1] < vm_mean) & (vms[1:] >= vm_mean)).sum()

            # Mapeo de fase a 4 para tener 0 = despierto, 1 = ligero, 3 = profundo, 5 = REM
            if fase == 2:
                fase = 1
            if fase == 3:
                fase = 2
            if fase == 4:
                fase = 2
            if fase == 5:
                fase = 3
            target = fase
            dataset.append([actigrafia, zcm, vm_mean, vm_std, target])

    return pd.DataFrame(dataset, columns=['actigrafia', 'zcm', 'vm_mean', 'vm_std', 'target'])

# Guardar los datos limpios
df_train = procesar_reducido(train_ids)
df_train.to_csv(os.path.join(ruta_salida, 'train.csv'), index=False)
df_train = procesar_reducido(val_ids)
df_train.to_csv(os.path.join(ruta_salida, 'val.csv'), index=False)
df_train = procesar_reducido(test_ids)
df_train.to_csv(os.path.join(ruta_salida, 'test.csv'), index=False)

import shutil
import os

# Define la ruta de la carpeta donde descomprimiste los archivos
folder_unzipped = '../data/raw/dataset'

# 1. Verificar si la carpeta existe
if os.path.exists(folder_unzipped):
    print(f"🧹 Limpiando archivos temporales en {folder_unzipped}...")

    # 2. Borrar la carpeta y todo lo que tiene dentro
    try:
        shutil.rmtree(folder_unzipped)
        print("✅ Carpeta de descompresión eliminada. Solo quedan los .csv.")
    except Exception as e:
        print(f"⚠️ No se pudo borrar la carpeta: {e}")
else:
    print("La carpeta temporal ya no existe o no fue encontrada.")

print("✅ Proceso completada.")