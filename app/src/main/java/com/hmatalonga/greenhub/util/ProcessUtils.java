package com.hmatalonga.greenhub.util;

import android.util.Log;

import com.hmatalonga.greenhub.managers.sampling.Inspector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Created by marco on 12-03-2018.
 */

public class ProcessUtils {

    private static final String TAG = makeLogTag(Inspector.class);

    public static List<String> getCommandOutputAsList(String command) {
        String[] lines = getCommandOutput(command).split(System.getProperty("line.separator"));
        return Arrays.asList(lines);
    }

    public static String getCommandOutput(String command) {

        try {
            // Executes the command.
            Process process = Runtime.getRuntime().exec(command);

            // Reads stdout.
            // NOTE: You can write to stdin of the command using
            //       process.getOutputStream().
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();

            // Waits for the command to finish.
            process.waitFor();

            return output.toString();


        } catch (IOException e) {
            Log.e(TAG, "Could not get ps command output");
        } catch (InterruptedException e) {
            Log.e(TAG, "Could not get ps command output");
        }

        return "";
    }
}
