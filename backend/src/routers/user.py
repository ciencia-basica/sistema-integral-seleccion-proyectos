"""
Contiene los endpoint relacionados a los usuarios
"""

from fastapi import APIRouter, Body
from user.user_auth import delete_user, get_user_id, is_user, register_user, login_user_
from user.user_storage import assert_user_storage, reset_user_storage
from pydantic import BaseModel, EmailStr

router = APIRouter(
    prefix="/user",
    tags=["user"],
)
# Modelo para recibir los datos del usuario
class UserCreate(BaseModel):
    email: EmailStr
    password: str

assert_user_storage()

# @router.get("/{user}")
# async def exists_user(user: str) -> dict[str, bool]:
#     """Registra un nuevo usuario"""
#     exists = is_user(user)
#     return {"exists":exists}

@router.post("/register")
async def reg_user(user: UserCreate = Body(...)) -> dict[str, str]:
    """Registra un nuevo usuario con email y contraseña"""
    register_user(user)
    return {"response": f"Usuario '{user.email}' registrado"}

@router.delete("/delete")
async def del_user(user: UserCreate) -> dict[str, str]:
    """Elimina un usuario"""
    delete_user(user)
    return {"response":f"Usuario '{user}' eliminado"}

@router.post("/reset/{user}") #To do
async def reset_user(user: str) -> dict[str, str]:
    """Resetea los archivos de un usuario"""
    reset_user_storage(get_user_id(user))
    return {"response":f"Usuario '{user}' reseteado"}

@router.post("/login")
async def login_user(user: UserCreate) -> dict[str, bool]:
    print(user.password)
    print(user)
    """Valida el login de un usuario sin encriptación (texto plano)"""
    success = login_user_(user) 
    return {"login_success": success}