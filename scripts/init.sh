#!/usr/bin/env bash
basedir="$(pwd -P)"
cd "$basedir/MagmaCube"
$basedir/MagmaCube/scripts/init.sh || exit 1
$basedir/MagmaCube/scripts/build.sh || exit 1
cd "$basedir" || exit 1
$basedir/scripts/mcdevsrc.sh || exit 1
./scripts/applyPatches.sh
