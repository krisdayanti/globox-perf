# GloBoxAppMain <AppIPs> xx <DbIPs>
# list of IPs -> zero or more IPs separated by simple space
# AppIPs      -> list of App servers (e.g. 10.0.0.1 10.0.0.2)
APP_IPs="127.0.0.1"
# DbIPs       -> list of Db (database) servers (e.g. 10.0.0.3 10.0.0.4)
DB_IPs="127.0.0.1"
# xx          -> lists' delimiter (add it on the end of the AppIPs' list)
MY_LISTENING_IP=127.0.0.1
# IP address were the App server is going to listen on
java -Djava.security.policy=policy.all -Djava.rmi.server.hostname=$MY_LISTENING_IP -cp GloBoxPerf.jar psd.globox.app.GloBoxAppMain $APP_IPs xx $DB_IPs
