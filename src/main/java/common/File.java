package common;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class File {

    public static int MAX_FILE_NAME_LENGTH = 200;


    /* PN:
    //Probably, useless function
    public static void SkipSpaceButNotNewLine(InputStream in) throws IOException {
        while (isspace(in.peek()) && in.peek() != '\n')
            in.read();
    }*/

    public static String addFileName(String dir, String file) {
        String newFile;
        if (dir != null)
            newFile = dir;
        else {
            newFile = null;
            if (file == null) {
                Message.invoke(MessageType.MSG_WARNING, "trying to create empty file name");
            }
        }
        if (file != null) {
            // jonas, 13 rows of new code to always have exactly 1 slash between directory levels
            int l = newFile.length();

            if (l > 0 && newFile.charAt(l - 1) != '/')
                newFile += "/";
            if ((file != null) && (file.charAt(0) == '/'))
                newFile += file.substring(1); // statement of if-statement in #ifdef
            else
                newFile += file;
        }
        return newFile;
    }

    public static String extension(String fileName) {
        for (int i = fileName.length() - 1; i > 0; i--)
            if (fileName.charAt(i) == '.')
                return fileName.substring(i + 1);
            else if (fileName.charAt(i) == '/')
                return "";
        return "";
    }

    public static boolean compareLabels(String labelRead, String labelWanted, boolean warn) throws IOException {
        if (labelWanted != null && labelWanted.equals(labelRead)) {
            if (warn)
                Message.invoke(MessageType.MSG_WARNING, "label wanted:", labelWanted, "; label read:", labelRead);
            return false;
        }
        return true;
    }

    public void setVersion(PrintStream out, String label) throws IOException {
        out.print(label);
    }

    public boolean checkVersion(InputStream in, String label) throws IOException {
        return checkLabel(in, label, false);
    }

    public boolean checkLabel(InputStream is, String labelWanted, boolean warn) throws IOException {
        Reader in = new InputStreamReader(is, StandardCharsets.UTF_8);

        char[] labelRead = new char[MAX_FILE_NAME_LENGTH];
        in.read(labelRead, 0, MAX_FILE_NAME_LENGTH);
        String labelReadS = new String(labelRead).strip();
        return compareLabels(labelReadS, labelWanted, warn);
    }
}

