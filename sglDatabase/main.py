from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from database import SessionLocal, engine
import models, schemas
import os
from dotenv import load_dotenv


models.Base.metadata.create_all(bind=engine)

app = FastAPI()

# Dependency to get DB session
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

# Books endpoints
@app.get("/books/", response_model=List[schemas.BookSchema])
def read_books(db: Session = Depends(get_db)):
    return db.query(models.Book).all()

@app.post("/books/", response_model=schemas.BookSchema)
def create_book(book: schemas.BookCreate, db: Session = Depends(get_db)):
    db_book = models.Book(**book.dict())
    db.add(db_book)
    db.commit()
    db.refresh(db_book)
    return db_book

# Authors
@app.get("/authors/", response_model=List[schemas.AuthorSchema])
def read_authors(db: Session = Depends(get_db)):
    return db.query(models.Author).all()

@app.get("/")
def root():
    return {"message": "Witaj w API biblioteki!"}

if __name__ == "__main__":
    import uvicorn
    load_dotenv()

    ssl_keyfile = os.getenv("SSL_KEYFILE")
    ssl_certfile = os.getenv("SSL_CERTFILE")
    ssl_port = int(os.getenv("SSL_PORT", 8000))

    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=ssl_port,
        reload=True,
        ssl_keyfile=ssl_keyfile,
        ssl_certfile=ssl_certfile
    )

# Uruchamianie API:
# uvicorn main:app --reload
# Uruchamianie API z certyfikatem SSL:
# uvicorn main:app --reload --ssl-keyfile=key.pem --ssl-certfile=cert.pem
# Adres pod którym działa API:
# https://127.0.0.1:8000/book/1