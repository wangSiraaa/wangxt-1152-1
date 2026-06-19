#!/bin/bash
set -e

cd "$(dirname "$0")/.."

JACKSON_CORE=/Users/mingyuan/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.15.2/jackson-core-2.15.2.jar
JACKSON_DATABIND=/Users/mingyuan/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.15.2/jackson-databind-2.15.2.jar
JACKSON_ANNOTATIONS=/Users/mingyuan/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.15.2/jackson-annotations-2.15.2.jar
SQLITE_JDBC=/Users/mingyuan/.m2/repository/org/xerial/sqlite-jdbc/3.41.2.2/sqlite-jdbc-3.41.2.2.jar

CP="target/classes:src/main/resources:$JACKSON_CORE:$JACKSON_DATABIND:$JACKSON_ANNOTATIONS:$SQLITE_JDBC"

echo "Starting Forest Pest Monitor Server..."
echo "Classpath: $CP"

exec java -cp "$CP" nc.forest.server.ForestServer 20452
