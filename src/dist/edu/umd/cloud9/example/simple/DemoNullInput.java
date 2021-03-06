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

package edu.umd.cloud9.example.simple;

import java.io.IOException;

import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Reporter;
import org.apache.log4j.Logger;

import edu.umd.cloud9.mapred.NullInputFormat;
import edu.umd.cloud9.mapred.NullMapper;
import edu.umd.cloud9.mapred.NullOutputFormat;

public class DemoNullInput {

	private static final Logger sLogger = Logger.getLogger(DemoNullInput.class);

	private static class MyMapper extends NullMapper {
		public void run(JobConf conf, Reporter reporter) throws IOException {
			sLogger.info("Counting to 10:");
			for (int i = 0; i < 10; i++) {
				sLogger.info(i + 1 + "...");
				try {
					Thread.sleep(10000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected DemoNullInput() {
	}

	/**
	 * Runs the demo.
	 */
	public static void main(String[] args) throws IOException {
		JobConf conf = new JobConf(DemoNullInput.class);
		conf.setJobName("DemoNullInput");

		conf.setNumMapTasks(10);
		conf.setNumReduceTasks(0);

		conf.setInputFormat(NullInputFormat.class);
		conf.setOutputFormat(NullOutputFormat.class);
		conf.setMapperClass(MyMapper.class);

		JobClient.runJob(conf);
	}
}
