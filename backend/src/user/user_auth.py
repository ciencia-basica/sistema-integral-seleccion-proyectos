import json
from typing import Any
from uuid import UUID, uuid4
from fastapi import HTTPException
from user.firebase_config import db  # Importa la configuración de Firebase 
from user.user_storage import del_user_storage, reset_user_storage


class UserRecordEnc(json.JSONEncoder):
    """Clase para codificar UUID como cadena de texto."""
    def default(self, o: Any) -> Any:
        if isinstance(o, UUID):
            return str(o)  # Convierte UUID a string para codificación JSON
        return super().default(o)


def user_record_dec(o: dict[str, Any]) -> Any:
    """Decodifica JSON y convierte cadenas de texto a UUID."""
    if '__uuid__' in o:
        return UUID(o['__uuid__'])  
    return o


def _get_user_record() -> dict[str, UUID]:
    # Recupera todos los registros de usuarios desde Firestore.
    user_record = {}
    users_ref = db.collection("users")
    docs = users_ref.stream()  
    for doc in docs:
        user_record[doc.id] = UUID(doc.get("id"))  # Guarda el ID del usuario como UUID
    return user_record


def register_user(user: str) -> None:
    """Registra un nuevo usuario en Firestore."""
    if user == "":
        raise HTTPException(400, detail="Nombre inválido")  

    user_record = _get_user_record()
    if user in user_record:
        raise HTTPException(400, detail="Usuario ya existe")  

    uid = uuid4()  # Genera un UUID único para el nuevo usuario
    while uid in user_record.values():
        uid = uuid4()  # Asegura que el UUID es único

    # Guarda el nuevo usuario en Firestore
    db.collection("users").document(user).set({"id": str(uid)})
    reset_user_storage(uid)  # Restablece el almacenamiento del usuario


def delete_user(user: str) -> None:
    """Elimina un usuario existente de Firestore y su almacenamiento."""
    user_record = _get_user_record()
    if user not in user_record:
        raise HTTPException(404, detail="Usuario no existe")  # Verifica si el usuario existe
    try:
        # Elimina el almacenamiento y el registro de usuario en Firestore
        del_user_storage(user_record[user])
        db.collection("users").document(user).delete()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error al eliminar usuario: {str(e)}")


def is_user(user: str) -> bool:
    """Verifica si un usuario existe en Firestore."""
    user_record = _get_user_record()
    return user in user_record  


def get_user_id(user: str) -> UUID:
    """Obtiene el UUID de un usuario dado."""
    try:
        user_record = _get_user_record()
        return user_record[user]  # Devuelve el UUID del usuario
    except KeyError:
        raise HTTPException(status_code=404, detail=f"usuario '{user}' no existe")  
