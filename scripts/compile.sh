# remove .class files and database files
rm -rf psd/ db*
# compile everything
javac -target 1.7 -cp ../libs/guava-15.0.jar:../src/:. -d . ../src/psd/globox/*/*.java
sleep 5
jar cvf GloBoxPerf-input.jar psd
sleep 5
java -jar proguard.jar @config-globox.pro
cp GloBoxPerf.jar globox-perf-v0.4/
