/*
 * Cloud9: A MapReduce Library for Hadoop
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.umd.cloud9.example.bfs;

import java.io.IOException;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

/**
 * <p>
 * Tool for extracting nodes that are a particular distance from the source
 * node.
 * </p>
 *
 * @author Jimmy Lin
 *
 */
public class FindNodeAtDistance extends Configured implements Tool {

	private static final Logger LOG = Logger.getLogger(FindNodeAtDistance.class);

	private static class MyMapper extends Mapper<IntWritable, BFSNode, IntWritable, BFSNode> {

		private static int distance;

		@Override
		public void setup(Context context) {
			distance = context.getConfiguration().getInt("distance", 0);
		}

		@Override
		public void map(IntWritable nid, BFSNode node, Context context) throws IOException,
				InterruptedException {
			if (node.getDistance() == distance) {
				context.write(nid, node);
			}
		}
	}

	public FindNodeAtDistance() {
	}

	private static int printUsage() {
		System.out.println("usage: [inputDir] [outputDir] [distance]");
		ToolRunner.printGenericCommandUsage(System.out);
		return -1;
	}

	/**
	 * Runs this tool.
	 */
	public int run(String[] args) throws Exception {
		if (args.length != 3) {
			printUsage();
			return -1;
		}

		String inputPath = args[0];
		String outputPath = args[1];
		int distance = Integer.parseInt(args[2]);

		LOG.info("Tool name: FindNodeAtDistances");
		LOG.info(" - inputDir: " + inputPath);
		LOG.info(" - outputDir: " + outputPath);
		LOG.info(" - distance: " + distance);

		Job job = new Job(getConf(), "FindNodeAtDistance");
		job.setJarByClass(FindNodeAtDistance.class);

		job.setNumReduceTasks(0);

		job.getConfiguration().setInt("distance", distance);
		job.getConfiguration().setInt("mapred.min.split.size", 1024 * 1024 * 1024);

		FileInputFormat.addInputPath(job, new Path(inputPath));
		FileOutputFormat.setOutputPath(job, new Path(outputPath));

		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(BFSNode.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(BFSNode.class);

		job.setMapperClass(MyMapper.class);

		// Delete the output directory if it exists already.
		FileSystem.get(job.getConfiguration()).delete(new Path(outputPath), true);

		job.waitForCompletion(true);

		return 0;
	}

	/**
	 * Dispatches command-line arguments to the tool via the
	 * <code>ToolRunner</code>.
	 */
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new FindNodeAtDistance(), args);
		System.exit(res);
	}
}
