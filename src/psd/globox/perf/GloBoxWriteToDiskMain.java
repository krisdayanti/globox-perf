package psd.globox.perf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import psd.globox.utils.GloBoxDefVals;
import psd.globox.utils.GloBoxProgressBarT;

import com.google.common.base.Stopwatch;

public class GloBoxWriteToDiskMain {

	private static void removeFiles(int nThreads, int nTheatersPerThread) {
		for (int i = 0; i < (nThreads * nTheatersPerThread); i++) {
			for (int j = 0; j < GloBoxDefVals.ROWS; j++) {
				for (int c = 0; c < GloBoxDefVals.COLUMNS; c++) {
					File file = new File("globox/t" + i + "/l" + j + "/" + c);
					if (file.exists())
						file.delete();
				}
				File file = new File("globox/t" + i + "/l" + j);
				if (file.exists())
					file.delete();
				
			}
			File file = new File("globox/t" + i);
			if (file.exists())
				file.delete();
		}
		File file = new File("globox");
		if (file.exists())
			file.delete();
	}
	
	private static void createFiles(int nThreads, int nTheatersPerThread)
	{
	for (int i = 0; i < (nThreads * nTheatersPerThread); i++) {
		File file = new File("globox/t" + i);
		file.mkdirs();
		for (int j = 0; j < GloBoxDefVals.ROWS; j++) {
			file = new File("globox/t" + i + "/l" + j);
			file.mkdirs();
		}
	}
	}

	@SuppressWarnings("deprecation")
    public static void main(String[] args) {
		
		int nThreads = 1;
		int nTheatersPerThread = 1;
		
		if (Integer.parseInt(args[0]) > 1 && Integer.parseInt(args[0]) <= 1000) {
			nThreads = Integer.parseInt(args[0]);
		}
		
		if (Integer.parseInt(args[1]) > 1 && Integer.parseInt(args[1]) <= 1500) {
			nTheatersPerThread = Integer.parseInt(args[1]);
		}		
		
		try {
			System.out.println("-----------------------------------------------------------");
			System.out.println("# of nThreads: " + nThreads);
			System.out.println("# of theaters per thread: " + nTheatersPerThread);
			System.out.println("# of writes (expected) per thread: " + nTheatersPerThread * GloBoxDefVals.ROWS * GloBoxDefVals.COLUMNS);
			System.out.println("# of writes (expected - TOTAL): " + nThreads * nTheatersPerThread * GloBoxDefVals.ROWS * GloBoxDefVals.COLUMNS);
			System.out.println("-----------------------------------------------------------");

			GloBoxProgressBarT progress = new GloBoxProgressBarT("GloBoxWriteFlushSync");
			progress.start();
			
			createFiles(nThreads, nTheatersPerThread);
			GloBoxWriteFlushSync vwfs[] = new GloBoxWriteFlushSync[nThreads];
			Stopwatch stopwatch = Stopwatch.createStarted();
			for (int i = 0; i < nThreads; i++) {
				vwfs[i] = new GloBoxWriteFlushSync(i, nTheatersPerThread);
			}
			for (int i = 0; i < nThreads; i++) {
				vwfs[i].start();
			}
			for (int i = 0; i < nThreads; i++) {
				vwfs[i].join();
			}
			stopwatch.stop();

			progress.stop();
			System.out.println();
			
			long milliseconds = stopwatch.elapsed(java.util.concurrent.TimeUnit.MILLISECONDS);
			float seconds = (float) (milliseconds / 1000.0);
			System.out.println("-----------------------------------------------------------");
			System.out.println("GloBoxWriteFlushSync: " + seconds + " seconds");
			System.out.println("GloBoxWriteFlushSync: " + (float)((nThreads * nTheatersPerThread * GloBoxDefVals.ROWS * GloBoxDefVals.COLUMNS) / seconds) + " writes/s");
			System.out.println("-----------------------------------------------------------");

			progress = new GloBoxProgressBarT("GloBoxWriteFlushSyncSingleFile");
			progress.start();
			
			removeFiles(nThreads, nTheatersPerThread);
			
			createFiles(nThreads, nTheatersPerThread);
			stopwatch = Stopwatch.createStarted();
			GloBoxWriteFlushSyncSingleFile vwfsf[] = new GloBoxWriteFlushSyncSingleFile[nThreads];
			for (int i = 0; i < nThreads; i++) {
				vwfsf[i] = new GloBoxWriteFlushSyncSingleFile(i, nTheatersPerThread);
			}
			for (int i = 0; i < nThreads; i++) {
				vwfsf[i].start();
			}
			for (int i = 0; i < nThreads; i++) {
				vwfsf[i].join();
			}
			stopwatch.stop();
			progress.stop();
			System.out.println();
			
			milliseconds = stopwatch.elapsed(java.util.concurrent.TimeUnit.MILLISECONDS);
			seconds = (float) (milliseconds / 1000.0);
			
			System.out.println("-----------------------------------------------------------");
			System.out.println("GloBoxWriteFlushSyncSingleFile: " + seconds + " seconds");
			System.out.println("GloBoxWriteFlushSyncSingleFile: " + (nThreads * nTheatersPerThread * GloBoxDefVals.ROWS * GloBoxDefVals.COLUMNS) / seconds + " writes/s");
			System.out.println("-----------------------------------------------------------");

			
			progress = new GloBoxProgressBarT("GloBoxWriteFlush");
			progress.start();
			
			removeFiles(nThreads, nTheatersPerThread);
			
			createFiles(nThreads, nTheatersPerThread);
			stopwatch = Stopwatch.createStarted();
			GloBoxWriteFlush vwf[] = new GloBoxWriteFlush[nThreads];
			for (int i = 0; i < nThreads; i++) {
				vwf[i] = new GloBoxWriteFlush(i, nTheatersPerThread);
			}
			for (int i = 0; i < nThreads; i++) {
				vwf[i].start();
			}
			for (int i = 0; i < nThreads; i++) {
				vwf[i].join();
			}
			stopwatch.stop();
			progress.stop();
			System.out.println();
			
			milliseconds = stopwatch.elapsed(java.util.concurrent.TimeUnit.MILLISECONDS);
			seconds = (float) (milliseconds / 1000.0);
			
			System.out.println("-----------------------------------------------------------");
			System.out.println("GloBoxWriteFlush: " + seconds + " seconds");
			System.out.println("GloBoxWriteFlush: " + (nThreads * nTheatersPerThread * GloBoxDefVals.ROWS * GloBoxDefVals.COLUMNS) / seconds + " writes/s");
			System.out.println("-----------------------------------------------------------");

			progress = new GloBoxProgressBarT("GloBoxWrite");
			progress.start();
			
			removeFiles(nThreads, nTheatersPerThread);
			
			createFiles(nThreads, nTheatersPerThread);
			stopwatch = Stopwatch.createStarted();
			GloBoxWrite vw[] = new GloBoxWrite[nThreads];
			for (int i = 0; i < nThreads; i++) {
				vw[i] = new GloBoxWrite(i, nTheatersPerThread);
			}
			for (int i = 0; i < nThreads; i++) {
				vw[i].start();
			}
			for (int i = 0; i < nThreads; i++) {
				vw[i].join();
			}
			stopwatch.stop();
			progress.stop();
			System.out.println();
			
			milliseconds = stopwatch.elapsed(java.util.concurrent.TimeUnit.MILLISECONDS);
			seconds = (float) (milliseconds / 1000.0);
			
			System.out.println("-----------------------------------------------------------");
			System.out.println("GloBoxWrite: " + seconds + " seconds");
			System.out.println("GloBoxWrite: " + (nThreads * nTheatersPerThread * GloBoxDefVals.ROWS * GloBoxDefVals.COLUMNS) / seconds + " writes/s");
			System.out.println("-----------------------------------------------------------");

			progress = new GloBoxProgressBarT("GloBoxAtomicFile");
			progress.start();
			
			removeFiles(nThreads, nTheatersPerThread);
			
			createFiles(nThreads, nTheatersPerThread);
			stopwatch = Stopwatch.createStarted();
			GloBoxAtomicFile vaf[] = new GloBoxAtomicFile[nThreads];
			for (int i = 0; i < nThreads; i++) {
				vaf[i] = new GloBoxAtomicFile(i, nTheatersPerThread);
			}
			for (int i = 0; i < nThreads; i++) {
				vaf[i].start();
			}
			for (int i = 0; i < nThreads; i++) {
				vaf[i].join();
			}
			stopwatch.stop();
			progress.stop();
			System.out.println();
			
			milliseconds = stopwatch.elapsed(java.util.concurrent.TimeUnit.MILLISECONDS);
			seconds = (float) (milliseconds / 1000.0);
			
			System.out.println("-----------------------------------------------------------");
			System.out.println("GloBoxAtomicFile: " + seconds + " seconds");
			System.out.println("GloBoxAtomicFile: " + (nThreads * nTheatersPerThread * GloBoxDefVals.ROWS * GloBoxDefVals.COLUMNS) / seconds + " writes/s");
			System.out.println("-----------------------------------------------------------");

			progress = new GloBoxProgressBarT("GloBoxDeleteFiles");
			progress.start();
			
			removeFiles(nThreads, nTheatersPerThread);
			
			progress.stop();
			System.out.println();
			System.out.println("-----------------------------------------------------------");


		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

class GloBoxWriteFlushSyncSingleFile extends Thread {
	private int theaterIdx;
	private int nTheatersPerThread;

	public GloBoxWriteFlushSyncSingleFile(int theaterIdx, int nTheatersPerThread) {
		this.theaterIdx = theaterIdx * nTheatersPerThread;
		this.nTheatersPerThread = nTheatersPerThread;
		setDaemon(true);
	}

	public void run() {
		byte b[] = new byte[GloBoxDefVals.WRITE_BLOCK_SIZE];
		
		FileOutputStream out = null;
		String fname = new String("globox/file-thread" + Thread.currentThread().getId() + "-" + (10000 + (long) (Math.random() * 10000)));
		try {
			out = new FileOutputStream(fname);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		for (int theater = this.theaterIdx; theater < (this.theaterIdx + nTheatersPerThread); theater++) {
			for (int row = 0; row < GloBoxDefVals.ROWS; row++) {
				for (int column = 0; column < GloBoxDefVals.COLUMNS; column++) {
					try {
						out.write(b);
						out.flush();
						out.getFD().sync();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		try {
	        out.close();
        } catch (IOException e) {
	        e.printStackTrace();
        }
		File file = new File(fname);
		file.delete();
	}
}


class GloBoxWriteFlushSync extends Thread {
	private int theaterIdx;
	private int nTheatersPerThread;

	public GloBoxWriteFlushSync(int theaterIdx, int nTheatersPerThread) {
		this.theaterIdx = theaterIdx * nTheatersPerThread;
		this.nTheatersPerThread = nTheatersPerThread;
		setDaemon(true);
	}

	public void run() {
		byte b[] = new byte[GloBoxDefVals.WRITE_BLOCK_SIZE];
		for (int theater = this.theaterIdx; theater < (this.theaterIdx + nTheatersPerThread); theater++) {
			for (int row = 0; row < GloBoxDefVals.ROWS; row++) {
				for (int column = 0; column < GloBoxDefVals.COLUMNS; column++) {
					try {
						FileOutputStream out = new FileOutputStream("globox/t" + theater + "/l" + row + "/" + column);
						out.write(b);
						out.flush();
						out.getFD().sync();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}

class GloBoxWriteFlush extends Thread {
	private int theaterIdx;
	private int nTheatersPerThread;

	public GloBoxWriteFlush(int theaterIdx, int nTheatersPerThread) {
		this.theaterIdx = theaterIdx * nTheatersPerThread;
		this.nTheatersPerThread = nTheatersPerThread;
		setDaemon(true);
	}

	public void run() {
		byte b[] = new byte[GloBoxDefVals.WRITE_BLOCK_SIZE];
		for (int theater = this.theaterIdx; theater < (this.theaterIdx + nTheatersPerThread); theater++) {
			for (int row = 0; row < GloBoxDefVals.ROWS; row++) {
				for (int column = 0; column < GloBoxDefVals.COLUMNS; column++) {
					try {
						FileOutputStream out = new FileOutputStream("globox/t" + theater + "/l" + row + "/" + column);
						out.write(b);
						out.flush();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}

class GloBoxWrite extends Thread {
	private int theaterIdx;
	private int nTheatersPerThread;

	public GloBoxWrite(int theaterIdx, int nTheatersPerThread) {
		this.theaterIdx = theaterIdx * nTheatersPerThread;
		this.nTheatersPerThread = nTheatersPerThread;
		setDaemon(true);
	}

	public void run() {
		byte b[] = new byte[GloBoxDefVals.WRITE_BLOCK_SIZE];
		for (int theater = this.theaterIdx; theater < (this.theaterIdx + nTheatersPerThread); theater++) {
			for (int row = 0; row < GloBoxDefVals.ROWS; row++) {
				for (int column = 0; column < GloBoxDefVals.COLUMNS; column++) {
					try {
						FileOutputStream out = new FileOutputStream("globox/t" + theater + "/l" + row + "/" + column);
						out.write(b);
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}

class GloBoxAtomicFile extends Thread {
	private int theaterIdx;
	private int nTheatersPerThread;

	public GloBoxAtomicFile(int theaterIdx, int nTheatersPerThread) {
		this.theaterIdx = theaterIdx * nTheatersPerThread;
		this.nTheatersPerThread = nTheatersPerThread;
		setDaemon(true);
	}

	public void run() {
		for (int theater = this.theaterIdx; theater < (this.theaterIdx + nTheatersPerThread); theater++) {
			for (int row = 0; row < GloBoxDefVals.ROWS; row++) {
				for (int column = 0; column < GloBoxDefVals.COLUMNS; column++) {
					try {
						File aFile = new File("globox/t" + theater + "/l" + row + "/" + column);
						aFile.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}


