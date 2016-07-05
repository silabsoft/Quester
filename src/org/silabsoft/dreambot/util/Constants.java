/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silabsoft.dreambot.util;

import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;

/**
 *
 * @author Silabsoft
 */
public class Constants {

    //Area
    public static final Area FARMER_FREDS_GATE = new Area(3191, 3280, 3185, 3283, 0);
    public static final Area FARMER_FREDS_HOUSE = new Area(3191, 3274, 3188, 3272, 0);

    public static final Area LUMBRIDGE_SHEEP_PEN_ENTRANCE = new Area(3213, 3260, 3215, 3263, 0);
    public static final Area LUMBRIDGE_SHEEP_PEN = new Area(3212, 3258, 3195, 3274, 0);
    public static final Area LUMBRIDGE_CASTLE_ENTRANCE_AREA = new Area(3213, 3217, 3216, 3213, 0);
    public static final Area LUMBRIDGE_CASTLE_FIRST_FLOOR_SOUTH_STAIRCASE_STAGING_AREA = new Area(3206,3209,3208,3210,0);
    //Tiles
    public static final Tile FARMER_FREDS_GATE_TILE = new Tile(3188, 3279, 0);
    public static final Tile FARMER_FREDS_DOOR_TILE = new Tile(3189, 3275, 0);

    public static final Tile LUMBRIDGE_CASTLE_FIRST_FLOOR_SOUTH_STAIRCASE_TILE = new Tile(3204, 3207, 0);
    public static final Tile LUMBRIDGE_CASTLE_SECOND_FLOOR_SOUTH_STAIRCASE_TILE = new Tile(3204, 3207, 1);
    public static final Tile LUMBRIDGE_CASTLE_THIRD_FLOOR_SOUTH_STAIRCASE_TILE = new Tile(3204, 3207, 1);
    public static final Tile LUMBRIDGE_SHEEP_PEN_GATE_TILE = new Tile(3213, 3262, 0);
    public static final Tile LUMBRIDGE_CASTLE_SPINNING_WHEEL_DOOR_TILE = new Tile(3208, 3214, 1);
    //npcs
    public static final int FRED_THE_FARMER = 732;
    public static final int SHOP_KEEPER = 506;
    public static final int SHOP_ASSISTANT = 507;

    //items
    public static final int SHEARS = 1735;
    public static final int BALL_OF_WOOL = 1759;
    public static final int WOOL = 1737;
}
