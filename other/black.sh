#!/usr/bin/env bash
# batch_invert_color.sh

white="stroke\s*:\s*\(#fff\|#ffffff\|white\)"

black="stroke:#000"
for x in *.svg; do
	sed -i "s/${white}/${black}/Ig" "${x}"
done

white="fill\s*:\s*\(#fff\|#ffffff\|white\)"

black="fill:#000"
for x in *.svg; do
	sed -i "s/${white}/${black}/Ig" "${x}"
done