#!/bin/sh
# GloBoxClientMain <log> <wToHD:b> <appComm> <dbRep> <nThs> <nReqs> <appIPs>"
# <log>        -> 0 = F and !0 = T (print messages on servers)
# <sync>       -> 0 = F and !0 = T (write & sync to disk)
# <buffered>   -> 0 = F and !0 = T (buffered write and keep history)
# <objects>    -> 0 = F and !0 = T (write threater objects to file)
# <batch>      -> int number of write ops before flush and/or sync output stream
# <broadcast>  -> 0 = F and !0 = T (send reservation to all app servers)
# <dbRep>      -> 0 = no DB data replication and !0 = use DB replicas
# <nThs>       -> int between 1 and 1000 (number of client threads to start)
# <nReqs>      -> int between 1 and 1000000 (number of requests per thread)
# <appIPs>     -> list of IPs (e.g. 10.1.1.1 10.1.1.2 10.1.1.3)
java -Djava.security.policy=policy.all -cp GloBoxPerf.jar:guava-15.0.jar:. psd.globox.web.GloBoxClientMain 0 0 0 0 1 0 1 10 10000 127.0.0.1
