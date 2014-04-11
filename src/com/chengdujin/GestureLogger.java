package com.chengdujin;

import com.leapmotion.leap.*;
import com.leapmotion.leap.Gesture.State;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

class SampleListener extends Listener {
    public void onConnect(Controller controller) {
        System.out.println("#LeapMotion: Connected");
        controller.enableGesture(Gesture.Type.TYPE_CIRCLE);
    }

    public void onExit(Controller controller) {
        System.out.println("#LeapMotion: Exited");
    }

    public void onFrame(Controller controller) {
        // Get the most recent frame and report some basic information
        Frame frame = controller.frame();

        GestureList gestures = frame.gestures();
        for (int i = 0; i < gestures.count(); i++) {
            Gesture gesture = gestures.get(i);

            switch (gesture.type()) {
                case TYPE_CIRCLE:
                    CircleGesture circle = new CircleGesture(gesture);
                    if (circle.state() != State.STATE_STOP) {
                        // Calculate clock direction using the angle between circle normal and pointable
                        if (circle.pointable().direction().angleTo(circle.normal()) <= Math.PI / 4) {
                            // Clockwise if angle is less than 90 degrees
                            String command = "python /home/jinyuan/Public/bnac.py jinyuan";
                            List<String> commandArgs = Arrays.asList(command.split(" "));
                            this.execCommand(commandArgs);
                        }
                    }
                    break;
                default:
                    System.out.println("#LeapMotion: Unknown gesture type.");
                    break;
            }
        }
    }

    private void execCommand(List<String> commands) {
        try {
            System.out.println(commands);
            //Run macro on target
            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            //Read output
            StringBuilder out = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null, previous = null;
            while ((line = br.readLine()) != null)
                if (!line.equals(previous)) {
                    previous = line;
                    out.append(line).append('\n');
                    System.out.println("Hello" + line);
                }

            //Check result
            if (process.waitFor() == 0)
                System.out.println("Success!");
            System.exit(0);

            //Abnormal termination: Log command parameters and output and throw ExecutionException
            System.err.println(commands);
            System.err.println(out.toString());
            System.exit(1);
        } catch (IOException e) {
            System.out.println();
        } catch (InterruptedException e) {
            System.out.println();
        }
    }

}

public class GestureLogger {
    public static void main(String[] args) {
        // Create a sample listener and controller
        SampleListener listener = new SampleListener();
        Controller controller = new Controller();

        // Have the sample listener receive events from the controller
        controller.addListener(listener);

        // Keep this process running until Enter is pressed
        System.out.println("Press Enter to quit...");

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Remove the sample listener when done
        controller.removeListener(listener);
    }
}
