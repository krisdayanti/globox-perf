sh compile.sh

cp GloBoxPerf.jar globox-perf-v0.3/

for i in 251 252 253 254
do 
    echo "rsync to host 192.168.128.$i ..."
    rsync -avz --delete -e ssh globox-perf-v0.3 globox@192.168.128.$i:/home/kreutz/
done

