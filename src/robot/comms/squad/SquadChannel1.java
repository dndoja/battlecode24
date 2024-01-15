package robot.comms.squad;

import battlecode.common.MapLocation;
import robot.Constants;
import robot.state.SquadOrder;

public class SquadChannel1 {
    public final SquadOrder activeOrder;
    public final MapLocation targetLocation;

    public SquadChannel1(SquadOrder activeOrder, MapLocation targetLocation) {
        this.activeOrder = activeOrder;
        this.targetLocation = targetLocation;
    }

    public static SquadChannel1 decode(int encoded) {
        if (encoded == 0) {
            return null;
        }
        
        final int order = encoded >> 12 & 0xF;
        final int x = (encoded >> 6) & 0x3F;
        final int y = encoded & 0x3F;

        final SquadOrder activeOrder = Constants.ORDERS[order];

        return new SquadChannel1(activeOrder, new MapLocation(x, y));
    }

    public int encode() {
        final int order = activeOrder.ordinal();
        final int x = targetLocation.x;
        final int y = targetLocation.y;

        return (order << 12) | (x << 6) | y;
    }
}