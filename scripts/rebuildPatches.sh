#!/usr/bin/env bash
basedir="$(pwd -P)"
git="git -c commit.gpgsign=false -c core.safecrlf=false"
echo "Rebuilding patch files..."
mkdir -p "$basedir/MagmaCube-Patches"
cd "$basedir/MagmaCube-Patches" || exit 1
rm -rf -- *.patch
cd "$basedir/Blueberry-Client" || exit 1
$git format-patch --zero-commit --full-index --no-signature --no-stat -N -o "$basedir/MagmaCube-Patches/" upstream/master >/dev/null
cd "$basedir" || exit 1
$git add -A "$basedir/MagmaCube-Patches"
cd "$basedir/MagmaCube-Patches" || exit 1
for patch in *.patch; do
  echo "$patch"
  diffs=$($git diff --staged "$patch" | grep --color=none -E "^(\+|\-)" | grep --color=none -Ev "(\-\-\- a|\+\+\+ b|^.index)")
  if [ "x$diffs" == "x" ] ; then
    $git reset HEAD "$patch" >/dev/null
    $git checkout -- "$patch" >/dev/null
  fi
done
echo "  Patches saved for Blueberry-Client to MagmaCube-Patches/"
