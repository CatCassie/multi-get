import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.BufferedInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class partialDownloader {

        private static final int BUFFER_SIZE = 4196304;

        private static final String manual = "\nThank you for using partialDownloader\n"
			+ "\n"
			+ "Usage:\n"
			+ "  java -jar partialDownloader.jar [OPTIONS]\n"
			+ "\n"
			+ "Options:\n"
			+ "  --url         <your download url>, url is required\n"
			+ "  -o --output   <name of your outoutFile>, it will be named as default if not specified\n"
			+ "  --parallel    if this flag is not specified, default is serial \n"
			+ "\n";

	public static void main(String[] args) throws IOException {
                String downloadURL = null;
                String outputFile = "default";
                boolean isParallel = false;
                // If no command line argument is received, print the usage manual
                if (args.length == 0){
                  	System.out.println(manual);
                        System.exit(0);
                }else{
                	for(int i=0;i<args.length;i++){  
                                // grab info from user: downloadURL, outputFile name if specified, if parallel download
                     		if (args[i].equals("--url")){
                        		downloadURL=args[i+1];
                        	}else if(args[i].equals("--output") || args[i].equals("-o") ){
					outputFile=args[i+1];			
                        	}else if(args[i].equals("--parallel")){
					isParallel=true;
				}
                	}
                        if(isParallel){
                                // use ExcutorService to run download tasks in parallel 
                        	ExecutorService pool = Executors.newFixedThreadPool(4);
				pool.execute(new downloadTask("0-1048575",0,downloadURL,outputFile));
                        	pool.execute(new downloadTask("1048576-2097151",1048576,downloadURL,outputFile));
                        	pool.execute(new downloadTask("2097152-3145727",2097152,downloadURL,outputFile));
                        	pool.execute(new downloadTask("3145728-4194303",3145728,downloadURL,outputFile));
                        	pool.shutdown();
                        	System.out.println("Parallel GET DONE");
                        }else{
                                // send Get requests one by one if parallel download is not required
				serialDownload("0-1048575",0,downloadURL,outputFile);
                        	serialDownload("1048576-2097151",1048576,downloadURL,outputFile);
                        	serialDownload("2097152-3145727",2097152,downloadURL,outputFile);
                        	serialDownload("3145728-4194303",3145728,downloadURL,outputFile);
                        	System.out.println("Serial GET DONE");
                        }
                }
	}

	private static void serialDownload(String byteRange, long startByte, String downloadURL, String outputFile) throws IOException {
		URL url = new URL(downloadURL);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		// send GET request to the downloadURL by specifying the bytes range
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Range", "bytes="+byteRange);
		int responseCode = conn.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);
                // append the downloaded chunks to a file 
                BufferedInputStream  in = new BufferedInputStream(conn.getInputStream());             
                RandomAccessFile raf = new RandomAccessFile(outputFile, "rw");
                // write to random access file at a given position
                raf.seek(startByte);
                byte data[] = new byte[BUFFER_SIZE];
	        int bytesRead;
		while((bytesRead = in.read(data,0,BUFFER_SIZE)) != -1)
		{
			// write to random access file
			raf.write(data,0,bytesRead);
		}
	}
        
	private static class downloadTask implements Runnable {
                
        	private String byteRange;
                private long startByte;
                private String downloadURL;
                private String outputFile;
                public downloadTask(String byteRange, long startByte, String downloadURL, String outputFile){
			this.byteRange = byteRange;
                        this.startByte = startByte;
                        this.downloadURL = downloadURL;
                        this.outputFile = outputFile;
		}
		
                @Override
                public void run() {
                        // overriden function does not support throw IOException, so used try-catch
                        try{
				parallelDownload(byteRange, startByte, downloadURL, outputFile);
			}catch (IOException ex){
                        	System.out.println("IO Exception while downloading");
			}
		}
                private void parallelDownload(String byteRange, long startByte, String downloadURL, String outputFile) throws IOException{
			// this function is identical to serialDownload in terms of content
                        URL url = new URL(downloadURL);
                	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        // send GET request to the downloadURL by specifying the bytes range
                	conn.setRequestMethod("GET");
                	conn.setRequestProperty("Range", "bytes="+byteRange);
                	int responseCode = conn.getResponseCode();
                	System.out.println("GET Response Code :: " + responseCode);
                	// append the downloaded chunks to a file
                	BufferedInputStream  in = new BufferedInputStream(conn.getInputStream());
                	RandomAccessFile raf = new RandomAccessFile(outputFile, "rw");
                        // write to random access file at a given position
                	raf.seek(startByte);
                	byte data[] = new byte[BUFFER_SIZE];
                	int bytesRead;
                	while((bytesRead = in.read(data,0,BUFFER_SIZE)) != -1)
                	{
                        	// write to random access file
                        	raf.write(data,0,bytesRead);
                	}

		}
                
	}  

}
