package robot.pathing;

import java.util.Stack;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import robot.utils.Utils;

public class BugNav {
    Direction mainDirection = Direction.CENTER;
    Direction secondaryDirection = Direction.CENTER;
    boolean prioritizeLeftTurns = false;
    Stack<Direction> reverse = new Stack<>();
    RobotController rc;

    public BugNav(RobotController rc) {
        this.rc = rc;
    }

    public void setMainDirection(Direction mainDirection) {
        this.mainDirection = mainDirection;
        this.secondaryDirection = mainDirection;
    }

    public void setSecondaryDirection(Direction secondaryDirection) {
        this.secondaryDirection = secondaryDirection;
        int rightTurns = 0;
        while (mainDirection != secondaryDirection) {
            mainDirection = mainDirection.rotateRight();
            rightTurns++;
        }

        if (rightTurns > 4) {
            prioritizeLeftTurns = true;
        }
    }

    public void move() throws GameActionException {
        if (!rc.isMovementReady()) return;

        Direction direction = mainDirection;
        final MapLocation currentLocation = rc.getLocation();

        for (int i = 7; --i >= 0; ) {
            final MapLocation nextLoc = currentLocation.add(direction);

            if (rc.canMove(direction)) {
                rc.move(direction);
                reverse.push(direction.opposite());
                return;
            }else if (Utils.isLocationOnMap(nextLoc) && rc.sensePassability(nextLoc)) {
                return;
            }

            direction = prioritizeLeftTurns ? direction.rotateLeft() : direction.rotateRight();
        }

        if (!reverse.isEmpty() && rc.canMove(reverse.peek())){
            rc.move(reverse.pop());
            return;
        }
    }
}
