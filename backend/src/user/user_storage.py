"""
Este modulo maneja los directorios de los usuarios
"""

"""
Este módulo maneja los directorios de los usuarios utilizando Firebase.
"""

import os
import shutil
import uuid
from os import path
from zipfile import ZipFile
from paths import ENCODING, USER_DEFAULT_FILES, USER_STORAGE, get_user_path
from user.firebase_config import db  # Asegúrate de que esta importación es correcta

# Define la colección de usuarios en Firestore
USERS_COLLECTION = "users"  # Colección de usuarios en Firestore


def assert_user_storage() -> None:
    """Asegura que el almacenamiento de usuarios esté configurado."""
    if not path.exists(USER_STORAGE):
        os.mkdir(USER_STORAGE)


def reset_user_storage(user_id: uuid.UUID) -> None:
    """Actualiza el directorio de archivos del usuario especificado."""
    user_path = get_user_path(user_id)
    if path.exists(user_path):
        shutil.rmtree(user_path)
    os.mkdir(user_path)  # Crea el directorio del usuario
    with ZipFile(USER_DEFAULT_FILES, "r") as zip_ref:
        zip_ref.extractall(user_path)  # Extrae archivos por defecto


def create_user(user_id: uuid.UUID, user_data: dict) -> None:
    """Crea un nuevo usuario en Firestore y restablece su almacenamiento."""
    db.collection(USERS_COLLECTION).document(get_user_path(user_id)).set(user_data)
    reset_user_storage(user_id)  # Extrae los archivos por defecto al crear el usuario


def get_user(user_id: uuid.UUID) -> dict:
    """Obtiene los datos de un usuario desde Firestore."""
    user_doc = db.collection(USERS_COLLECTION).document(get_user_path(user_id)).get()
    return user_doc.to_dict() if user_doc.exists else None



def del_user_storage(user_id: uuid.UUID) -> None:
    """Elimina el directorio de archivos del usuario especificado."""
    user_path = get_user_path(user_id)
    if path.exists(user_path):
        shutil.rmtree(user_path)

    user_doc = db.collection(USERS_COLLECTION).document(str(user_id)).get()
    if user_doc.exists:
        db.collection(USERS_COLLECTION).document(str(user_id)).delete()
    else:
        print(f"Usuario {user_id} no existe en Firestore.")
