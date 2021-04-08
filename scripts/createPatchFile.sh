#!/usr/bin/env bash
source ./scripts/functions.sh
git="git -c commit.gpgsign=false"
rm -rf "$basedir/work/jbsdiff"
$git clone https://github.com/malensek/jbsdiff "$basedir/work/jbsdiff" || exit 1
cd ./work/jbsdiff || exit 1
mvn clean package || exit 1
cd "$basedir" || exit 1
rm -rf "$basedir/work/jbsdiffPatcher"
$git clone https://github.com/BlueberryMC/jbsdiffPatcher "$basedir/work/jbsdiffPatcher" || exit 1
mkdir -p "$basedir/work/jbsdiffPatcher/src/main/resources/" || exit 1
patchFile="$basedir/work/jbsdiffPatcher/src/main/resources/patch.bz2"
vanillaUrl="$clientJarUrl"
vanillaHash=$(sha256sum "$basedir/MagmaCube/work/Minecraft/$version/client.jar" | head -c 64) || exit 1
patchedHash=$(sha256sum "$basedir/Blueberry-Client/target/blueberry-$version.jar" | head -c 64) || exit 1
echo "name=blueberry" > "$basedir/work/jbsdiffPatcher/src/main/resources/patch.properties"
echo "version=$version" >> "$basedir/work/jbsdiffPatcher/src/main/resources/patch.properties"
echo "vanillaUrl=$vanillaUrl" >> "$basedir/work/jbsdiffPatcher/src/main/resources/patch.properties"
echo "vanillaHash=$vanillaHash" >> "$basedir/work/jbsdiffPatcher/src/main/resources/patch.properties"
echo "patchedHash=$patchedHash" >> "$basedir/work/jbsdiffPatcher/src/main/resources/patch.properties"
echo "Creating patch"
java -jar "$basedir/work/jbsdiff/target/jbsdiff-1.0.jar" diff "$basedir/MagmaCube/work/Minecraft/$version/client.jar" "$basedir/Blueberry-Client/target/blueberry-$version.jar" "$patchFile" || exit 1
cd "$basedir/work/jbsdiffPatcher" || exit 1
mvn clean package || exit 1
cp "$basedir/work/jbsdiffPatcher/target/jbsdiffPatcher.jar" "$basedir/blueberry-$version.jar"
