"""
Contiene los endpoint relacionados a los usuarios
"""

from fastapi import APIRouter
from user.user_auth import delete_user, get_user_id, is_user, register_user
from user.user_storage import assert_user_storage, reset_user_storage

router = APIRouter(
    prefix="/user",
    tags=["user"],
)

assert_user_storage()


@router.get("/{user}")
async def exists_user(user: str) -> dict[str, bool]:
    """Registra un nuevo usuario"""
    exists = is_user(user)
    return {"exists": exists}


@router.post("/{user}")
async def reg_user(user: str) -> dict[str, str]:
    """Registra un nuevo usuario"""
    register_user(user)
    return {"response": f"Usuario '{user}' registrado"}


@router.delete("/{user}")
async def del_user(user: str) -> dict[str, str]:
    """Elimina un usuario"""
    delete_user(user)
    return {"response": f"Usuario '{user}' eliminado"}


@router.post("/reset/{user}")
async def reset_user(user: str) -> dict[str, str]:
    """Resetea los archivos de un usuario"""
    reset_user_storage(get_user_id(user))
    return {"response": f"Usuario '{user}' reseteado"}
