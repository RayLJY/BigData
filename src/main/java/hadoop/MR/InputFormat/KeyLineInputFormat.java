package hadoop.MR.InputFormat;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Ray on 17/4/1.
 * This class like {@link KeyValueTextInputFormat}. I use {@link IntWritable} type
 * as Key type, not {@link Text} type in order to sorting by number. And there
 * also have some things different. Each line is divided into key and value
 * parts by a separator byte ('\t'). If no such a byte exists, the key will is
 * the min_value of the {@link Integer} type. Clean your data to make sure key can
 * be converted into the Long type.
 */
public class KeyLineInputFormat extends FileInputFormat<IntWritable, Text> {
    @Override
    public RecordReader<IntWritable, Text> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
        context.setStatus(split.toString());
        return new KeyLineRecordReader();
    }
}

class KeyLineRecordReader extends RecordReader<IntWritable, Text> {

    private final LineRecordReader lineRecordReader;

    private IntWritable key;

    private Text value;

    KeyLineRecordReader() throws IOException {
        lineRecordReader = new LineRecordReader();
    }

    @Override
    public void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
        lineRecordReader.initialize(split, context);
    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        byte[] line;
        int lineLen;
        if (lineRecordReader.nextKeyValue()) {
            Text innerValue = lineRecordReader.getCurrentValue();
            line = innerValue.getBytes();
            lineLen = innerValue.getLength();
        } else {
            return false;
        }
        if (line == null)
            return false;
        if (key == null) {
            key = new IntWritable() ;
        }
        if (value == null) {
            value = new Text();
        }
        int pos = findSeparator(line, lineLen);
        setKeyValue(key, value, line, lineLen, pos);
        return true;
    }

    private int findSeparator(byte[] utf, int length) {
        byte separator = (byte) '\t';
        for (int i = 0; i < length; i++)
            if (utf[i] == separator) {
                return i;
            }
        return -1;
    }

    private void setKeyValue(IntWritable key, Text value, byte[] line,
                             int lineLen, int pos) {
        if (pos == -1) {
            key.set(Integer.MIN_VALUE);
            value.set(line);
        } else {
            int keyInt = Integer.valueOf(new String(Arrays.copyOfRange(line, 0, pos)));
            key.set(keyInt);
            value.set(line, pos + 1, lineLen - pos - 1);
        }
    }

    @Override
    public IntWritable getCurrentKey() {
        return key;
    }

    @Override
    public Text getCurrentValue() {
        return value;
    }

    @Override
    public float getProgress() throws IOException {
        return lineRecordReader.getProgress();
    }

    @Override
    public void close() throws IOException {
        lineRecordReader.close();
    }
}
