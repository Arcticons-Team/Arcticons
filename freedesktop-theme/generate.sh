#!/usr/bin/env bash
#
# Generates the arcticons freedesktop icon theme.
#
# Usage: ./generate.sh [STYLE [LINE_WEIGHT]]
#
# STYLE:
#   Either "white" or "black"
#
# LINE_WEIGHT:
#   The relative thickness of the generated icon lines. Can be any positive
#   number (recommended range: about 0.75 to 1.5). A value of 1 means line
#   weight for the 48x48 is identical to the original line weight.
#
# This will create an `arcticons` directory in the current working dir
# containing the icon theme and an archive of the directory as
# `arcticons.tar.gz`.
#
# WARNING! If the directory already exists, its original contents will be wiped.
#
# The generated icons can be defined via the `mapping.txt` file. Each line
# represents a single icon and has the following format:
#
# <source_icon_name>,<destination_icon_path>
#
# ... where <source_icon_name> is the name of the icon in the original Arcticons
# for Android icon set and <destination_icon_path> is the path (name and,
# optionally, preceding directories) of the generated icon in the freedesktop
# icon theme.

set -euo pipefail

style=${1:-white}
line_weight=${2:-1.0}

rm -r ./arcticons/ 2> /dev/null || true

dest_root="./arcticons/scalable"

for line in $(cat mapping.txt); do
  echo $line
  src=$(echo "$line" | cut -d, -f1)
  dests=( `echo "$line" | cut -d, -f2 | tr ':' ' '` )
  dest=${dests[0]}

  mkdir -p "$dest_root/$(dirname "$dest")"
  cp -v "../icons/$style/$src.svg" "$dest_root/$dest.svg" || continue

  grep -v 'stroke-width' "$dest_root/$dest.svg" > /dev/null && sed -i 's/\(stroke:[^;]\+\)/\1;stroke-width:1px/g' "$dest_root/$dest.svg"
  awk -i inplace -F 'stroke-width:|px' "{ print \$1 \"stroke-width:\" (\$2 * $line_weight) \"px\" \$3; }" "$dest_root/$dest.svg"

  if [ ${#dests[@]} -gt 1 ]; then
    for i in $(seq 1 $((${#dests[@]}-1))); do
      mkdir -p "$dest_root/$(dirname "${dests[$i]}")"
      ln -vs "../${dests[0]}.svg" "$dest_root/${dests[$i]}.svg"
    done
  fi
done

if type inkscape; then
  for line in $(cat mapping.txt); do
    echo $line
    src=$(echo "$line" | cut -d, -f1)
    dests=( `echo "$line" | cut -d, -f2 | tr ':' ' '` )
    dest=${dests[0]}
    dest_root="./arcticons/symbolic"
    src_root="./arcticons/scalable"

    mkdir -p "$dest_root/$(dirname "$dest")"
    inkscape --actions="select-all;object-stroke-to-path" --export-filename="$dest_root/$dest-symbolic.svg" "$src_root/$dest.svg" || true
    rm $src_root/$dest*.0.svg || true

    if [ ${#dests[@]} -gt 1 ]; then
      for i in $(seq 1 $((${#dests[@]}-1))); do
        mkdir -p "$dest_root/$(dirname "${dests[$i]}")"
        ln -vs "../${dests[0]}-symbolic.svg" "$dest_root/${dests[$i]}-symbolic.svg"
      done
    fi
  done
else echo "Inkscape not found, skipping creating symbolic icons"
fi

folders=(8x8 16x16 16x16@2x 18x18 18x18@2x 22x22 22x22@2x 24x24 24x24@2x 32x32 32x32@2x 42x42 48x48 48x48@2x 64x64 64x64@2x 84x84 96x96 128x128)
for folder in "${folders[@]}"; do
  ln -sv scalable arcticons/$folder
done

cp index.theme arcticons/

tar czf arcticons.tar.gz arcticons
