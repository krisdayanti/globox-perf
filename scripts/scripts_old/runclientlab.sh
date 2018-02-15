#!/bin/sh
# GloBoxClientMain <logging> <sync> <broadcast> <nThreads> <nRequests> <appServerIPs>"
# <logging>      -> 0 = false and !0 = true
# <sync>         -> 0 = false and !0 = true
# <broadcast>    -> 0 = false and !0 = true (broadcast msgs to all app servers)
# <replicateDB>  -> 0 = false and !0 = true (use a DB replica)
# <bufferedHist> -> 0 = false and !0 = true (use buffered history on writing DB files)
# <writeObject>  -> 0 = false and !0 = true (write theater objects to DB files)
# <nThreads>     -> positive int value (number of client threads to start)
# <nRequests>    -> positive int value (number of operations per thread)
# <appServerIPs> -> list of IPs (e.g., 10.1.1.1 10.1.1.2)
java -cp .:../libs/guava-15.0.jar psd.globox.web.GloBoxClientMain 0 0 0 1 0 1 10 10000 10.101.149.55 10.101.149.58 10.101.149.50
