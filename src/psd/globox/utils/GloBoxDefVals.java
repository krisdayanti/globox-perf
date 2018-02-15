package psd.globox.utils;

public class GloBoxDefVals {
		public static short RANGE = 50;
		public static short ROWS = 26;
		public static short COLUMNS = 40;
		public static short N_THEATERS = 500;
		public static int THEATER_SEATS = ROWS*COLUMNS;
		
		public static long COUNTERS_GLOBAL_VALUE = 0;
		
		public static long MAX_CLIENT_ID = 1000000;
		
		public static int APP_DEFAULT_PORT = 3232;
		public static int DB_DEFAULT_PORT = 3231;
		
		public static int MAX_RANDOM_NUMBER = 10000;
		public static long MAX_MSG_SEQUENCE_ID = Long.MAX_VALUE;
		
		public static int STARTUP_WAITING_IN_MS = 10000;
		
		public static int WRITE_BLOCK_SIZE = 4096;
		
		public static final char WRITE_TO_DISK_FLUSH = 1;
		public static final char WRITE_TO_DISK_FLUSH_SYNC = 2;
		public static final char WRITE_TO_DISK_BUFFERED = 3;
		public static final char WRITE_TO_DISK_OBJECTS = 4;
		
		public static final char APP_SERVER_COMM_NONE = 0;
		public static final char APP_SERVER_COMM_BROADCAST = 1;
		public static final char APP_SERVER_COMM_MAJORITY = 2;
}
