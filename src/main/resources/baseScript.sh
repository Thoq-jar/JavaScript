#!/bin/bash

if ! command -v java &> /dev/null; then
  echo "Java is required to run this program!"
  exit 1
fi

FILENAME="mocha${RANDOM}temp${RANDOM}"
FILEPATH="/tmp/mocha/${FILENAME}.java"

JAVA_CODE=$(cat <<'EOF'
#<CODE>#
EOF)

if [ ! -d "/tmp/mocha" ]; then
    mkdir -p /tmp/mocha
fi

echo "$JAVA_CODE" | sed "s/public class .*{/public class $FILENAME {/" > "$FILEPATH"

javac "$FILEPATH"
if [ $? -ne 0 ]; then
  echo "Compilation failed!"
  exit 1
fi

java -cp /tmp/mocha "$FILENAME"

rm "$FILEPATH"
rm "/tmp/mocha/${FILENAME}.class"