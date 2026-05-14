Codigo usado para simular una consulta:
Invoke-RestMethod -Uri "http://localhost:8000/obtener_consejo" -Method Post -ContentType "application/json" -Body '{"total": 6.5, "rem": 0.8, "profundo": 1.2, "ligero": 4.5}' 
