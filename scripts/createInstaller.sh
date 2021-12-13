#!/usr/bin/env bash
basedir="."
BN=$1
if [ -z "$BN" ]; then
  BN="0"
fi
source "$basedir/scripts/functions.sh" || exit 1
git="git -c commit.gpgsign=false"
apiversion=$(mvn -f Blueberry-API/pom.xml help:evaluate -Dexpression=project.version -q -DforceStdout)
datetime=$(date +%Y-%m-%dT%T%:z)
if [ ! -e "$basedir/work/Installer" ]; then
  $git clone https://github.com/BlueberryMC/Installer "$basedir/work/Installer" || exit 1
else
  cd "$basedir/work/Installer" || exit 1
  $git fetch || exit 1
  $git checkout origin/main || exit 1
  cd "$basedir" || exit 1
fi
name="blueberry-$version-$apiversion.$BN"
res="$basedir/work/Installer/src/main/resources"
prop="$res/profile.properties"
echo "name=$name" > "$prop"
echo "hideServer=true" >> "$prop" # server is not supported... i assume.
echo "extractFiles=client.jar,client.json,profile.properties" >> "$prop"
cp "$basedir/work/jbsdiffPatcher/target/jbsdiffPatcher.jar" "$res/client.jar" || exit 1
cp "$basedir/scripts/files/version.json" "$res/client.json" || exit 1
echo "  \"releaseTime\": \"$datetime\"," >> "$res/client.json"
echo "  \"time\": \"$datetime\"," >> "$res/client.json"
echo "  \"mainClass\": \"net.blueberrymc.client.main.ClientMain\"," >> "$res/client.json"
echo "  \"id\": \"$name\"" >> "$res/client.json"
echo "}" >> "$res/client.json"
echo "Baking installer"
cd "$basedir/work/Installer" || exit 1
mvn clean package
cp "$basedir/work/Installer/target/Installer.jar" "$basedir/$name-installer.jar"
echo "Done. Installer is located at: $basedir/$name-installer.jar"
