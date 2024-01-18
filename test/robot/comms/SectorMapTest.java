package robot.comms;

import org.junit.Test;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import robot.utils.Sectors;

public class SectorMapTest {
    @Test
    public void testGetSectorNumber() {
        final Sectors regular = new Sectors(56,56);
        
        assert regular.getSectorNumber(0,0) == 0;
        assert regular.getSectorNumber(8, 0) == 1;
        assert regular.getSectorNumber(0, 8) == 8;

        final Sectors irregular = new Sectors(60, 60);
        assert irregular.getSectorNumber(59,0) == 7;

        final Sectors smol = new Sectors(31,31);
        assert smol.getSectorNumber(11, 26) == 13;

        final Sectors tiny = new Sectors(3, 3, 1);
        assert tiny.getSectorNumber(1, 1) == 4;
        assert tiny.getSectorNumber(0,0) == 0;
        assert tiny.getSectorNumber(0,2) == 6;
    }
    
    @Test
    public void testGetSectorCenter() {
        final Sectors regular = new Sectors(56,56);

        assert regular.getSectorCenter(0).equals(new MapLocation(3,3));
        assert regular.getSectorCenter(1).equals(new MapLocation(10,3));
        assert regular.getSectorCenter(8).equals(new MapLocation(3,10));

        final Sectors irregular = new Sectors(60, 60);
        assert irregular.getSectorCenter(0).equals(new MapLocation(3,3));
        assert irregular.getSectorCenter(7).equals(new MapLocation(54,3));
        assert irregular.getSectorCenter(63).equals(new MapLocation(54,54));

        final Sectors smol = new Sectors(31,31);
        assert smol.getSectorCenter(0).equals(new MapLocation(3,3));
        assert smol.getSectorCenter(1).equals(new MapLocation(10,3));

        final Sectors tiny = new Sectors(3, 3, 1);
        assert tiny.getSectorCenter(4).equals(new MapLocation(1,1));
        assert tiny.getSectorCenter(0).equals(new MapLocation(0,0));
    }

    @Test
    public void testGetSectorAtDirection() {
        final Sectors regular = new Sectors(56,56);

        assert regular.getSectorAtDirection(0, Direction.SOUTH) == -1;
        assert regular.getSectorAtDirection(0, Direction.NORTH) == 8;
        assert regular.getSectorAtDirection(0, Direction.NORTHEAST) == 9;
    }
}
