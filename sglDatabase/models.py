from sqlalchemy import Column, Integer, String, ForeignKey, Enum, Text, DateTime
from sqlalchemy.orm import relationship
from database import Base

class Author(Base):
    __tablename__ = "authors"
    id = Column(Integer, primary_key=True)
    first_name = Column(String(100), nullable=False)
    last_name = Column(String(100), nullable=False)
    nationality_id = Column(Integer, ForeignKey("nationalities.id"))
    books = relationship("Book", back_populates="author")

class Book(Base):
    __tablename__ = "books"
    id = Column(Integer, primary_key=True)
    title = Column(String(255))
    author_id = Column(Integer, ForeignKey("authors.id"))
    language_id = Column(Integer, ForeignKey("languages.id"))
    published_year = Column(Integer)
    pages = Column(Integer)
    isbn = Column(String(20))
    description = Column(Text)
    author = relationship("Author", back_populates="books")

class User(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True)
    username = Column(String(50), unique=True, nullable=False)
    email = Column(String(100), unique=True, nullable=False)
    password_hash = Column(String(255), nullable=False)
