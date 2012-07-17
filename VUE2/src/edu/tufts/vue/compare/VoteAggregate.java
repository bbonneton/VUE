/*
* Copyright 2003-2010 Tufts University  Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.osedu.org/licenses/ECL-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

/**
 *
 * @author akumar03
 */
package edu.tufts.vue.compare;

import tufts.vue.DEBUG;
import java.util.*;

public class VoteAggregate extends WeightAggregate
{
    private static final org.apache.log4j.Logger Log = org.apache.log4j.Logger.getLogger(VoteAggregate.class);

    public static final int POSITIVE_VOTE = 1;
    public static final int NEGATIVE_VOTE = 0;
    public static final double DEFAULT_THRESHOLD = .5;
    
    private double nodeThreshold = DEFAULT_THRESHOLD;
    private double linkThreshold = DEFAULT_THRESHOLD;
    
    protected final int[][] linkCounts;
    
    public static VoteAggregate create(List<ConnectivityMatrix> matrices) {
        final IndexedCountingSet preComputed = new IndexedCountingSet();
        for (ConnectivityMatrix matrix : matrices)
            preComputed.addAll(matrix.keys);
        // We pre-compute the index so the matrix aggregate will know how big
        // of a matrix will be required to hold it all.
        return new VoteAggregate(preComputed, matrices);
    }
    
    private VoteAggregate(IndexedCountingSet preComputed, List<ConnectivityMatrix> matrices) {
        super(preComputed, matrices);
        // WeightAggregate superclass will have merged in all the matrices to produce counts: we save them here
        this.linkCounts = super.cx.clone();
        computeVotes();
    }
    
    private void computeVotes() {
        
        final int count = getCount();
        final double threshold = (double)count*linkThreshold;
        final int size = super.size();
        if (DEBUG.Enabled) Log.debug("computeVotes, threshold=" + threshold + " out of " + count);
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (super.cx[i][j] >= threshold)
                    super.cx[i][j] = POSITIVE_VOTE;
                else
                    super.cx[i][j] = NEGATIVE_VOTE;
            }
        }
    }
    
    /*
     * returns true if a node/label ia above the threshold
     * @param label the label/merge property of the nodes
     * @return  true if the node occurs above certain threshold
     */
    
    public boolean isNodeVoteAboveThreshold(Object key) {
        final int nodeCount = getNodeCount(key);
        final int count = getCount();
        final double threshold = (double)count*nodeThreshold;

        return nodeCount >= threshold;
    }
   /*
    * returns the nunber of links between two nodes
    * @param label1 label/merge property of node 1
    * @param label2 label/merge property of node 2
    * @return number of links between node 1 and node 2
    */
            
    public int getLinkCount(Object label1, Object label2) {
        int index1 = keys.findIndex(label1);
        int index2 = keys.findIndex(label2);
        if (index1 >= 0 && index2 >=0)
            return linkCounts[index1][index2];
        else
            return 0;
    }
    
    /*
     * This method is similar to isNodeVoteAboveThreshold except it works for links
     * @param label1 label/merge property of node 1
     * @param label2 label/merge property of node 2
     * @return true when number of links is above threshold
     */
    
    public boolean isLinkVoteAboveThreshold(Object key1, Object key2) {
        final int linkCount = getLinkCount(key1, key2);
        final int count = getCount();
        final double threshold = (double)count*linkThreshold;

        return (linkCount >= threshold) && (linkCount > 0);
    }
    
    public void setLinkThreshold(double percentage) {
        linkThreshold = percentage;
        if (DEBUG.Enabled) Log.debug("linkThresh set to " + percentage);
    }
    
    public void setNodeThreshold(double percentage) {
        nodeThreshold = percentage;
        if (DEBUG.Enabled) Log.debug("nodeThresh set to " + percentage);
    }

    // public VoteAggregate(List<ConnectivityMatrix> matrices,boolean nodeMerge) { super(matrices); }    
}
