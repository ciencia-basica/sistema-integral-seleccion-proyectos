"""
Este modulo contiene el manejo de registro y autenticacion de usuarios
"""

import json
from typing import Any
from uuid import UUID, uuid4
from pydantic import BaseModel
from fastapi import HTTPException
from paths import ENCODING, USER_RECORD
from user.user_storage import del_user_storage, reset_user_storage

# Modelo para recibir los datos del usuario
class UserCreate(BaseModel):
    email: str
    password: str

class UserRecordEnc(json.JSONEncoder):
    """TODO"""
    def default(self, o: Any) -> Any:
        """TODO"""
        if isinstance(o, UUID):
            return str(o)
        return super().default(o)

def user_record_dec(o: dict[str, Any]) -> Any:
    """TODO"""
    if '__uuid__' in o:
        return UUID(o['__uuid__'])
    return o


def _get_user_record() -> dict[str, UUID]:
    with open(USER_RECORD, "r", encoding=ENCODING) as file:
        return json.loads(file.read(), object_hook=user_record_dec)["users"]


def _save_user_record(user_record: dict) -> None:
    with open(USER_RECORD, "w", encoding=ENCODING) as file:
        json.dump({"users": user_record}, file, ensure_ascii=False, indent=4)

def register_user(user: UserCreate):
    user_record = _get_user_record()
    # Verificar si el correo ya está registrado
    if user.email in user_record:
        raise HTTPException(status_code=400, detail="El correo ya está registrado")

    # Generar un UID único para el usuario
    uid = str(uuid4())
    while uid in user_record.values():
        uid = str(uuid4())

    # Guardar el nuevo usuario
    user_record[user.email] = {"uid": uid, "password": user.password}
    reset_user_storage(user_record[user.email]["uid"])
    _save_user_record(user_record)

    # Simular el envío de un correo de confirmación
    #send_confirmation_email(user.email)

    return {"response": "Usuario registrado correctamente. Por favor, confirme su correo."}


def delete_user(user: str) -> None:
    user_record = _get_user_record()
    if user.email not in user_record:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")

    if user_record[user.email]["password"] != user.password:
        raise HTTPException(status_code=403, detail="Contraseña incorrecta")
    
    # Obtener el UID del usuario y eliminar su almacenamiento
    user_id = user_record[user.email]["uid"]
    del_user_storage(UUID(user_id))

    # Eliminar al usuario del registro
    del user_record[user.email]
    _save_user_record(user_record)

    return {"response": f"Usuario '{user.email}' eliminado exitosamente"}

def login_user_(user: str) -> bool:
    # Verificar si el usuario existe
    user_record = _get_user_record()
    if user.email not in user_record:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")

    # Obtener la contraseña almacenada para este usuario
    stored_password = user_record[user.email]["password"]

    # Comparar la contraseña ingresada con la almacenada
    if user.password == stored_password:
        return True  # Devolver True en caso de éxito
    else:
        return False
        raise HTTPException(status_code=403, detail="Contraseña incorrecta")  # Error si la contraseña es incorrecta
      

def is_user(user: str) -> bool:
    """TODO
    """
    return user in _get_user_record()

def get_user_id(user: str) -> UUID:
    """TODO
    """
    try:
        return _get_user_record()[user]
    except KeyError:
        # pylint: disable=raise-missing-from
        raise HTTPException(status_code=404, detail=f"usuario '{user}' no existe")
