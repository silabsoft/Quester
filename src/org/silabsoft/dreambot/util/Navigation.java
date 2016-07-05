/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silabsoft.dreambot.util;

import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;
import static org.silabsoft.dreambot.util.Constants.*;
import static org.silabsoft.dreambot.util.Navigation.LumbridgeCastleSpinningWheelNavigationOptions.GO_TO_BANK_FROM_WHEEL;
import static org.silabsoft.dreambot.util.Navigation.LumbridgeCastleSpinningWheelNavigationOptions.GO_TO_WHEEL_FROM_BANK;
import static org.silabsoft.dreambot.util.Navigation.LumbridgeCastleSpinningWheelNavigationOptions.GO_TO_WHEEL_FROM_ENTRANCE;

/**
 * Navigations that can be used for more than one quest or task
 *
 * @author Silabsoft
 */
public class Navigation extends CommonBotMethods {

    /**
     * Freds house is used for the sheep shearer quest but also is a location to
     * get a free set of shears.
     *
     * @param script
     * @param isEntering - true for entering the house, false for exiting.
     * @return
     */
    public static boolean navigateFredsHouse(AbstractScript script, boolean isEntering) {
        GameObject gate = script.getGameObjects().getTopObjectOnTile(FARMER_FREDS_GATE_TILE);
        GameObject door = script.getGameObjects().getTopObjectOnTile(FARMER_FREDS_DOOR_TILE);
        GameObject[] checkOrder = isEntering ? new GameObject[]{gate, door} : new GameObject[]{door, gate};
        for (GameObject object : checkOrder) {
            if (object != null &&  object.hasAction("Open")) {
                if (!isEntityInView(script.getCamera(), object)) {
                    return false;
                }
                object.interact("Open");
                return false;
            }
        }
        Area area = isEntering ? FARMER_FREDS_HOUSE : FARMER_FREDS_GATE;
        if (area.contains(script.getLocalPlayer().getTile())) {
            return true;
        }
        script.getWalking().walk(area.getRandomTile());
        return false;
    }

    /**
     * Navigates Lumbridge castle bank cleaner than the walking api.
     *
     *
     * @param script
     * @param isEntering - true for entering, false for exiting.
     * @return
     */
    public static boolean navigateLumbridgeCastleBank(AbstractScript script, boolean isEntering) {
        Player player = script.getLocalPlayer();
        if (player.getTile().getZ() == 0 && isEntering) {
            GameObject staircase = script.getGameObjects().getTopObjectOnTile(LUMBRIDGE_CASTLE_FIRST_FLOOR_SOUTH_STAIRCASE_TILE);
            if (staircase == null) {
                return false;
            }
            if (!LUMBRIDGE_CASTLE_FIRST_FLOOR_SOUTH_STAIRCASE_STAGING_AREA.contains(player.getTile())) {
                script.getWalking().walk(LUMBRIDGE_CASTLE_FIRST_FLOOR_SOUTH_STAIRCASE_STAGING_AREA.getRandomTile());
                return false;
            }
            if (!isEntityInView(script.getCamera(), staircase)) {
                return false;
            }
            staircase.interact("Climb-up");
            return false;
        }
        if (player.getTile().getZ() == 1) {
            GameObject staircase = script.getGameObjects().getTopObjectOnTile(LUMBRIDGE_CASTLE_SECOND_FLOOR_SOUTH_STAIRCASE_TILE);
            if (player.getTile().distance(staircase) > 2) {
                script.getWalking().walk(staircase);
                return false;
            }
            if (!isEntityInView(script.getCamera(), staircase)) {
                return false;
            }
            staircase.interact(isEntering ? "Climb-up" : "Climb-down");
            return false;
        }
        if (player.getTile().getZ() == 2 && !isEntering) {
            GameObject staircase = script.getGameObjects().getTopObjectOnTile(LUMBRIDGE_CASTLE_THIRD_FLOOR_SOUTH_STAIRCASE_TILE);
            if (player.getTile().distance(staircase) > 2) {
                script.getWalking().walk(staircase);
                return false;
            }
            if (!isEntityInView(script.getCamera(), staircase)) {
                return false;
            }
            staircase.interact("Climb-down");
            return false;
        }

        Area area = !isEntering ? BankLocation.LUMBRIDGE.getArea(4) : LUMBRIDGE_CASTLE_ENTRANCE_AREA;
        if (area.contains(player.getTile())) {
            return true;
        }
        script.getWalking().walk(area.getRandomTile());
        return false;
    }

    public static boolean navigateSheepPen(AbstractScript script, boolean isEntering) {
        GameObject object = script.getGameObjects().getTopObjectOnTile(LUMBRIDGE_SHEEP_PEN_GATE_TILE);
        if (object != null && object.hasAction("Open")) {
            if (!isEntityInView(script.getCamera(), object)) {
                return false;
            }
            object.interact("Open");
            return false;
        }
        Area area = isEntering ? LUMBRIDGE_SHEEP_PEN : LUMBRIDGE_SHEEP_PEN_ENTRANCE;
        if (area.contains(script.getLocalPlayer().getTile())) {
            return true;
        }
        script.getWalking().walk(area.getRandomTile());
        return false;
    }

    public enum LumbridgeCastleSpinningWheelNavigationOptions {
        GO_TO_WHEEL_FROM_ENTRANCE,
        GO_TO_WHEEL_FROM_BANK,
        GO_TO_ENTRANCE_FROM_WHEEL,
        GO_TO_BANK_FROM_WHEEL 
      
    }

    public static boolean navigateLumbridgeCastleSpinningWheel(AbstractScript script, LumbridgeCastleSpinningWheelNavigationOptions option) {
        Player player = script.getLocalPlayer();
        if (player.getTile().getZ() == 0 && option == GO_TO_WHEEL_FROM_ENTRANCE) {
            GameObject staircase = script.getGameObjects().getTopObjectOnTile(LUMBRIDGE_CASTLE_FIRST_FLOOR_SOUTH_STAIRCASE_TILE);
            if (staircase == null) {
                return false;
            }
            if (!LUMBRIDGE_CASTLE_FIRST_FLOOR_SOUTH_STAIRCASE_STAGING_AREA.contains(player.getTile())) {
                script.getWalking().walk(LUMBRIDGE_CASTLE_FIRST_FLOOR_SOUTH_STAIRCASE_STAGING_AREA.getRandomTile());
                return false;
            }
            if (!isEntityInView(script.getCamera(), staircase)) {
                return false;
            }
            staircase.interact("Climb-up");
            return false;
        }
        if (player.getTile().getZ() == 1) {
            GameObject spinningWheel = script.getGameObjects().closest("Spinning Wheel");
            GameObject door = script.getGameObjects().getTopObjectOnTile(LUMBRIDGE_CASTLE_SPINNING_WHEEL_DOOR_TILE);

            if (option == GO_TO_WHEEL_FROM_ENTRANCE || option == GO_TO_WHEEL_FROM_BANK) {
                if (player.getTile().distance(spinningWheel) < 2) {
                    return true;
                }
                if (door != null &&  door.hasAction("Open") && isEntityInView(script.getCamera(), door)) {
                    door.interact("open");
                }
                script.getWalking().walk(spinningWheel);
                return false;
            }
            GameObject staircase = script.getGameObjects().getTopObjectOnTile(LUMBRIDGE_CASTLE_SECOND_FLOOR_SOUTH_STAIRCASE_TILE);
            if (player.getTile().distance(staircase) > 2) {
                script.getWalking().walk(staircase);
                return false;
            }
            if (!isEntityInView(script.getCamera(), staircase)) {
                return false;
            }
            staircase.interact(option == GO_TO_BANK_FROM_WHEEL ? "Climb-up" : "Climb-down");
            return false;
        }
        if (player.getTile().getZ() == 2 && option != GO_TO_BANK_FROM_WHEEL) {
            GameObject staircase = script.getGameObjects().getTopObjectOnTile(LUMBRIDGE_CASTLE_THIRD_FLOOR_SOUTH_STAIRCASE_TILE);
            if (player.getTile().distance(staircase) > 2) {
                script.getWalking().walk(staircase);
                return false;
            }
            if (!isEntityInView(script.getCamera(), staircase)) {
                return false;
            }
            staircase.interact("Climb-down");
            return false;
        }
        Area area = option == GO_TO_BANK_FROM_WHEEL ? BankLocation.LUMBRIDGE.getArea(4) : LUMBRIDGE_CASTLE_ENTRANCE_AREA;
        if (area.contains(script.getLocalPlayer().getTile())) {
            return true;
        }
        script.getWalking().walk(area.getRandomTile());
        return false;
    }
}
