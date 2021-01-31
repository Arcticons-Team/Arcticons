#!/bin/bash

APP="../app/src/main/res/xml/appfilter.xml"

# remove whitespace at beginning of line
sed -r 's:^[ \t]*::g' ${APP} > onefilter.xml
# remove whitespace at end of line
sed -ri 's:[ \t]*$::g' onefilter.xml
# remove extra spaces in items
sed -ri 's: +: :g' onefilter.xml
# replace spaces with bar character
sed -ri 's: :|:g' onefilter.xml

# move each app to one line for sorting
while read -r line
do
    TYPE=$(echo ${line} | cut -c 1-4)
    case ${TYPE} in
      "<!--")
        printf "\n$line" >> twofilter.xml
        ;;
      "<ite")
        printf " $line" >> twofilter.xml
        ;;
      "<sca")
        printf " $line" >> twofilter.xml
        ;;
      "")
        # do nothing
        ;;
    esac
done < onefilter.xml

# sort by label
sort -k 1 -n twofilter.xml > onefilter.xml

# separate each item group
sed -ri 's: ([^ ]*)$:&\n:g' onefilter.xml

# replace spaces with newlines
tr ' ' '\n' < onefilter.xml > twofilter.xml

# add indentation
sed -ri 's:.+:    &:g' twofilter.xml
# replace bar character with space in items
sed -ri 's:\|: :g' twofilter.xml
# add space at the end
sed -ri 's:\s?\/>: \/>:g' twofilter.xml

echo "<resources>" > ${APP}
cat twofilter.xml >> ${APP}
echo "</resources>" >> ${APP}
rm onefilter.xml twofilter.xml
