import os
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from dotenv import load_dotenv

# Załaduj dane z pliku .env
load_dotenv()

# Konfiguracja połączenia z bazą danych
DB_HOST = os.getenv("DB_HOST", "localhost")
DB_PORT = os.getenv("DB_PORT", "3306")
DB_USER = os.getenv("DB_USER", "root")
DB_PASSWORD = os.getenv("DB_PASSWORD", "")
DB_NAME = os.getenv("DB_NAME", "biblioteka")
USE_SSL = os.getenv("USE_SSL", "false").lower() == "true"

# Składamy URL połączenia z MySQL (przez PyMySQL)
DATABASE_URL = (
    f"mysql+pymysql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}"
)

# Opcjonalne argumenty SSL
if USE_SSL:
    ssl_ca = os.getenv("SSL_CA")
    ssl_cert = os.getenv("SSL_CERT")
    ssl_key = os.getenv("SSL_KEY")

    if not all([ssl_ca, ssl_cert, ssl_key]):
        raise ValueError("Brakuje plików SSL")

    ssl_args = {
        "ssl": {
            "ca": ssl_ca,
            "cert": ssl_cert,
            "key": ssl_key,
            "check_hostname": False,
            "verify_cert": False
        }
    }

engine = create_engine(
    DATABASE_URL,
    connect_args=ssl_args if USE_SSL else {}
)

# Tworzenie fabryki sesji
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Klasa bazowa do modeli
Base = declarative_base()

# Dorobic SSL
# rozbic books


#Z DZISIAJ
# dorobic master/slaave (docker?)
# dorobic ssl
