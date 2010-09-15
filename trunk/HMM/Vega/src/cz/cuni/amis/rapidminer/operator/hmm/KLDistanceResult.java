package cz.cuni.amis.rapidminer.operator.hmm;

import com.rapidminer.operator.ResultObjectAdapter;

public class KLDistanceResult extends ResultObjectAdapter {

    protected double distance;

    public KLDistanceResult(double distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "Distance between HMMs is: " + distance;
    }
}
