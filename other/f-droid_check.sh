#!/bin/bash
links=$(grep "https://f-droid.org/*" $1)

for link in ${links}; do
	if curl --output /dev/null --silent --head --fail "$link"; then
		echo "$link"
	fi
done
