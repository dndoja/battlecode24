package robot.pathing;

import java.util.ArrayList;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public interface Navigator {
    public ArrayList<Direction> getValidDirections(MapLocation location);
}