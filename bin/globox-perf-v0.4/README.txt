-------------------------------------------------------------------------------
It takes only four simple steps to start the system:
0 - check the IP addresses and parameters of all scripts (*.sh)
1 - start the DB servers (exec "sh run_db_server.sh" on each DB machine)
2 - start the APP servers (exec "sh run_app_server.sh" on each APP machine)
3 - start the clients (exec "sh run_client.sh" on each Client machine)
WARN0: you may need to run "rmiregistry &" on all machines.
WARN1: all DB Servers have to be started together within a max interval of 10s.
WARN2: all App Servers have to be started together within a max interval of 10s.
WARN3: wait at least 10s after starting the App Servers before starting the 
clients.

If everything started just fine, execute again (several times) the system 
clients with different parameters (check inside the script run_client.sh).
You will notice performance variations between one and another of your
executions depending on the chosen input parameters. 
Example: if you set SYNC equal to TRUE, you will notice a significant drop
in the overall system performance.
When using SYNC, try to use higher values for the BATCH_SIZE variable
(e.g. 20, 60, 120).

Client (run_client.sh) outputs:
1) Global statistics (commulative stats of subsequent client executions)
2) Per client exec stats (numbers of the current "client execution" - run_client.sh)
3) Summarized throughput (operations/s) and number of operation failures.

A few short notes about the system:
- it always transfers the whole theater (26*40 bytes) in reservation 
  requests 
  (it could be improve by transfering only free spaces and/or sending 
   only an array of 33 bytes, where each bit represents a seat)
- each thread represents a different unique client generating requests
- theaters are randomly selected for each operation (reservation+purchase)
- clients use round robin when sending requests to App Servers
- DB servers have one file per theater (except for atomic file creation)
- DB files are synchronized to prevent concurrency problems 
  (e.g. problems caused by multiple concurrent writing threads)
- there are per client (per thread) counters on the App and Db Servers
  (consequently, in this case/system the number of clients will affect 
   the system performance due to potential contention issues - e.g. a 
   small number of clients/threads will potentially cause a bit 
   contention on the synchronized counters)
-------------------------------------------------------------------------------

-------------------------------------------------------------------------------
Execute "sh run_test_disk_perf.sh" to test the performance of your system.
It will execute several write tests to see how you OS and disk (sync) perform.
T1 - write + flush + sync (multiple files)
T2 - write + flush + sync (single file)
T3 - write + flush (multiple files)
T4 - write (multiple files)
T5 - create files atomically (one file per purchase)
WARNING: T1 and T2 operations can be really slow (e.g. 50/s). Therefore, take 
care when you choose the numbers (N THREADS and N_THEATERS_PER_THREAD) before 
running the script.
-------------------------------------------------------------------------------

