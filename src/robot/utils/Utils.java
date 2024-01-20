package robot.utils;

import battlecode.common.MapLocation;
import robot.RobotPlayer;

public class Utils {
    public static int trimRobotId(int id) {
        return id - 10000;
    }

    public static int untrimRobotId(int id) {
        return id + 10000;
    }

    public static boolean isLocationOnMap(MapLocation location) {
        return location.x >= 0 && 
            location.x < RobotPlayer.state.getMapWidth() && 
            location.y >= 0 && 
            location.y < RobotPlayer.state.getMapHeight();
    }

    public static int encodeLocation(MapLocation location) {
        return (location.x << 6) + location.y;
    }

    public static int encodeLocation(int x, int y) {
        return (x << 6) + y;
    }

    public static MapLocation decodeLocation(int encoded) {
        return new MapLocation(encoded >> 6, encoded & 0x3f);
    }
}
