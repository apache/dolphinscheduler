#!/bin/bash
script_dir=`dirname $0`
cd $script_dir/../../../target
package_file=`ls apache-dolphinscheduler-*-bin.tar.gz`
echo $package_file
decompress_dirname="${package_file%.tar.gz}"
rm -rf $decompress_dirname
#Decompress package file
tar -xf $package_file
cd $decompress_dirname

SHARED_LIB_DIR="libs"
# create share lib directory
mkdir -p $SHARED_LIB_DIR

echo 'iterate through the lib directory for all subprojects'
for module in api-server master-server worker-server alert-server tools; do
  MODULE_LIB_DIR="$module/libs"
  echo "handling $MODULE_LIB_DIR"

  if [ -d "$MODULE_LIB_DIR" ]; then
    cd $MODULE_LIB_DIR

    for jar in `ls *.jar`; do
      # Move jar file to share lib directory
      mv $jar ../../$SHARED_LIB_DIR/

      # Create a symbolic link in the subproject's lib directory
      ln -s ../../$SHARED_LIB_DIR/$jar .
    done

    cd - > /dev/null
  fi
done
#Recompress the package
cd ..
tar -zcf $package_file $decompress_dirname
rm -rf $decompress_dirname