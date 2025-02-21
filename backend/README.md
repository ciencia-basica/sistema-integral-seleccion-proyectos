# Servicio-Becario-Algoritmos Backend

Contiene el codigo de la API del backend, expone una interfaz para la
comunicacion entre el ejecutable de algoritmos, y la logica de la interfaz.

### Dependencias

La api utilize minimamene la version 3.12 de python, tambien require un java
runtime, se sugiere JDK 21. La api usa el framework FastAPI, esta y las demas
dependencias de python estan especificadas en requirements.txt

### Ejecucion del Backend

Instalar previamente las dependencias, luego crea un virtual environment e
instala las dependencias de python:

```bash
cd backend

python -m venv venv

# activa el virtual environment
# windows
#.\venv\Scripts\activate
# Linux/Mac
#source venv/bin/activate

pip install -r requirements.txt
```

Ejecutar backend utilizando uvicorn

```bash
py src/main.py

# u opcionalmente

cd src
uvicorn main:app --reload
```
