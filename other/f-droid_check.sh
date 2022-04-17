#!/bin/bash
links=$(grep "https://f-droid.org/*" $1)
repeat=0

for link in ${links}; do
	printf "Not found (since last success): $repeat\r"

	if curl --output /dev/null --silent --head --fail "$link"; then
		printf "\rFound: $link\n"
		repeat=0
	else
		repeat=$((repeat+1))
	fi
done
