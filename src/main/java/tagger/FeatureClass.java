package tagger;

import common.Ensure;

public class FeatureClass extends Feature {
    public int[] features = new int[MAX_FEATURES_PER_CLASS];
    public int nFeatures;

    public FeatureClass() {
        nFeatures = 0;
    }

    public int getNFeatures() {
        return nFeatures;
    }

    public int getFeature(int n) {
        Ensure.ensure(n >= 0 && n < nFeatures);
        return features[n];
    }

    public boolean hasFeature(int fv) {
        for (int i = 0; i < nFeatures; i++)
            if (features[i] == fv)
                return true;
        return false;
    }
}
