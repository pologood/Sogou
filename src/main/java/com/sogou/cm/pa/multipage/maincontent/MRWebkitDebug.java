package com.sogou.cm.pa.multipage.maincontent;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.ByteOrderMarkDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.UnicodeDetector;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.LazyOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sogou.cm.pa.maincontent.Element;
import com.sogou.cm.pa.maincontent.RuleBasedMainContentExtractor;
import com.sogou.web.selector.offsum.OriginPageInputFormatV3;
import com.sogou.web.selector.offsum.OriginPageOutputFormat;
import com.sogou.web.selector.offsum.OriginPageWritable;
import com.sogou.web.selector.offsum.OriginPageWritable.Attribute;
import com.sogou.web.selector.urllib.URLUtils;
import com.sogou.web.selector.urllib.UrlInfo;

public class MRWebkitDebug extends Configured implements Tool {
	
	public static String getMemStat() {
		 SimpleDateFormat STANDARD_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	 
	  Runtime runtime = Runtime.getRuntime();
	Date date = new Date(System.currentTimeMillis());
				StringBuilder sb = new StringBuilder(1024);
				sb.append("[").append(STANDARD_FORMAT.format(date)).append("]");
				sb.append("[").append(runtime.totalMemory() >> 20).append(":").append(runtime.freeMemory() >> 20)
						.append("] ");
				sb.append("");
				return (sb.toString());
	}

	private static class ScanMapper extends Mapper<BytesWritable, OriginPageWritable, Text, Text> {
		byte[] output = new byte[4096 * 1024 + 1];
		Inflater decompresser = new Inflater();
		CodepageDetectorProxy	codepageDetectorProxy	= CodepageDetectorProxy.getInstance();
		RuleBasedMainContentExtractor main_content = new RuleBasedMainContentExtractor();
	//	HashMap<Long, String> url2cluster = new HashMap<Long, String>();
		MessageDigest md;
		byte[] dig_id;
		
		XpathNodeCounter		htmlContentHandler		= new XpathNodeCounter();
		Parser					parser					= new Parser();
		Random ran = new Random();
		
		
		protected void setup(Context context) throws IOException, InterruptedException {
			System.out.println(((FileSplit) context.getInputSplit()).getPath());
			parser.setContentHandler(htmlContentHandler);
			try {
				md = MessageDigest.getInstance("md5");
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dig_id = new byte[16];
			/*
			String fileName = "/root/CustomResult/web_pdm/sunjian/page_cluster/url_clusterid.txt";
					FileSystem fileSys = FileSystem.get(context.getConfiguration());
					Path path = new Path(fileName);
					FSDataInputStream input = fileSys.open(path);
					InputStreamReader isr = new InputStreamReader(input);
					BufferedReader reader = new BufferedReader(isr);

		//	BufferedReader reader2 = new BufferedReader(new FileReader(new File("url_cluster.txt")));
			String line;
			int cnt = 0;
			while ((line = reader.readLine()) != null) {
				++cnt;
				if (cnt%100000 == 0) {
					System.out.println("process: " + cnt);
					context.progress();
				}
				String[] segs = line.split("\t");
			//	System.out.println(line + '\t' + segs.length);
				if (segs.length != 2) {
					continue;
				}
				md.reset();
				String text = segs[0];
				md.update(text.getBytes());
				Long docid= new Long(0);
				try {
					md.digest(dig_id, 0, 16);
					for (int j = 0; j < dig_id.length; ++j) {
						docid=docid<<8;
						docid += dig_id[j]&0xff;
					}
					url2cluster.put(docid, segs[1]);
				} catch (DigestException e) {
				//	docid = Long.valueOf(html_page.xpath2info.get(xpath).num);
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			reader.close();
			*/
		}

		
		protected void map(BytesWritable key, OriginPageWritable value, Context context) throws IOException, InterruptedException {
			InputSplit split = context.getInputSplit();
			FileSplit fs = (FileSplit) split;
			String fname = fs.getPath().getParent().getName();
			
			boolean is_webkit = false;
			if (fname.indexOf("webkit") >= 0) {
				is_webkit = true;
			}
			if (key == null)
				return;

			if (value == null) {
				return;
			}
			
			if (value.url.toString().length() > 256) {
				return;
			}
			if (!value.url.toString().equalsIgnoreCase("http://www.kanshuge.com/files/article/html/52/52432/9671522.html")) {
				return;
			}
			
			/*
			String clusterid = null;
			
			md.reset();
			String text = value.url.toString();
			md.update(text.getBytes());
			Long docid= new Long(0);
			try {
				md.digest(dig_id, 0, 16);
				for (int j = 0; j < dig_id.length; ++j) {
					docid=docid<<8;
					docid += dig_id[j]&0xff;
				}
				clusterid = url2cluster.get(docid);
				if (clusterid == null) {
					return;
				}
			} catch (DigestException e) {
			//	docid = Long.valueOf(html_page.xpath2info.get(xpath).num);
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
*/
			
			Attribute err_reason = value.getAttribute("Error-Reason");
			if (err_reason != null) {
				return;
			}
			if (value.body.getLength() > 0) {
				int rc = 0;
				try {
					Attribute originSiteAttr = value.getAttribute("Original-Size");
					if (originSiteAttr == null)
						throw new IOException("NULL Original-Size");
					int originalSize = Integer.parseInt(originSiteAttr.val.toString());
					if (originalSize > 4096 * 1024) {
						rc = 1;
						throw new Exception("error");
					}
					Inflater decompresser = new Inflater();
decompresser.setInput(value.body.getBytes(),0, value.body.getLength());
					int resultLength=0;
					try {
						resultLength = decompresser.inflate(output);
					} catch (DataFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (resultLength != originalSize) {
						rc = 2;
						throw new Exception("error");
					}
					decompresser.end();
					String htmlPage = new String(output, 0, resultLength, "utf8");


					
					context.write(value.url, new Text(htmlPage));

				} catch (Throwable e) {
					System.out.println(value.url);
					e.printStackTrace();
					context.getCounter("SCANNER", "ErrorContent:" + rc).increment(1);
				}
				
			}
		}
	}
	
	private static class ScanReducer extends
	Reducer<Text, Text, Text, Text> {
		
		protected void setup(Context context) throws IOException, InterruptedException {

		}
		
		public void reduce(Text key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			for (Text val: values) {
				context.write(key, val);
			}
		}
		

	}


	
	public int run(String[] args) throws Exception {
		Job job = new Job(this.getConf(), "AdTrainDataExtractor");

		job.setJarByClass(this.getClass());

		job.setMapperClass(ScanMapper.class);
		job.setReducerClass(ScanReducer.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);

	//	job.setPartitionerClass(ScanPartitioner.class);

		job.setInputFormatClass(OriginPageInputFormatV3.class);
	//	job.setOutputFormatClass(OriginPageOutputFormat.class);
	//	job.setOutputFormatClass(GBKOutputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
	//	LazyOutputFormat.setOutputFormatClass(job, GBKOutputFormat.class);

		job.waitForCompletion(true);

		return 0;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Tool pageScanner = new MRWebkitDebug();
		ToolRunner.run(pageScanner, args);
	}

}
