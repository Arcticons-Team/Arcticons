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

sizes="32 48 64 96 128 256 512"

rm -r ./arcticons/*/** 2> /dev/null || true

for size in $sizes; do
  factor=$(awk "BEGIN { print 48 / $size }")
  dest_root="./arcticons/${size}x${size}"

  for line in $(cat mapping.txt); do
    src=$(echo "$line" | cut -d, -f1)
    dest=$(echo "$line" | cut -d, -f2)

    mkdir -p "$dest_root/$(dirname "$dest")"
    cp -v "../icons/$style/$src.svg" "$dest_root/$dest.svg"

    grep -v 'stroke-width' "$dest_root/$dest.svg" > /dev/null && sed -i 's/\(stroke:[^;]\+\)/\1;stroke-width:1px/g' "$dest_root/$dest.svg"
    awk -i inplace -F 'stroke-width:|px' "{ print \$1 \"stroke-width:\" (\$2 * $line_weight * $factor) \"px\" \$3; }" "$dest_root/$dest.svg"
  done

  rm "${dest_root}@2" 2> /dev/null || true
  ln -s "${size}x${size}" "${dest_root}@2"
  rm "${dest_root}@3" 2> /dev/null || true
  ln -s "${size}x${size}" "${dest_root}@3"
done

tar czf arcticons.tar.gz arcticons