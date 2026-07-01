#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")"

ANDROIDJAR=/usr/lib/android-sdk/platforms/android-23/android.jar
DX=/usr/lib/android-sdk/build-tools/debian/dx
KEYSTORE=override.keystore
KS_PASS="override123"

rm -rf gen obj bin
mkdir -p gen obj bin

echo "== aapt: generate R.java =="
aapt package -f -m -J gen -M AndroidManifest.xml -S res -I "$ANDROIDJAR"

echo "== aapt: package resources =="
aapt package -f -M AndroidManifest.xml -S res -I "$ANDROIDJAR" -F bin/resources.ap_

echo "== javac =="
SRC_FILES=$(find gen src -name "*.java")
javac -source 8 -target 8 -encoding UTF-8 -bootclasspath "$ANDROIDJAR" -classpath "$ANDROIDJAR" -d obj $SRC_FILES

echo "== dx: dex =="
"$DX" --dex --output=bin/classes.dex obj

echo "== assemble unsigned apk =="
cp bin/resources.ap_ bin/app.unsigned.apk
(cd bin && zip -j app.unsigned.apk classes.dex)

echo "== zipalign =="
zipalign -f -p 4 bin/app.unsigned.apk bin/app.aligned.apk

echo "== keystore =="
if [ ! -f "$KEYSTORE" ]; then
  keytool -genkeypair -v -keystore "$KEYSTORE" -alias override \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -storepass "$KS_PASS" -keypass "$KS_PASS" \
    -dname "CN=OVERRIDE, O=OVERRIDE, C=US"
fi

echo "== sign =="
apksigner sign --ks "$KEYSTORE" --ks-pass "pass:$KS_PASS" --key-pass "pass:$KS_PASS" \
  --out bin/OVERRIDE.apk bin/app.aligned.apk

echo "== verify =="
apksigner verify bin/OVERRIDE.apk

echo "Built: bin/OVERRIDE.apk"
