package Ray.hadoop.MR.io;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by Ray on 17/4/1.
 * <p>
 * This type be use for Secondary sort that is grouped by Int type and
 * sorted by characters type.
 */
public class IntTextPair implements WritableComparable<IntTextPair> {

    private IntWritable first = new IntWritable();
    private Text second = new Text();

    public void set(int left, String right) {
        first.set(left);
        second.set(right);
    }

    public IntWritable getFirst() {
        return first;
    }

    public Text getSecond() {
        return second;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        first.readFields(in);
        second.readFields(in);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        first.write(out);
        second.write(out);
    }

    @Override
    public int hashCode() {
        return first.hashCode() + second.hashCode();
    }

    @Override
    public boolean equals(Object right) {
        if (right instanceof IntTextPair) {
            IntTextPair r = (IntTextPair) right;
            return r.first.get() == first.get() && r.second.equals(second);
        } else {
            return false;
        }
    }

    /**
     * This function decides whether using ascending sort or not.
     */
    @Override
    public int compareTo(IntTextPair o) {
        if (first.get() != o.first.get()) {
            return first.get() < o.first.get() ? -1 : 1;
        } else {
            return second.compareTo(o.getSecond());
        }
    }


    /**
     * A Comparator that compares serialized IntTextPair.
     * There have some differences of sort by character,
     * e.g:
     *    type one:
     *             ab
     *             acd
     *             ad
     *
     *    type two:
     *             ab
     *             ad
     *             acd
     *
     * If we don't offer a comparator for this type, we will get the result of type one.
     * If we want the result that like the type two, try to make a comparator.
     * advise: you can use {@link Text.Comparator}.
     */
//    static {
//        WritableComparator.define(IntTextPair.class, new Text.Comparator());
//    }

}
