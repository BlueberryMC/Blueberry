#!/usr/bin/env bash
echo "=================================================="
echo "WARNING! This script is deprecated and not recommended! (and does not work)"
echo "Please run ./blueberry installer and use the installer."
echo "This script will run in 20 seconds, so you can CTRL+C now to interrupt this script."
echo "=================================================="
echo ""
sleep 20s
echo "Building Blueberry-Client"
nobuild=false
if [ "$1" == "--nobuild" ]; then
  nobuild=true
fi
version=""
basedir="."
source ./scripts/functions.sh
apiversion=$(mvn -f Blueberry-API/pom.xml help:evaluate -Dexpression=project.version -q -DforceStdout)
datetime=$(date +%Y-%m-%dT%T%:z)
if [ "$nobuild" == "true" ]; then
  echo "Skipping build, installing version."
else
  $basedir/scripts/build.sh
fi
VERSIONS=~/.minecraft/versions
if [[ "$OSTYPE" == *"win"* || "$OSTYPE" == "msys" ]]; then
  VERSIONS=~/AppData/Roaming/.minecraft/versions
fi
echo "Installing version to Minecraft Launcher"
name="$version-blueberry-$apiversion"
mkdir -p "$VERSIONS/$name/"
rm -f "$VERSIONS/$name/$name.jar"
rm -f "$VERSIONS/$name/$name.json"
cp "$basedir/Blueberry-Client/target/blueberry-$version.jar" "$VERSIONS/$name/$name.jar"
cp "$basedir/scripts/files/version.json" "$VERSIONS/$name/$name.json"
echo "  \"releaseTime\": \"$datetime\"," >> "$VERSIONS/$name/$name.json"
echo "  \"time\": \"$datetime\"," >> "$VERSIONS/$name/$name.json"
echo "  \"mainClass\": \"net.minecraft.client.main.Main\"," >> "$VERSIONS/$name/$name.json"
echo "  \"id\": \"$name\"" >> "$VERSIONS/$name/$name.json"
echo "}" >> "$VERSIONS/$name/$name.json"
echo "Done. Please restart the Minecraft Launcher to make sure you see blueberry."
