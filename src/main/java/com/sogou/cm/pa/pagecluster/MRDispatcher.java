package com.sogou.cm.pa.pagecluster;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.sogou.web.selector.urllib.URLUtils;


public class MRDispatcher  extends Configured implements Tool {

	/**
	 * @param args
	 */
	public static class TokenizerMapper extends
			Mapper<Object, Text, Text, Text> {
		HashSet<String> domains = new HashSet<String>();
		protected void setup(Context context) throws IOException,
		InterruptedException {
	}
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {

			String[] segs = value.toString().split("\t");
			if (segs.length < 3) {
				return;
			}
			String out_key = segs[0] + "\t" + segs[1];

			context.write(new Text(out_key), new Text(segs[2]));

		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException {

		}
	}
	

	public static class IntSumReducer extends
			Reducer<Text, Text, Text, Text> {

		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			for (Text val: values) {
				context.write(key, val);
			}
		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException {

		}
	}

	
	public int run(String[] args) throws Exception {
		Job job = new Job(this.getConf(), "SampleIndexUrl");

		job.setJarByClass(this.getClass());

		job.setMapperClass(TokenizerMapper.class);
		job.setReducerClass(IntSumReducer.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.waitForCompletion(true);

		return 0;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Tool tool = new MRDispatcher();
		ToolRunner.run(tool, args);
	}

}
