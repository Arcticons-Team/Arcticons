# Guide to making a custom coloured variant

The guide will use the colour #4080ff as an example

## Getting things set up

- install git bash

- install inkscape

- install android studio

## Creating local repository

- create a folder called "Arcticons_4080ff"

- git bash inside the folder

- `git init`

- `git remote add upstream https://github.com/Donnnno/Arcticons/`

- `git pull upstream main`

  Wait for it to finish...

## Creating a github repository

- create a github account

- create a new github repository "Arcticons-4080ff"

- in git bash, run `git remote add origin <url of your repo>`

- `git push origin main`

Now we're all set to make our own changes and

## Change color

- Open other/rasterwhite.sh

- Replace the #fff in $replace strings with #4080ff

- Open other/raster.sh

- comment out the `sh rasterlight.sh` line to `# sh rasterlight.sh`

- commit changes using `git commit -am "<message describing commit>"`

- push changes to github using `git push origin main`

## Recolor icons

- cut all icons from /icons/white and paste to /other

- open git bash in /other

- run raster.sh

It will run for a couple of hours [Have patience]

- commit changes and push to github

## Build app

- open folder in studio64 (Android studio)

- sync gradle

- click the build drop down menu on top bar

- build apk

## publish on github

- open the release page of your git repository

- create a release and attach the apk with the release
