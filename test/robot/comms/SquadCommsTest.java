package robot.comms;
import org.junit.Test;

import battlecode.common.MapLocation;
import robot.comms.squad.*;
import robot.state.SquadOrder;
public class SquadCommsTest {
    @Test
    public void testSquadChannel1() {
        final SquadChannel1 channel = new SquadChannel1(SquadOrder.ATTACK, new MapLocation(7, 5));
        final int encoded = channel.encode();
        final SquadChannel1 decoded = SquadChannel1.decode(encoded);

        assert decoded.activeOrder == SquadOrder.ATTACK;
        assert decoded.targetLocation.equals(new MapLocation(7, 5));
    }

    @Test
    public void testSquadChannel2() {
        final SquadChannel2 channel = new SquadChannel2(7, 5);
        final int encoded = channel.encode();
        final SquadChannel2 decoded = SquadChannel2.decode(encoded);

        assert decoded.currentSectorNumber == 7;
    }
}
