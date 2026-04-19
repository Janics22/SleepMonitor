import pandas as pd
import matplotlib.pyplot as plt

# Cargar un trozo del train.csv
df = pd.read_csv('../data/processed/train.csv').head(1000)

# Dibujar la actigrafía según la fase de sueño
plt.figure(figsize=(10, 6))
plt.scatter(df.index, df['actigrafia'], c=df['target'], cmap='viridis')
plt.title('Movimiento detectado por fase de sueño')
plt.colorbar(label='Fase (0:Wake, 1:Light, 2:Deep, 3:REM)')
plt.show()