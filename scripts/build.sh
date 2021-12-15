#!/usr/bin/env bash
basedir="."
BN=$1
if [ -z "$BN" ]; then
  BN="0"
fi
source ./scripts/functions.sh
apiversion=$(mvn -f Blueberry-API/pom.xml help:evaluate -Dexpression=project.version -q -DforceStdout | sed s/-SNAPSHOT//)
datetime=$(date +%Y-%m-%dT%T%:z)
commit=$(git rev-parse HEAD)
cd "$basedir/MagmaCube/Minecraft" || exit 1
magmacubeCommit=$(git rev-parse HEAD)
cd "$basedir" || exit 1
version_prop="$basedir/Blueberry-API/src/main/resources/api-version.properties"
echo "name=blueberry" > "$version_prop"
echo "version=$apiversion" >> "$version_prop"
echo "magmaCubeCommit=$magmacubeCommit" >> "$version_prop"
echo "commit=$commit" >> "$version_prop"
echo "builtAt=$datetime" >> "$version_prop"
echo "buildNumber=$BN" >> "$version_prop"
echo "" >> "$version_prop"
echo "api-version.properties:"
cat "$version_prop"
mvn clean install || exit 1
git restore "$basedir/Blueberry-API/src/main/resources/api-version.properties"
