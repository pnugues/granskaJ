package common;

import tagger.Settings;

import java.io.PrintStream;

public class Message {

    static PrintStream[] m_out = {System.err, System.err, System.err,
            System.err, System.err, System.err, System.err};

    static MessageType prevT = MessageType.MSG_STATUS;
    static int nWarnings = 0;
    static int nMinorWarnings = 0;

    private Message() {

    }

    public static void invoke(MessageType t, String... m) {
        if (t == MessageType.MSG_VERBOSE && !Settings.xVerbose)
            return;

        PrintStream out = m_out[MessageType.MSG_STATUS.ordinal()];

        if (t != MessageType.MSG_CONTINUE)
            out = m_out[t.ordinal()];

        if (out == null)
            return;

        if (prevT == MessageType.MSG_MINOR_WARNING && !Settings.xWarnAll)
            return;
        if (t == MessageType.MSG_MINOR_WARNING) {
            nMinorWarnings++;
            prevT = t;
            if (!Settings.xWarnAll)
                return;
        }

        ; // (*out) << std::endl;

        prevT = t;
        switch (t) {
            case MSG_N_TYPES:
            case MSG_ERROR:
                out.print("ERROR:  ");
                break;
            case MSG_WARNING:
                nWarnings++;
            case MSG_MINOR_WARNING:
                out.print("WARNING: ");
                break;
            case MSG_VERBOSE:
                break;
            case MSG_STATUS:
                break;
            case MSG_CONTINUE:
                out.print("         ");
                break;
            case MSG_COUNTS:
                if ((nWarnings > 0) || (nMinorWarnings > 0)) {
                    out.print(nWarnings);
                    out.print(" warning");
                    out.print(Basics.optS(nWarnings != 1));
                    out.print(' ');
                    if (nMinorWarnings > 0)
                        out.print("and ");
                    out.print(nMinorWarnings);
                    out.print(" minor warning");
                    out.print(Basics.optS(nMinorWarnings != 1));
                    out.print(' ');
                    nWarnings = nMinorWarnings = 0;
                } else
                    return;
        }

        /*
          if (m1)
            (*out) << m1;
          if (m2)
            (*out) << ' ' << m2;
          if (m3)
            (*out) << ' ' << m3;
          if (m4)
            (*out) << ' ' << m4;
         */
        out.print(String.join(" ", m));

        if (t == MessageType.MSG_WARNING || t == MessageType.MSG_ERROR) {
            //if (xCurrRuleTerm) (*out) << " ruleterm: " << xCurrRuleTerm;
            //if (xCurrSentence) (*out) << " sen: " << xCurrSentence;
        }
        if (t != MessageType.MSG_STATUS || Settings.xVerbose)
            out.println();
        //if (t == MSG_STATUS)
        //  (*out) << '\r';
        if (t == MessageType.MSG_STATUS)
            out.println();
        if (t == MessageType.MSG_ERROR) {
            Message.invoke(MessageType.MSG_COUNTS);
            System.exit(1);
        }
    }
/*
    Message(MessageType t, String m1, String m2, String m3, String m4) throws IOException {
        if (t == MessageType.MSG_VERBOSE && !Settings.xVerbose)
            return;
        MessageType prevT = MessageType.MSG_STATUS;
        int nWarnings = 0;
        int nMinorWarnings = 0;
        PrintStream out = m_out[MessageType.MSG_STATUS.ordinal()];
        if (t != MessageType.MSG_CONTINUE)
            out = m_out[t.ordinal()];
        if (out == null)
            return;
        if (t == MessageType.MSG_CONTINUE && prevT == MessageType.MSG_MINOR_WARNING && !Settings.xWarnAll)
            return;
        if (t == MessageType.MSG_MINOR_WARNING) {
            nMinorWarnings++;
            prevT = t;
            if (!Settings.xWarnAll)
                return;
        }
        prevT = t;
        switch (t) {
            case MSG_N_TYPES:
            case MSG_ERROR:
                out.write("ERROR:  ".getBytes());
                break;
            case MSG_WARNING:
                nWarnings++;
            case MSG_MINOR_WARNING:
                out.write("WARNING: ".getBytes());
                break;
            case MSG_VERBOSE:
                break;
            case MSG_STATUS:
                break;
            case MSG_CONTINUE:
                out.write("         ".getBytes());
                break;
            case MSG_COUNTS:
                if (nWarnings != 0 || nMinorWarnings != 0) {
                    if (nMinorWarnings != 0) {
                        out.write("and ".getBytes());
                        out.write(String.valueOf(nMinorWarnings).getBytes());
                        out.write(" minor warning".getBytes());
                        out.write(Basics.optS(nMinorWarnings != 1));
                        out.write(' ');
                    }
                    nWarnings = nMinorWarnings = 0;
                } else
                    return;
        }
        if (m1 != null)
            out.write(m1.getBytes());
        if (m2 != null) {
            out.write(' ');
            out.write(m2.getBytes());
        }
        if (m3 != null) {
            out.write(' ');
            out.write(m3.getBytes());
        }
        if (m4 != null) {

            out.write(' ');
            out.write(m4.getBytes());
        }
        /*if (t == MessageType.MSG_WARNING || t == MessageType.MSG_ERROR) {
            if (xCurrRuleTerm) ( * out) <<" ruleterm: " << xCurrRuleTerm;
            if (xCurrSentence) ( * out) <<" sen: " << xCurrSentence;
        }*/
        /*if (t != MessageType.MSG_STATUS || Settings.xVerbose)
            out.write(Settings.xEndl.getBytes());*/
    //if (t == MSG_STATUS)
    //  (*out) << '\r';
        /*if (t == MessageType.MSG_STATUS)
            out.write('\n');
        if (t == MessageType.MSG_ERROR) {
            new Message(MessageType.MSG_COUNTS);
            System.exit(1);
        }
    }*/
}