# GloBoxAppMain <AppIPs> xx <DbIPs>
# list of IPs -> zero or more IPs separated by simple space
# AppIPs      -> list of App servers (e.g. 10.0.0.1 10.0.0.2)
APP_IPs="192.168.128.252 192.168.128.253 192.168.128.254"
# DbIPs       -> list of Db (database) servers (e.g. 10.0.0.3 10.0.0.4)
DB_IPs="192.168.128.252 192.168.128.253 192.168.128.254"
# xx          -> lists' delimiter (add it on the end of the AppIPs' list)
MY_LISTENING_IP=192.168.128.254
# IP address were the App server is going to listen on
java -Djava.security.policy=policy.all -Djava.rmi.server.hostname=$MY_LISTENING_IP -cp GloBoxPerf.jar psd.globox.app.GloBoxAppMain $APP_IPs xx $DB_IPs
