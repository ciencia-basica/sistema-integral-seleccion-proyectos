import firebase_admin
from firebase_admin import credentials, auth, firestore

# Inicializa Firebase Admin con tu archivo de credenciales
cred = credentials.Certificate('/Users/omar/Documents/GitHub/sistema-integral-seleccion-proyectos/backend/src/servicio-becario-algoritmos-firebase-adminsdk-nkadc-e57fe3feef.json')
firebase_admin.initialize_app(cred)

# Inicializa Firestore
db = firestore.client()


