"""
Este modulo contiene la constante EXEC_FILES_DIR
"""

from os import path
from uuid import UUID

SRC_DIR = path.dirname(path.abspath(__file__))

USER_DEFAULT_FILES = path.join(SRC_DIR, "executable.zip")
EXEC_FILES_DIR = "Files"
ENCODING = "utf-8"
TXT_EXT = ".txt"
USER_RECORD = path.join(SRC_DIR, "users.json")
USER_STORAGE = path.join(SRC_DIR, "user_storage")

# logging

ERRORS_LOG = path.join(SRC_DIR, "errors.log")


def get_user_path(user_id: UUID) -> str:
    """Retorna el path al directorio del usuario"""
    return path.join(USER_STORAGE, str(user_id))
