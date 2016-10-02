package tech.michaeloverman.android.stickit;

import android.content.Context;

/**
 * Created by Michael on 5/18/2016.
 */
public class Sequence {
    private static final String TAG = "Sequence";
    private static int patternLength;
    private static boolean breakUpSpace = true;
    private static int breakUpDiff = 0;
//    private static final String FILES_FOLDER = "sequences";

//    private AssetManager mAssets;

    private int[] two4nextDisp = {1,3,5,7,9,11,13,15,17,19,21,23,25,27,29,31,33,35,37,39,41,43,45,47,49,51,53,55,57,59,61,63,65,67,69,71,73,75,77,79,81,83,85,87,89,91,93,95,97,99,101,103,105,107,109,111,113,115,117,119,121,123,125,127,129,131,133,135,137,139,141,143,145,147,149,151,153,155,157,159,161,163,165,167,169,171,173,175,177,179,181,183,185,187,189,191,193,195,197,199,201,203,205,207,209,211,213,215,217,219,221,223,225,227,229,231,233,235,237,239,241,243,245,247,249,251,253,255,2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,50,52,54,56,58,60,62,64,66,68,70,72,74,76,78,80,82,84,86,88,90,92,94,96,98,100,102,104,106,108,110,112,114,116,118,120,122,124,126,128,130,132,134,136,138,140,142,144,146,148,150,152,154,156,158,160,162,164,166,168,170,172,174,176,178,180,182,184,186,188,190,192,194,196,198,200,202,204,206,208,210,212,214,216,218,220,222,224,226,228,230,232,234,236,238,240,242,244,246,248,250,252,254,256};
    private int[] two4prevDisp = {1,129,2,130,3,131,4,132,5,133,6,134,7,135,8,136,9,137,10,138,11,139,12,140,13,141,14,142,15,143,16,144,17,145,18,146,19,147,20,148,21,149,22,150,23,151,24,152,25,153,26,154,27,155,28,156,29,157,30,158,31,159,32,160,33,161,34,162,35,163,36,164,37,165,38,166,39,167,40,168,41,169,42,170,43,171,44,172,45,173,46,174,47,175,48,176,49,177,50,178,51,179,52,180,53,181,54,182,55,183,56,184,57,185,58,186,59,187,60,188,61,189,62,190,63,191,64,192,65,193,66,194,67,195,68,196,69,197,70,198,71,199,72,200,73,201,74,202,75,203,76,204,77,205,78,206,79,207,80,208,81,209,82,210,83,211,84,212,85,213,86,214,87,215,88,216,89,217,90,218,91,219,92,220,93,221,94,222,95,223,96,224,97,225,98,226,99,227,100,228,101,229,102,230,103,231,104,232,105,233,106,234,107,235,108,236,109,237,110,238,111,239,112,240,113,241,114,242,115,243,116,244,117,245,118,246,119,247,120,248,121,249,122,250,123,251,124,252,125,253,126,254,127,255,128,256};
    private int[] nextStone;
    private int[] nextOther;
    private int[] wasteDigit;

    private int currentSequence;
    private static int bitMask;

    public Sequence(Context context, int length) {
        newSequenceLength(length);
    }
    public void newSequenceLength(int length) {
        patternLength = length;

        bitMask = (1 << patternLength) - 1;

        currentSequence = 0b0101010101010101;
        if (patternLength % 2 == 1) {
            currentSequence = currentSequence << 1;
            breakUpDiff = 0;
        }
        currentSequence = currentSequence & bitMask;

    }
    public void setNewSpacing(int option) {
        switch (option) {
            case 0:
                breakUpSpace = false;
                break;
            case 1:
                breakUpSpace = true;
                breakUpDiff = 0;
                break;
            case 2:
                breakUpDiff = -1;
                breakUpSpace = true;
                break;
            default:
        }
    }
    private void loadStickings() {

    }

    public String toString() {
        return toString(currentSequence);
    }
    public String toString(int pattern) {
        StringBuffer sb = new StringBuffer();

        for (int i =patternLength -1; i >= patternLength / 2 - breakUpDiff; i--) {
            if (((pattern >> i) & 1) == 0) sb.append("R");
            else sb.append("L");
        }
        if (breakUpSpace) sb.append(" ");
        for (int i = (patternLength / 2) - 1 - breakUpDiff; i >= 0; i--) {
            if (((pattern >> i) & 1) == 0) sb.append("R");
            else sb.append("L");
        }

        return sb.toString();
    }
    public int getInt() {
        return currentSequence;
    }
    public static int cycleUp(int pattern) {
        return (pattern + 1) & bitMask;
    }
    public void cycleUp() {
        currentSequence++;
        if (currentSequence >= (1 << patternLength)) {
            currentSequence = 0;
        }
    }
    public static int cycleDown(int pattern) {
        return (pattern - 1) & bitMask;
    }
    public void cycleDown() {
        currentSequence--;
        if (currentSequence < 0) {
            currentSequence = (1 << patternLength) - 1;
        }
    }
    public static int rhythmicDisplaceR(int pattern) {
        int bit = (pattern & 1) << (patternLength - 1);
        pattern = pattern >> 1;
//        System.out.println("XORing " + Integer.toBinaryString(pattern) + " with " +
//                Integer.toBinaryString(bit));
        return (pattern ^ bit) & bitMask;
    }
    public void rhythmicDisplaceR() {
        currentSequence = rhythmicDisplaceR(currentSequence);
    }
    public static int rhythmicDisplaceL(int pattern) {
        int bit = (pattern >> (patternLength -1)) & 1;
        pattern = pattern << 1;
//        System.out.println("XORing " + Integer.toBinaryString(pattern) + " with " +
//                Integer.toBinaryString(bit));
        return (pattern ^ bit) & bitMask;
    }
    public void rhythmicDisplaceL() {
        currentSequence = rhythmicDisplaceL(currentSequence);
    }
    public static int switchHands(int pattern) {
        return ~ pattern;
    }
}
