package tech.michaeloverman.android.mscount.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael on 2/26/2017.
 */

public class Utilities {
    public static final String TAG = Utilities.class.getSimpleName();

    /**
     * Used to turn beat length data, in List, from database, into int[] for actual use by metronome.
     * @param integerList
     * @return
     */
    public static int[] integerListToArray(List<Integer> integerList) {
//        if(integerList == null) return null;
        int[] ints = new int[integerList.size()];
        for(int i = 0; i < integerList.size(); i++) {
            ints[i] = integerList.get(i);
        }
        return ints;
    }

    /**
     * Used by hard coded data entry. Should probably be removed prior to actual release.
     * @param ints
     * @return
     */
    public static List<Integer> arrayToIntegerList(int[] ints) {
        List<Integer> list = new ArrayList<>();
        for(int i = 0 ; i < ints.length; i++) {
            list.add(ints[i]);
        }
        return list;
    }

    public static void printArray(int[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i] + ", ");
        }
        Log.d(TAG, sb.toString());
    }

    public static int[] combine(int[] countoff, int[] beats) {
        Log.d(TAG, "combine() arrays...");
        int[] combination = new int[countoff.length + beats.length];
        System.arraycopy(countoff, 0, combination, 0, countoff.length);
        System.arraycopy(beats, 0, combination, countoff.length, beats.length);
        return combination;
    }

    public static void appendCountoff(int[] countoff, List<Integer> beats, List<Integer> downBeats) {
        for(int i = countoff.length - 1; i >= 0; i--) {
            beats.add(0, countoff[i]);
        }
        downBeats.add(0, countoff.length);
//        return compound;
    }

    public static List<Integer> createBeatList(int[] downbeats, int subd) {
        List<Integer> beats = new ArrayList<>();
        for (int i = 0; i < downbeats.length; i++) {
            for(int j = 0; j < downbeats[i]; j++) {
                beats.add(subd);
            }
        }
        return beats;
    }
}
