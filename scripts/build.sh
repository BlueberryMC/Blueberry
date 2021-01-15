#!/usr/bin/env bash
version=""
basedir="."
source ./scripts/functions.sh
WORLD_VERSION=2586
PROTOCOL_VERSION=754
PACK_VERSION=6
apiversion=$(mvn -f Blueberry-API/pom.xml help:evaluate -Dexpression=project.version -q -DforceStdout)
datetime=$(date +%Y-%m-%dT%T%:z)
commit=$(git rev-parse HEAD | head -c 10)
cd "$basedir/MagmaCube/Minecraft" || exit 1
magmacubeCommit=$(git rev-parse HEAD | head -c 10)
cd "$basedir" || exit 1
version_prop="$basedir/Blueberry-API/src/main/resources/api-version.properties"
version_prop_bak="$basedir/Blueberry-API/src/main/resources/api-version.properties.bak"
cp -f "$version_prop" "$version_prop_bak" || exit 1
echo "name=blueberry" > "$version_prop"
echo "version=$apiversion" >> "$version_prop"
echo "magmaCubeCommit=$magmacubeCommit" >> "$version_prop"
echo "commit=$commit" >> "$version_prop"
echo "builtAt=$datetime" >> "$version_prop"
echo "" >> "$version_prop"
echo "api-version.properties:"
cat "$version_prop"
mvn clean install
cp -f "$version_prop_bak" "$version_prop" || exit 1
rm -f "$version_prop_bak"
