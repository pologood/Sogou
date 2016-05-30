package com.sogou.cm.pa.pagecluster;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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


public class MrGetCrawlUrl  extends Configured implements Tool {

	/**
	 * @param args
	 */
	public static class TokenizerMapper extends
			Mapper<Object, Text, Text, Text> {
		int cnt = 0;
		int sample_num = 1000;
		int max_int = 0x7fffffff;
		HashMap<String, ArrayList<String>> data = new HashMap<String, ArrayList<String>>();
		HashMap<String, Integer> domain_cnt = new HashMap<String, Integer>();
		Random random = new Random();
		protected void setup(Context context) throws IOException,
		InterruptedException {
	}
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			
			String val = value.toString();
			if (val.indexOf("SUCCESS") < 0) {
				return;
			}
			String[] segs = value.toString().split(" ");
			String url = segs[4];
			if (!url.startsWith("http:")) {
				return;
			}
			String site = URLUtils.getMainDomain(url);
			if (site == null) {
				return;
			}
			context.write(new Text(url), new Text(""));

		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException {
		}
	}

	public static class IntSumReducer extends
			Reducer<Text, Text, Text, Text> {
		int cnt = 0;
		int sample_num = 1000;
		int max_int = 0x7fffffff;
		ArrayList<String> urls_arr = new ArrayList<String>();
		Random random = new Random();
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			context.write(key, null);

		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException {
		}
	}

	
	public int run(String[] args) throws Exception {
		Job job = new Job(this.getConf(), "GetCrawlUrl");

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
		Tool tool = new MrGetCrawlUrl();
		ToolRunner.run(tool, args);
	}

}
