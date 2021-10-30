#!/usr/bin/env bash
basedir="$(pwd -P)"
git submodule update --init
cd "$basedir/MagmaCube" || exit 1
git submodule update --init
echo "Checked out: $(git log --oneline HEAD -1)"
echo "MagmaCube Version: $(head -2 $basedir/MagmaCube/scripts/functions.sh | tail -1 | cut -c9-)"
$basedir/MagmaCube/scripts/init.sh || exit 1
$basedir/MagmaCube/scripts/build.sh || exit 1
cd "$basedir" || exit 1
$basedir/scripts/mcdevsrc.sh || exit 1
./scripts/applyPatches.sh
