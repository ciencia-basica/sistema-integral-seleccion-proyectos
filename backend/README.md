# Servicio-Becario-Algoritmos Backend

Contiene el codigo de la API del backend, junto con la implementacion de la comunicacion entre el 
ejecutable de algoritmos, y la logica de la interfaz.

### Ejecucion del Backend

Descargar python 3.13, y tenrer Java instalado, version minima 1.8, crear un virtual enviroment y configurar las dependencias especificadas en requirements.txt
```bash
cd backend-src

python -m venv venv

# windows
#.\venv\Scripts\activate
# Linux/Mac
#source venv/bin/activate

pip install -r requirements.txt
```

Ejecutar backend utilizando uvicorn

```bash
uvicorn main:app --reload
```
