#!/usr/bin/env bash
source ./scripts/functions.sh
basedir="$(pwd -P)"
git="git -c commit.gpgsign=false"
apply="$git am --3way --ignore-whitespace"
windows="$([[ "$OSTYPE" == "cygwin" || "$OSTYPE" == "msys" ]] && echo "true" || echo "false")"
mkdir -p "$basedir/MagmaCube-Patches"
echo "Resetting Blueberry-Client..."
mkdir -p "$basedir/Blueberry-Client"
cd "$basedir/Blueberry-Client" || exit 1
$git init
$git remote rm upstream > /dev/null 2>&1
$git remote add upstream "$basedir/MagmaCube/Minecraft" >/dev/null 2>&1
$git checkout master 2>/dev/null || $git checkout -b master
$git fetch upstream >/dev/null 2>&1
$git reset --hard upstream/master
echo "  Applying patches to Blueberry-Client..."
$git am --abort >/dev/null 2>&1
if [[ $windows == "true" ]]; then
  echo "  Using workaround for Windows ARG_MAX constraint"
  find "$basedir/MagmaCube-Patches/"*.patch -print0 | xargs -0 $apply
else
  $apply "$basedir/MagmaCube-Patches/"*.patch
fi
if [ "$?" != "0" ]; then
  echo "  Something did not apply cleanly to Blueberry-Client."
  echo "  Please review above details and finish the apply then"
  echo "  save the changes with rebuildPatches.sh"
  if [[ $windows == "true" ]]; then
    echo ""
    echo "  Because you're on Windows you'll need to finish the AM,"
    echo "  rebuild all patches, and then re-run the patch apply again."
    echo "  Consider using the scripts with Windows Subsystem for Linux."
  fi
  exit 1
else
  echo "  Patches applied cleanly to Blueberry-Client"
fi
