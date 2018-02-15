#!/bin/sh
# GloBoxWriteToDiskMain <nThreads> <nTheatersPerThread> 
# <nThreads>           -> int value between 1 and 1000
N_THREADS=3
# <nTheatersPerThread> -> int value between 1 and 1500
N_THEATERS_PER_THREAD=5
# Note 1: each theater has 26 rows and 40 columns.
# Note 2: the number of writes = N_THREADS * N_THEATERS_PER_THREAD * 26 * 40
# WARNING: a high number of threads or theaters can take a long time to execute!
# Some systems will be able to do only 50 sync writes per second, for instance.
# On Mac OS systems you can achieve 2000+ sync writes/s. 
# However, this is the effect of disk controller peaggyback.
java -cp GloBoxPerf.jar:guava-15.0.jar:. psd.globox.perf.GloBoxWriteToDiskMain $N_THREADS $N_THEATERS_PER_THREAD
