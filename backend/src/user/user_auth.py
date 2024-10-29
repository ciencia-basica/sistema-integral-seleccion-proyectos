import json
from typing import Any
from uuid import UUID, uuid4
from fastapi import HTTPException
from user.firebase_config import db  # Importa la configuración de Firebase
from user.user_storage import del_user_storage, reset_user_storage


class UserRecordEnc(json.JSONEncoder):
    """Encode UUID as string."""
    def default(self, o: Any) -> Any:
        if isinstance(o, UUID):
            return str(o)
        return super().default(o)


def user_record_dec(o: dict[str, Any]) -> Any:
    """Decode JSON to UUID."""
    if '__uuid__' in o:
        return UUID(o['__uuid__'])
    return o


def _get_user_record() -> dict[str, UUID]:
    # Lee los registros de usuario de Firestore en lugar del archivo local
    user_record = {}
    users_ref = db.collection("users")
    docs = users_ref.stream()
    for doc in docs:
        user_record[doc.id] = UUID(doc.get("id"))
    return user_record


def register_user(user: str) -> None:
    """Register a new user."""
    if user == "":
        raise HTTPException(400, detail="Nombre inválido")

    user_record = _get_user_record()
    if user in user_record:
        raise HTTPException(400, detail="Usuario ya existe")

    uid = uuid4()
    while uid in user_record.values():
        uid = uuid4()

    # Guarda en Firestore
    db.collection("users").document(user).set({"id": str(uid)})
    reset_user_storage(uid)


def delete_user(user: str) -> None:
    """Delete an existing user."""
    user_record = _get_user_record()
    if user not in user_record:
        raise HTTPException(404, detail="Usuario no existe")

    # Elimina de Firestore
    del_user_storage(user_record[user])
    db.collection("users").document(user).delete()


def is_user(user: str) -> bool:
    """Check if a user exists."""
    user_record = _get_user_record()
    return user in user_record


def get_user_id(user: str) -> UUID:
    """Get a user's UUID."""
    try:
        user_record = _get_user_record()
        return user_record[user]
    except KeyError:
        raise HTTPException(status_code=404, detail=f"usuario '{user}' no existe")
