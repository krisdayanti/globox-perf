# GloBoxDbMain <DbRepIPs>
# DbRepIPs -> list of DB replica's IPs with 0 or more IPs (e.g. 10.0.0.1 10.0.0.2)
# Note: the number of IPs is equal to the number of replicas of this server.
# One replica per DB server should be enough.
DB_REPLICA_IPs="172.31.42.148"
MY_LISTENING_IP=172.31.42.149
java -Djava.net.preferIPv4Stack=true -Djava.security.policy=policy.all -Djava.rmi.server.hostname=$MY_LISTENING_IP -cp GloBoxPerf.jar psd.globox.db.GloBoxDbMain $DB_REPLICA_IPs
