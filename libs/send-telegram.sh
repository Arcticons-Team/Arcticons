if [ "$TRAVIS_PULL_REQUEST" = true ]; then
  exit 0
fi

LAST_COMMIT_LOG=$(git log -1 --pretty=format:'%s %b')

if [ "$(echo "$LAST_COMMIT_LOG" | grep -c '\[skip apk\]')" -gt 0 ]; then
  echo 'Found `[skip apk]` tag. Skipping APK publishing.'
  exit 0
fi

cd $TRAVIS_BUILD_DIR/app/build/outputs/apk/release/

apk_name='CandyBar-'
if [ "$TRAVIS_TAG" ]; then
  apk_name+="$TRAVIS_TAG"
else
  apk_name+=$(date +%d%m%Y-%H%M)
fi
apk_name+='.apk'

mv 'app-release.apk' $apk_name

curl -v \
  -F document=@"$apk_name" \
  -F chat_id=-1001381276297 \
  -F disable_notification=true \
  https://api.telegram.org/bot$BOT_TOKEN/sendDocument
