package cz.cuni.amis.rapidminer;

/**
 *
 * @author ik
 */
public class Util {

    public static int getClusterNum(String clusterValue) {
        return Integer.parseInt(clusterValue.split("_")[1]);
    }
}
