package de.saar.coli.irtg.experimental.astar;

class RuntimeStatistics {
    private long numDequeuedItems;
    private long runtime;
    private double score;
    private long numDequeuedSupertags;
    private int N;

    public RuntimeStatistics(int sentenceLength, long numDequeuedItems, long numDequeuedSupertags, long runtime, double score) {
        this.numDequeuedItems = numDequeuedItems;
        this.numDequeuedSupertags = numDequeuedSupertags;
        this.runtime = runtime;
        this.score = score;
        this.N = sentenceLength;
    }

    public double getScore() {
        return score;
    }

    public long getRuntime() {
        return runtime;
    }

    public long getNumDequeuedItems() {
        return numDequeuedItems;
    }

    public long getNumDequeuedSupertags() {
        return numDequeuedSupertags;
    }

    public int getSentenceLength() {
        return N;
    }

    @Override
    public String toString() {
        return String.format("length=%d, time=%dms, dequeued=%d, supertags=%d, logprob=%f", N, runtime / 1000000, numDequeuedItems, numDequeuedSupertags, score);
    }
}
