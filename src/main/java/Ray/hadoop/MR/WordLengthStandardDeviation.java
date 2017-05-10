package Ray.hadoop.MR;

import com.google.common.base.Charsets;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.reduce.LongSumReducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/**
 * Created by Ray on 17/4/10.
 * Copy from {@link org.apache.hadoop.examples.WordStandardDeviation}.
 */

public class WordLengthStandardDeviation extends Configured implements Tool {

    private final static Text LENGTH = new Text("length");
    private final static Text SQUARE = new Text("square");
    private final static Text COUNT = new Text("count");
    private final static LongWritable ONE = new LongWritable(1);

    /**
     * Maps words from line of text into 3 key-value pairs; one key-value pair for
     * counting the word, one for counting its length, and one for counting the
     * square of its length.
     */
    public static class WordStandardDeviationMapper extends
            Mapper<Object, Text, Text, LongWritable> {

        private LongWritable wordLen = new LongWritable();
        private LongWritable wordLenSq = new LongWritable();

        /**
         * Emits 3 key-value pairs for counting the word, its length, and the
         * squares of its length. Outputs are (Text, LongWritable).
         *
         * @param value This will be a line of text coming in from our input file.
         */
        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString());
            while (itr.hasMoreTokens()) {
                String string = itr.nextToken();

                int length = string.length();
                this.wordLen.set(length);

                // the square of an integer is an integer...
                this.wordLenSq.set((long) Math.pow(length, 2.0));

                context.write(LENGTH, this.wordLen);
                context.write(SQUARE, this.wordLenSq);
                context.write(COUNT, ONE);
            }
        }
    }

    /**
     * Reads the output file and parses the summation of lengths, the word count,
     * and the lengths squared, to perform a quick calculation of the standard
     * deviation.
     *
     * @param path The path to find the output file in. Set in main to the output
     *             directory.
     * @throws IOException If it cannot access the output directory, we throw an exception.
     */
    private double readAndCalcStdDev(Path path, Configuration conf)
            throws IOException {
        FileSystem fs = FileSystem.get(conf);
        Path file = new Path(path, "part-r-00000");

        if (!fs.exists(file))
            throw new IOException("Output not found!");

        double stddev;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(fs.open(file), Charsets.UTF_8));
            long count = 0;
            long length = 0;
            long square = 0;
            String line;
            while ((line = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);

                // grab type
                String type = st.nextToken();

                // differentiate
                if (type.equals(COUNT.toString())) {
                    String countLit = st.nextToken();
                    count = Long.parseLong(countLit);
                } else if (type.equals(LENGTH.toString())) {
                    String lengthLit = st.nextToken();
                    length = Long.parseLong(lengthLit);
                } else if (type.equals(SQUARE.toString())) {
                    String squareLit = st.nextToken();
                    square = Long.parseLong(squareLit);
                }
            }

            // standard deviation = sqrt( sum( (length-mean)^2) / count )
            //                    = sqrt( sum(length^2-2*length*mean+mean^2) / count )
            //                    = sqrt( sum(length^2)/count - 2*mean*sum(length)/count + mean^2 )
            //                    = sqrt( sum(length^2) / count - 2*mean*mean + mean^2 )
            //                    = sqrt( sum(length^2) / count - mean^2 )


            // average = total sum / number of elements;
            double mean = 1.0 * length / count;

            double term = 1.0 * square / count;

            mean = Math.pow(mean, 2.0);

            stddev = Math.sqrt(term - mean);

            System.out.println("The standard deviation is: " + stddev);
        } finally {
            if (br != null) {
                br.close();
            }
        }
        return stddev;
    }

    @Override
    public int run(String[] args) throws Exception {

        Configuration conf = getConf();

        Job job = Job.getInstance(conf, "word standard deviation");
        job.setJarByClass(WordLengthStandardDeviation.class);
        job.setMapperClass(WordStandardDeviationMapper.class);
        job.setCombinerClass(LongSumReducer.class);
        job.setReducerClass(LongSumReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0].trim()));
        Path output = new Path(args[1].trim());
        FileOutputFormat.setOutputPath(job, output);

        // using in test
        if (FileUtil.fullyDelete(new File(output.toString())))
            System.out.println("delete directory : " + output.toString());

        boolean result = job.waitForCompletion(true);

        if (result)
            readAndCalcStdDev(output, conf);

        return result ? 0 : 1;
    }

    public static void main(String[] s) throws Exception {

        String[] args = {"data/hadoop/mr/wordCount", "res/hadoop/mr/wordCount/1"};

        if (args.length != 2)
            System.err.println("Usage: WordLengthStandardDeviation input output");

        ToolRunner.run(new Configuration(), new WordLengthStandardDeviation(), args);
    }
}