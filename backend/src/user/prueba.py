import json
from typing import Any
from uuid import UUID, uuid4
from fastapi import HTTPException
from firebase_config import db  # Importa la configuración de Firebase
from user_storage import del_user_storage, reset_user_storage
from paths import ENCODING, USER_DEFAULT_FILES, USER_STORAGE, get_user_path

db.collection("users").document('omar').delete()


class UserRecordEnc(json.JSONEncoder):
    """Encode UUID as string."""
    def default(self, o: Any) -> Any:
        if isinstance(o, UUID):
            return str(o)
        return super().default(o)




data = {"user_id": uuid4()}
json_data = json.dumps(data, cls=UserRecordEnc)  # Usa UserRecordEnc para serializar
print(json_data)