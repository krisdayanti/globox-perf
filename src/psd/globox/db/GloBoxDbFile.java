package psd.globox.db;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;

import psd.globox.utils.GloBoxDefVals;

public class GloBoxDbFile {

	private long writeToFileCounter;
	private long writeToBufferedCounter;
	private long writeToObjectCounter;
	private File dbFile;
	private FileWriter dbFileWriter;
	private BufferedWriter dbFileBufferedWriter;

	private FileOutputStream out;
	private ObjectOutputStream outObjects;
	
	private StringBuilder theaterSeats;

	public GloBoxDbFile(String tmpDir, int fname) throws IOException {
		out = new FileOutputStream(tmpDir + "/" + fname);
		outObjects = new ObjectOutputStream(out);
		dbFile = new File(tmpDir + "/" + fname + "-buffered");
		dbFileWriter = new FileWriter(dbFile.getAbsoluteFile());
		dbFileBufferedWriter = new BufferedWriter(dbFileWriter);
		theaterSeats = new StringBuilder();
		writeToFileCounter = 0;
		writeToBufferedCounter = 0;
		writeToObjectCounter = 0;
		for (int i = 0; i < (GloBoxDefVals.THEATER_SEATS); i++) {
			theaterSeats.append('0');
		}
	}

	public synchronized String getTheaterSeats() {
		theaterSeats.setCharAt((int) (Math.random() * (theaterSeats.length())), '2');
		return theaterSeats.toString();
	}

	public synchronized void writeToFileBufferedHistory(short seat, short batchSize) throws IOException {
		dbFileBufferedWriter.append(Integer.toString(seat));
		if (writeToBufferedCounter % batchSize == 0)
			dbFileBufferedWriter.flush();
		writeToBufferedCounter++;
		if (writeToBufferedCounter >= (Long.MAX_VALUE-1)) 
			writeToBufferedCounter = 0;
	}

	public synchronized void writeObjectToFile(short seat, short batchSize) throws IOException {
		theaterSeats.setCharAt(seat, '1');
		outObjects.writeObject(theaterSeats);
		if (writeToObjectCounter % batchSize == 0)
			outObjects.flush();
		writeToObjectCounter++;
		if (writeToObjectCounter >= (Long.MAX_VALUE-1)) 
			writeToObjectCounter = 0;
	}
	
	public synchronized void writeToFile(short seat, boolean sync, short batchSize) throws IOException {
		theaterSeats.setCharAt(seat, '1');
		out.write(seat);
		if (writeToFileCounter % batchSize == 0) {
			out.flush();
			if (sync) {
				out.getFD().sync();
			}
		}
		writeToFileCounter++;
		if (writeToFileCounter >= (Long.MAX_VALUE-1)) 
			writeToFileCounter = 0;
	}

	public synchronized void closeFile() throws IOException {
		out.close();
	}
}
