#!/usr/bin/env bash
basedir="$(cd "$1" && pwd -P)"
workdir="$basedir/work"
git="git -c commit.gpgsign=false"
mcdev="$workdir/mcdev"
quickunzip="$basedir/MagmaCube/work/quickunzip/quickunzip.jar"
mkdir -p "$mcdev" || exit 1
mkdir -p "$mcdev/brigadier" || exit 1
cd "$mcdev" || exit 1
brigadier="https://libraries.minecraft.net/com/mojang/brigadier/1.0.18/brigadier-1.0.18-sources.jar"
echo "Downloading brigadier"
curl "$brigadier" --output "$mcdev/brigadier.zip"
if [ $? != 0 ]; then
  echo "Could not download brigadier source, please check for errors above, fix it, then run again."
  exit 1
fi
java -Xmx1G -jar "$quickunzip" -q "$mcdev/brigadier.zip" "$mcdev/brigadier"

export MODLOG=""

function import {
  dir="${1}"
  file="${2}.java"
  base="$mcdev/$dir/com/mojang/$dir/$file"
  targetDir="$basedir/MagmaCube/Minecraft/src/main/java/com/mojang/"
  mkdir -p "$targetDir/$dir"
  target="$targetDir/$dir/$file"
  if [[ ! -f "$target" ]]; then
    export MODLOG="$MODLOG  Imported $file from $dir\n";
    echo "Copying $base to $target"
    cp "$base" "$target"
  else
    echo "un-needed import: $file ($base)"
  fi
}

cd "$basedir/MagmaCube/Minecraft" || exit 1
lastlog=$($git log -1 --oneline)
if [[ "$lastlog" = *"mc-dev Imports"* ]]; then
  $git reset --hard HEAD^
fi
cd "$basedir" || exit 1

#############################################
#             mc-dev Imports
#
# Import libraries like brigadier here.
#

import brigadier CommandDispatcher

cd "$basedir/MagmaCube/Minecraft" || exit 1
$git add src/main/java/com/mojang/ -A >/dev/null 2>&1
echo -e "mc-dev Imports\n\n$MODLOG" | $git commit . -F -
