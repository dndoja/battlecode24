package robot;

import battlecode.common.Team;

public class Logger {
    public static void log(String message) {
        if (RobotPlayer.state.getTeam() == Team.A){
            System.out.println(message);
        }
    }
}
