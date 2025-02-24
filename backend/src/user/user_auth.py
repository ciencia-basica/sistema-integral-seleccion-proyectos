"""
Este modulo contiene el manejo de registro y autenticacion de usuarios
"""

import json
from typing import Any
from uuid import UUID, uuid4

from fastapi import HTTPException

from paths import ENCODING, USER_RECORD
from user.user_storage import del_user_storage, reset_user_storage


class UserRecordEnc(json.JSONEncoder):
    """TODO"""

    def default(self, o: Any) -> Any:
        """TODO"""
        if isinstance(o, UUID):
            return str(o)
        return super().default(o)


def user_record_dec(o: dict[str, Any]) -> Any:
    """TODO"""
    if "__uuid__" in o:
        return UUID(o["__uuid__"])
    return o


def _get_user_record() -> dict[str, UUID]:
    with open(USER_RECORD, "r", encoding=ENCODING) as file:
        return json.loads(file.read(), object_hook=user_record_dec)["users"]


def register_user(user: str) -> None:
    """TODO"""
    if user == "":
        raise HTTPException(400, detail="Nombre invalid")

    user_record = _get_user_record()
    if user in user_record:
        raise HTTPException(400, detail="Usuario ya existe")

    with open(USER_RECORD, "w", encoding=ENCODING) as file:
        uid = uuid4()
        while uid in user_record.keys():
            uid = uuid4()
        user_record[user] = uid

        reset_user_storage(user_record[user])
        file.write(json.dumps({"users": user_record}, cls=UserRecordEnc))


def delete_user(user: str) -> None:
    """TODO"""
    user_record = _get_user_record()
    if not user in user_record:
        raise HTTPException(404, detail="Usuario no existe")

    with open(USER_RECORD, "w", encoding=ENCODING) as file:
        del_user_storage(user_record[user])
        del user_record[user]
        file.write(json.dumps({"users": user_record}, cls=UserRecordEnc))


def is_user(user: str) -> bool:
    """TODO"""
    return user in _get_user_record()


def get_user_id(user: str) -> UUID:
    """TODO"""
    try:
        return _get_user_record()[user]
    except KeyError:
        # pylint: disable=raise-missing-from
        raise HTTPException(status_code=404, detail=f"usuario '{user}' no existe")
