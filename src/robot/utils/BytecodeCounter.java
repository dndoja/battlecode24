package robot.utils;

import battlecode.common.Clock;
import robot.Constants;

public class BytecodeCounter {
    private static int bytecodesUsed = 0;

    public static void start(){
        bytecodesUsed = Clock.getBytecodeNum();
    }
    
    public static void checkpoint(String scope){
        final int current = Clock.getBytecodeNum();
        if (Constants.PRINT) {
            System.out.println(scope + ": " + (current - bytecodesUsed));
        }
        bytecodesUsed = current;
    }
}
