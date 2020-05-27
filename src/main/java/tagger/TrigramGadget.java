package tagger;

class TrigramGadget {


    public int n;
    public int selected;
    public Tag[] tag = new Tag[Tag.MAX_WORD_VERSIONS];
    public double[] lexProb = new double[Tag.MAX_WORD_VERSIONS];
    public double[][] prob = new double[Tag.MAX_WORD_VERSIONS][Tag.MAX_WORD_VERSIONS];
    public char[][] prev = new char[Tag.MAX_WORD_VERSIONS][Tag.MAX_WORD_VERSIONS];      // using char instead of int seemed faster on UNIX 981012

    public TrigramGadget() {
        for (int i = 0; i < tag.length; i++) {
            tag[i] = new Tag();
        }
        reset();
    } // Reset(); make sure this has no effect on tagging performance

    void reset() {
        for (int i = 0; i < Tag.MAX_WORD_VERSIONS; i++) {
            tag[i] = null;
            lexProb[i] = 0;
            for (int j = 0; j < Tag.MAX_WORD_VERSIONS; j++) {
                prev[i][j] = (char) -1;
                prob[i][j] = -1;
            }
            n = 0;
        }
        selected = -1;
    }

    void normalize(int m, int p) {
        double sum = 0;
        int i;
        for (i = 0; i < m; i++)
            for (int j = 0; j < p; j++)
                sum += prob[i][j];
        for (i = 0; i < m; i++)
            for (int j = 0; j < p; j++)
                prob[i][j] /= sum;
    }

    void setTags(Word w) {
        n = 0;
        for (WordTag q = w; q != null; q = q.next()) {
            tag[n] = q.getTag();
            prob[0][n] = q.getLexProb();
            n++;
        }
    }
}