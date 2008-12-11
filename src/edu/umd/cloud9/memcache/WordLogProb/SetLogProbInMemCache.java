package edu.umd.cloud9.memcache.WordLogProb;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapFileOutputFormat;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.lib.IdentityReducer;

public class SetLogProbInMemCache {

	
	public static class MyMapper extends MapReduceBase implements
	Mapper<Text, FloatWritable, Text, FloatWritable > {

		Float keyTemp = new Float(0);
		Object obj ;
		MemcachedClient m;

		public void configure(JobConf conf) {
			try {
				m = new MemcachedClient(AddrUtil.getAddresses(conf.get("ADDRESSES")));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


		public void map(Text text, FloatWritable value, OutputCollector<Text, FloatWritable> output,
				Reporter reporter) throws IOException {


			// writing key value pair to cache	
			
			obj = ((Float)(value.get())).toString();
			
			m.set(text.toString(),60*60*20,obj);
			// to fulfill the mapper configuration
			output.collect(text, value);
		}	
	}



	public SetLogProbInMemCache() {
	
	}
	
	
	private static String getListOfIpAddresses(String inputFile){
		String ipAddresses="";
		// default port
		String port="11211";
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
			String line;
			while((line = in.readLine())!=null){
				if(!line.equals("")){
					String temp = line+":"+port;
					if(ipAddresses.equals(""))
						ipAddresses = temp;
					else
						ipAddresses += " " + temp;
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return ipAddresses;
	}
	/**
	 * Runs the demo.
	 */
	public static void main(String[] args) throws IOException {

		/* 
		 * First argument - path of file on local file system on master node containing list of memcache servers
		 * Second argument - path of file on dfs on master node to be converted into sequence file and put in memcache 
		 */

		if(args.length != 2){
			System.out.println(" usage : [path of ip address file] [path of sequence file on dfs ]");
			System.exit(1);
		}

		String pathOfIpAddressFile = args[0];
		String inputPathSeqFile = args[1];
		
		String ipAddress = getListOfIpAddresses(pathOfIpAddressFile);
		if(ipAddress.equals("")){
			System.out.println("List of Memcache servers IP Addresses not available");
			System.exit(1);
		}else{
			System.out.println("List of IP addresses : "+ ipAddress);
		}
		
		
		String extraPath = "/shared/extraInfo";
		MemcachedClient myMCC;
		myMCC = new MemcachedClient(AddrUtil.getAddresses(ipAddress));
		myMCC.flush();
		int mapTasks = 1;
		int reduceTasks = 0;

		JobConf conf = new JobConf(SetLogProbInMemCache.class);
		conf.setJobName("SetInMemCache");

		conf.set("ADDRESSES", ipAddress);
		conf.setNumMapTasks(mapTasks);
		conf.setNumReduceTasks(reduceTasks);

		FileInputFormat.setInputPaths(conf, new Path(inputPathSeqFile));
		conf.setInputFormat(SequenceFileInputFormat.class);
		FileOutputFormat.setOutputPath(conf, new Path(extraPath));
		conf.setMapperClass(MyMapper.class);
		conf.setReducerClass(IdentityReducer.class);
		Path outputDir = new Path(extraPath);
		FileSystem.get(conf).delete(outputDir, true);
		
		
		System.out.println("getting: " + conf.get("ADDRESSES"));
		JobClient.runJob(conf);
	}
}
