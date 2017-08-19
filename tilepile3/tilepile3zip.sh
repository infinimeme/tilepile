
rm tilepile3.zip
rm -rf tilepile3

mkdir tilepile3

mkdir tilepile3/tilepileCommon
mkdir tilepile3/tilepileAdmin
mkdir tilepile3/tilepileMainStation
mkdir tilepile3/tilepileSubStation

cp ../tilepileCommon/target/*.jar tilepile3/tilepileCommon
cp ../tilepileAdmin/target/*.jar tilepile3/tilepileAdmin
cp ../tilepileMainStation/target/*.jar tilepile3/tilepileMainStation
cp ../tilepileSubStation/target/*.jar tilepile3/tilepileSubStation

cp -r ../tilepile*n/target/*/*.app tilepile3

zip -r tilepile3.zip tilepile3

