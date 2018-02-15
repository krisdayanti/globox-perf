#!/bin/sh
# GloBoxClientMain <log> <sync> <buffered> <objects> <batchSize> <broadcast> <dbRep> <nThreads> <nRequests> <rateLimit> <appIPs>
# <log>        -> 0 = F and !0 = T (print messages on servers)
LOG=0
# <sync>       -> 0 = F and !0 = T (write() & flush() & sync() to disk)
SYNC=0
#
# WARN: SYNC to disk can significantly reduce the number of operations/s.
#       When SYNC=1, try to use fewer threads and/or requests per thread.
#
# <buffered>   -> 0 = F and !0 = T (buffered write with history track)
BUFFERED=0
# <objects>    -> 0 = F and !0 = T (write threater objects to file)
OBJECTS=0
#
# NOTE: the precedence order is OBJECTS, BUFFERED, SYNC, i.e. sync will only work
#       when OBJECTS and BUFFERED are not active (in normal File.write() ops).
#
# <batchSize>  -> int number of write ops before flush and/or sync output stream
BATCH_SIZE=1
# <broadcast>  -> 0 = F and !0 = T (send reservation to all app servers)
APP_BROADCAST=0
# <dbRep>      -> 0 = no DB data replication and !0 = use DB replicas
DB_REPLICA=1
# <nThreads>   -> int between 1 and 1000 (number of client threads to start)
N_CLIENT_THREADS=200
# <nRequests>  -> int between 1 and 1000000 (number of requests per thread)
N_REQS_PER_THREAD=300
# <rateLimit>  -> max number of operations per second per thread (int less than 1 is equal to unlimited)
MAX_REQS_PER_SEC=0 
# <appIPs>     -> list of IPs (e.g. 10.1.1.1 10.1.1.2 10.1.1.3)
APP_SERVER_IPs="127.0.0.1"
java -Djava.net.preferIPv4Stack=true -Djava.security.policy=policy.all -cp GloBoxPerf.jar:guava-15.0.jar:. psd.globox.web.GloBoxClientMain $LOG $SYNC $BUFFERED $OBJECTS $BATCH_SIZE $APP_BROADCAST $DB_REPLICA $N_CLIENT_THREADS $N_REQS_PER_THREAD $MAX_REQS_PER_SEC $APP_SERVER_IPs
