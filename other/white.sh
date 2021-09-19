#!/usr/bin/env bash
# batch_invert_color.sh

white="stroke\s*:\s*\(#fff\|#ffffff\|white\)"

replace="stroke:#fff"
for x in *.svg; do
	sed -i "s/${white}/${replace}/Ig" "${x}"
done

white="fill\s*:\s*\(#fff\|#ffffff\|white\)"

replace="fill:#fff"
for x in *.svg; do
	sed -i "s/${white}/${replace}/Ig" "${x}"
done