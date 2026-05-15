# -*- coding: utf-8 -*-
"""
Dataset de Oro (Golden Dataset) para la evaluación de modelos RAG de sueño.
Contiene casos estructurados y controlados matemáticamente para probar las reglas de la API.
"""

GOLDEN_DATASET = [
    {
        "id": 1,
        "descripcion": "Déficit severo de sueño profundo (< 15%)",
        "datos": {"total": 7.5, "rem": 1.8, "profundo": 0.6, "ligero": 5.1}
    },
    {
        "id": 2,
        "descripcion": "Buen sueño profundo pero déficit de fase REM (< 20%)",
        "datos": {"total": 8.0, "rem": 1.1, "profundo": 1.8, "ligero": 5.1}
    },
    {
        "id": 3,
        "descripcion": "Sueño equilibrado y saludable (Caso de control)",
        "datos": {"total": 7.5, "rem": 1.7, "profundo": 1.5, "ligero": 4.3}
    },
    {
        "id": 4,
        "descripcion": "Insomnio severo / Pocas horas totales (Caso crítico)",
        "datos": {"total": 4.0, "rem": 0.6, "profundo": 0.4, "ligero": 3.0}
    },
    {
        "id": 5,
        "descripcion": "Fase REM en el límite mínimo (~18%)",
        "datos": {"total": 7.0, "rem": 1.3, "profundo": 1.4, "ligero": 4.3}
    }
]