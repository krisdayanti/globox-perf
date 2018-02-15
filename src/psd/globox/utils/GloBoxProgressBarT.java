package psd.globox.utils;

public class GloBoxProgressBarT extends Thread {
		private String message;
		
		public GloBoxProgressBarT(String message) {
			this.message = message;
			setDaemon(true);
		}

		public void run() {
			GloBoxProgressBar bar = new GloBoxProgressBar(this.message);

	        System.out.print(message);

	        bar.update(0, 2000);
	        for(int i = 0; i < 2000; i++) {
	        	try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	            bar.update(i, 2000);
	        }
	}

	class GloBoxProgressBar {
		private String message;
	    private StringBuilder progress;

	    public GloBoxProgressBar(String message) {
	    	this.message = message;
	        init();
	    }

	    public void update(int done, int total) {
	        char[] workchars = {'|', '/', '-', '\\'};
	        String format = "\r#%s %c [%s] ";

	        int percent = (++done * 100) / total;
	        int extrachars = (percent / 2) - this.progress.length();

	        while (extrachars-- > 0) {
	            progress.append('#');
	        }

	        System.out.printf(format, progress, workchars[done % workchars.length], message);

	        if (done == total) {
	            System.out.flush();
	            System.out.println();
	            init();
	        }
	    }

	    private void init() {
	        this.progress = new StringBuilder(120);
	    }
	}
}
