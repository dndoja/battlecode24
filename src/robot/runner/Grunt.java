package robot.runner;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import robot.comms.squad.SquadChannel1;
import robot.comms.squad.SquadComms;
import robot.pathing.BruteMover;
import robot.state.Formations;
import robot.state.RobotState;
import robot.utils.Offset;

public final class Grunt {
    public static void doTurn(RobotController rc, RobotState state, int turnNumber, BruteMover mover) throws GameActionException {
        if (rc.canSenseRobot(state.getCaptainId())){
            final RobotInfo captain = rc.senseRobot(state.getCaptainId());
            final MapLocation captainLoc = captain.location;
            final SquadChannel1 channel1Data = SquadComms.readChannel1(rc, state.getSquadNumber());

            if (channel1Data == null) {
                return;
            }

            final MapLocation currentLocation = rc.getLocation();
            final Direction targetDirection = currentLocation.directionTo(channel1Data.targetLocation); 
            final Offset positionInFormation = Formations.getFormationFromDirection(targetDirection)[state.getRank()];
            final MapLocation targetLocation = captainLoc.translate(positionInFormation.dx, positionInFormation.dy);
            mover.setTarget(targetLocation, 1);
        }

        mover.move();
    }
}
