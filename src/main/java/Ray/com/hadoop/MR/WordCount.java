package Ray.com.hadoop.MR;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by ray on 17/3/29.
 * <p>
 * word count example
 */
public class WordCount extends Configured implements Tool {

    public static class wordMap extends MapReduceBase implements Mapper<LongWritable, Text, Text, LongWritable> {
        private LongWritable one = new LongWritable(1l);

        public void map(LongWritable key, Text value, OutputCollector<Text, LongWritable> output, Reporter reporter) throws IOException {
            String lines = value.toString().replaceAll("[\":,.\\[\\]'”/\\\\—“]", " ");
            String[] words = lines.split("\\s+");

            for (String word : words) {
                if (word.length() > 0) {
                    output.collect(new Text(word.toLowerCase()), one);
                }
            }
        }
    }

    public static class countReduce extends MapReduceBase implements Reducer<Text, LongWritable, Text, LongWritable> {

        public void reduce(Text key, Iterator<LongWritable> values, OutputCollector<Text, LongWritable> output, Reporter reporter) throws IOException {
            long count = 0l;
            while (values.hasNext()) {
                count += values.next().get();
            }
            output.collect(key, new LongWritable(count));
        }
    }


    public int run(String[] args) throws Exception {
        if (args.length != 2) {
            throw new Exception("args: input output");
        }
        Path input = new Path(args[0].trim());
        Path output = new Path(args[1].trim());

        JobConf conf = new JobConf(getConf(), WordCount.class);

        conf.setJobName("word count");
        conf.setJarByClass(WordCount.class);
        conf.setMapperClass(wordMap.class);
        conf.setReducerClass(countReduce.class);
        conf.setMapOutputKeyClass(Text.class);
        conf.setMapOutputValueClass(LongWritable.class);
        conf.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.setInputPaths(conf, input);
        FileOutputFormat.setOutputPath(conf, output);

        RunningJob job = JobClient.runJob(conf);

        return job.isSuccessful() ? 0 : 1;
    }

    public static void main(String[] s) throws Exception {

        String[] args = {"data/hadoop/mr/wordcount", "res/hadoop/mr/wordcount/1"};

        if (args.length != 2) {
            throw new Exception("args: input directory output directory");
        }

        int exitCode = ToolRunner.run(new Configuration(), new WordCount(), args);

        System.out.println("exit code :" + exitCode);
    }
}
