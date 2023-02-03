#!/bin/bash

for DIR in $(find -name "*.svg")
do
  FILE=${DIR##*/}
  NAME=${FILE%.*}
  VAR=$(grep -ohP '(?<=)stroke:#.*?(?=;)' ${NAME}.svg)
  VAR2=$(grep -ohP '(?<=)fill:#.*?(?=;)' ${NAME}.svg)
  wait
  for item in $VAR
	do
		echo "$item"
		if [ $item != stroke:#ffffff ] && [ $item != stroke:#fff ] && [ $item != stroke:#FFFFFF ];
			then 
				echo ${NAME} >> not_white.txt
				echo ${item} >> not_white.txt
			fi
		
			echo "$VAR"
	done
  for item in $VAR2
	do
		echo "$item"
		if [ $item != fill:#ffffff ] && [ $item != fill:#fff ] && [ $item != fill:#FFFFFF ];
			then 
				echo ${NAME} >> not_white.txt
				echo ${item} >> not_white.txt
			fi
		
			echo "$VAR2"
	done
done



