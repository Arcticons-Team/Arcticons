#!/usr/bin/env bash
# batch_invert_color.sh

white="stroke\s*:\s*\(#fff\|#ffffff\|white\)"
white="fill\s*:\s*\(#fff\|#ffffff\|white\)"

# If you want to replace all stroke colors (won't match rgb()/hsl()/..., but will match #... and black/blue/...):
# ="stroke\s*:\s*#\?[[:alnum:]]\+"

black="stroke:#000" # can replace with any target color
black="fill:#000" # can replace with any target color
for x in *.svg; do
	sed -i "s/${white}/${black}/Ig" "${x}"
done
