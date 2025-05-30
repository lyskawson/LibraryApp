from pydantic import BaseModel
from typing import Optional

class AuthorSchema(BaseModel):
    id: int
    first_name: str
    last_name: str

    class Config:
        orm_mode = True

class BookSchema(BaseModel):
    id: int
    title: Optional[str]
    published_year: Optional[int]

    class Config:
        orm_mode = True

class BookCreate(BaseModel):
    title: str
    author_id: int
    published_year: Optional[int]
