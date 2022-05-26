#!/bin/bash

# Copyright 2022 Arcticons contributers
# This script is distributed under the GNU General Public License.
#
# It was originally written to be used in the Arcticons Icon pack for checking other/requests.txt to output valid F-Droid links.

links=$(grep "https://f-droid.org/*" $1)
progress=0
error=0
check=0

for link in ${links}; do
	check=$((check+1))
	printf "Not found (since last success): $progress\r"

	if curl --output /dev/null --silent --head --fail "$link"; then
		printf "\rFound: $link\n"
		
		progress=0
	else
		progress=$((progress + 1))
		error=$((error + 1))
	fi
done

printf "\n\n"
echo "Total:        $check"
echo "Errors:       $error"
echo "Successes:    $((check - error))"
echo "Success in %: $(echo "1 - $error / $check * 100 + 100" | bc -l)%"
