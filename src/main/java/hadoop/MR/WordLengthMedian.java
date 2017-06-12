package hadoop.MR;

import com.google.common.base.Charsets;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.TaskCounter;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/**
 * Created by Ray on 17/4/10.
 * The readAndFindMedian function is wrong in {@link org.apache.hadoop.examples.WordMedian},
 * so I rewrite one.
 */
public class WordLengthMedian extends Configured implements Tool {

    public static class LengthMap extends Mapper<LongWritable, Text, IntWritable, IntWritable> {

        private static IntWritable ONE = new IntWritable(1);
        private static IntWritable LENGTH = new IntWritable();

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            StringTokenizer tokens = new StringTokenizer(value.toString());
            while (tokens.hasMoreTokens()) {
                int length = tokens.nextToken().length();
                LENGTH.set(length);
                context.write(LENGTH, ONE);
            }
        }
    }

    public static class SumReduce extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {

        private static IntWritable SUM = new IntWritable();

        @Override
        protected void reduce(IntWritable key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {

            int sum = 0;
            for (IntWritable v : values) {
                sum += v.get();
            }
            SUM.set(sum);
            context.write(key, SUM);
        }
    }

    @Override
    public int run(String[] args) throws Exception {

        Configuration conf = getConf();
        Job job = Job.getInstance(conf);

        job.setJarByClass(WordLengthMedian.class);

        job.setMapperClass(LengthMap.class);
        job.setCombinerClass(SumReduce.class);
        job.setReducerClass(SumReduce.class);

        //job.setInputFormatClass(TextInputFormat.class);
        //job.setOutputFormatClass(TextOutputFormat.class);

        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0].trim()));
        Path output = new Path(args[1].trim());
        FileOutputFormat.setOutputPath(job, output);

        // using in test
        if (FileUtil.fullyDelete(new File(output.toString())))
            System.out.println("delete directory : " + output.toString());

        boolean ret = job.waitForCompletion(true);

        long totalWords = job.getCounters()
                .getGroup(TaskCounter.class.getCanonicalName())
                .findCounter("MAP_OUTPUT_RECORDS", "Map output records").getValue();
        long medianIndex1 = totalWords + 1 >> 1;
        long medianIndex2 = totalWords + 2 >> 1;

        System.out.println("totalWords " + totalWords);
        System.out.println("medianIndex1 " + medianIndex1);
        System.out.println("medianIndex2 " + medianIndex2);

        if (ret)
            readAndFindMedian(output.toString(), medianIndex1, medianIndex2, conf);

        return ret ? 0 : 1;
    }

    private double readAndFindMedian(String path, long medianIndex1,
                                     long medianIndex2, Configuration conf) throws IOException {
        // no median found
        double theMedian = -1;

        FileSystem fs = FileSystem.get(conf);
        Path file = new Path(path, "part-r-00000");

        if (!fs.exists(file))
            throw new IOException("Output not found!");

        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(fs.open(file), Charsets.UTF_8));
            int num = 0;

            String line;
            while ((line = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);

                // grab length
                String currLen = st.nextToken();

                // grab count
                String lengthFreq = st.nextToken();

                int preNum = num;
                num += Integer.parseInt(lengthFreq);

                if (preNum < medianIndex1 && medianIndex2 <= num) {
                    System.out.println("The median is: " + currLen);
                    br.close();
                    return Double.parseDouble(currLen);
                } else if (medianIndex1 == num) {// omit judging condition (&& medianIndex2 > num)
                    if ((line = br.readLine()) != null) {
                        StringTokenizer nextLine = new StringTokenizer(line);
                        String nextLen = nextLine.nextToken();
                        theMedian = (Integer.parseInt(currLen) + Integer.parseInt(nextLen)) / 2.0;
                    }
                    System.out.println("The median is: " + theMedian);
                    br.close();
                    return theMedian;
                }
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }
        return theMedian;
    }

    public static void main(String[] s) throws Exception {

        String[] args = {"data/hadoop/mr/wordCount", "res/hadoop/mr/wordCount/1"};

        if (args.length != 2)
            System.err.println("Usage: wordLengthMedian input output");

        int exitCode = ToolRunner.run(new Configuration(), new WordLengthMedian(), args);
        System.out.println("exit code : " + exitCode);
    }
}
