"""
Este modulo maneja los directorios de los usuarios
"""

import json
import os
import shutil
import uuid
from os import path
from zipfile import ZipFile

from paths import ENCODING, USER_DEFAULT_FILES, USER_RECORD, USER_STORAGE, get_user_path


def assert_user_storage() -> None:
    """TODO"""
    if not path.exists(USER_RECORD):
        with open(USER_RECORD, "w", encoding=ENCODING) as file:
            user_json = json.dumps({"users": {}})
            file.write(user_json)

    if not path.exists(USER_STORAGE):
        os.mkdir(USER_STORAGE)


def reset_user_storage(user_id: uuid.UUID) -> None:
    """Actualiza el directorio de archivos del usuario especificado"""
    user_path = get_user_path(user_id)
    if path.exists(user_path):
        shutil.rmtree(user_path)
    with ZipFile(USER_DEFAULT_FILES, "r") as zip_ref:
        zip_ref.extractall(user_path)


def del_user_storage(user_id: uuid.UUID) -> None:
    """Borra el directorio de archivos del usuario especificado"""
    shutil.rmtree(get_user_path(user_id))
