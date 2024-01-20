package robot;

import battlecode.common.Team;

public class Loggy {
    public static void log(String message) {
        if (RobotPlayer.state.getTeam() == Team.A){
            System.out.println(message);
        }
    }
}
