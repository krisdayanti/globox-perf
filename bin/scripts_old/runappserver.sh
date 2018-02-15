# GloBoxAppMain <AppIPs> xx <DbIPs>
# list of IPs -> zero or more IPs separated by simple space
# AppIPs      -> list of App servers (e.g. 10.0.0.1 10.0.0.2)
# DbIPs       -> list of Db (database) servers (e.g. 10.0.0.3 10.0.0.4)
# xx          -> lists' delimiter (add it on the end of the AppIPs' list)
java -cp .:../libs/guava-15.0.jar psd.globox.app.GloBoxAppMain 127.0.0.1 xx 127.0.0.1
