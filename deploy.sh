#!/bin/bash

# ==============================
# CONFIGURATION
# ==============================
APP_NAME="framework"
SRC_DIR="src"
OUT_DIR="out"
BIN_DIR="bin"
LIB_DIR="lib"
SERVLET_API_JAR="$LIB_DIR/servlet-api.jar"
JAR_NAME="$APP_NAME.jar"

# ==============================
# CLEAN OLD BUILD
# ==============================

rm -rf $OUT_DIR $BIN_DIR $JAR_NAME
mkdir -p $OUT_DIR $BIN_DIR

# ==============================
# COMPILE JAVA FILES
# ==============================
echo "Compilation des sources..."

find $SRC_DIR -name "*.java" > sources.txt

javac -cp $SERVLET_API_JAR -d $OUT_DIR @sources.txt

if [ $? -ne 0 ]; then
    echo "Erreur de compilation"
    exit 1
fi


# ==============================
# CREATE JAR
# ==============================
echo "Création du JAR..."

jar cf $JAR_NAME  -C $OUT_DIR .

if [ $? -eq 0 ]; then
    echo "JAR créé avec succès : $JAR_NAME"
else
    echo "Erreur lors de la création du JAR"
    exit 1
fi

# ==============================
# CLEAN TEMP FILES
# ==============================
rm -f sources.txt 

echo "Build terminé"