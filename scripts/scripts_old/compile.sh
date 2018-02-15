# remove .class files and database files
rm -rf psd/ db*
# compile everything
javac -cp ../libs/guava-15.0.jar:../src/:. -d . ../src/psd/globox/*/*Main.java
sleep 5
jar cvf GloBoxPerf-input.jar psd
sleep 5
java -jar proguard.jar @config-globox.pro
