package tagger;

import java.io.Serializable;

class Feature implements Serializable { // abstract class
    public static int MAX_CLASSES = 21;
    public static int MAX_VALUES = 100;
    public static int MAX_NAME = 8;
    public static int MAX_DESCRIPTION = 45;
    public static int MAX_FEATURES_PER_CLASS = 25;
    public static int UNDEF = 0;

    public String name;
    public String description;
    public int index;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getIndex() {
        return index;
    }

     /* std::ostream& operator<<(std::ostream& os, Feature &f) {
        os << f.Name();
        return os;
    }

     std::ostream& operator<<(std::ostream& os, Feature *f) {
        if (f)
            os << *f;
        else
        os << "(null)";
        return os;
    }*/
}



