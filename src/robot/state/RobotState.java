package robot.state;
import java.util.HashMap;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;
import robot.utils.Sectors;

public class RobotState {
    boolean initialized = false;

    public int id;
    public Team team;
    public MapSymmetry mapSymmetry;
    public Sectors mapSectors;
    public int mapHeight;
    public int mapWidth;
    public SpawnZones spawnZones;

    public int initialSpawnZone;
    public RobotRole role;
    public HashMap<Integer, RobotRole> squad;
    public int squadNumber;
    public int turnsSinceSquadFormation = -1;
    
    public int targetSectorNumber = -1;
    public int currentSectorNumber = -1;
    public int prevSectorNumber = -1;

    public RobotState(){
        team = null;
        squadNumber = -1;
        role = null;
        id = -1;
        squad = new HashMap<Integer, RobotRole>();
    }

    public void initialize(RobotController rc){
        if (initialized){
            return;
        }

        initialized = true;

        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();

        team = rc.getTeam();
        id = rc.getID();
        mapSectors = new Sectors(mapWidth, mapHeight);
        spawnZones = SpawnZones.fromSpawnLocations(rc.getAllySpawnLocations());
        mapSymmetry = MapSymmetry.find(spawnZones.getCombinedBoundingBox(), mapWidth, mapHeight);
    }

    public RobotRole getRole(){
        return role;
    }
    
    public void setRoleFromSpawnOrder(int spawnOrder){
        if (spawnOrder == 0){
            role = RobotRole.CAPTAIN;
        }else if (spawnOrder <= 3){
            role = RobotRole.MEDIC;
        }else if (spawnOrder <= 6){
            role = RobotRole.SOLDIER;
        }else{
            role = RobotRole.BUILDER;
        }
    }

    public Team getTeam(){
        return team;
    }

    public int getId(){
        return id;
    }

    public int getSquadNumber(){
        return squadNumber;
    }

    public void setSquadNumber(int squadNumber){
        this.squadNumber = squadNumber;
    }

    public void addSquadMember(int id, RobotRole role){
        squad.put(id, role);
    }

    public HashMap<Integer, RobotRole> getSquad(){
        return squad;
    }

    public int getTurnsSinceSquadFormation(){
        return turnsSinceSquadFormation;
    }

    public void incrementTurnsSinceSquadFormation(){
        turnsSinceSquadFormation++;
    }

    public Sectors getMapSectors(){
        return mapSectors;
    }

    public SpawnZones getSpawnZones(){
        return spawnZones;
    }

    public int getInitialSpawnZone(){
        return initialSpawnZone;
    }

    public void setInitialSpawnZone(int initialSpawnZone){
        this.initialSpawnZone = initialSpawnZone;
    }

    public MapSymmetry getMapSymmetry(){
        return mapSymmetry;
    }

    public int getTargetSectorNumber(){
        return targetSectorNumber;
    }

    public void setTargetSectorNumber(int targetSectorNumber){
        this.targetSectorNumber = targetSectorNumber;
    }

    public int getCurrentSectorNumber(){
        return currentSectorNumber;
    }

    public int getPrevSectorNumber(){
        return prevSectorNumber;
    }

    public boolean updateCurrentSectorNumber(MapLocation mapLocation){
        final int currentSectorNumber = mapSectors.getSectorNumber(mapLocation);
        final boolean changed = this.currentSectorNumber != currentSectorNumber;
        this.prevSectorNumber = this.currentSectorNumber;
        this.currentSectorNumber = currentSectorNumber;

        return changed;
    }

    public int getMapWidth(){
        return mapWidth;
    }

    public int getMapHeight(){
        return mapHeight;
    }
}
