package org.silabsoft.dreambot.quest.impl;

import java.util.ArrayList;
import java.util.List;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.Quest;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.silabsoft.dreambot.quest.QuestSolver;
import static org.silabsoft.dreambot.quest.QuestSolver.State.END;
import static org.silabsoft.dreambot.quest.impl.CookAssistant.QuestStep.*;
import static org.silabsoft.dreambot.quest.QuestSolver.State.QUESTING;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Silabsoft
 */
public class CookAssistant extends QuestSolver {

    private QuestStep currentStep = START_QUEST;
    public static final Area LUMBRIDGE_CASTLE_KITCHEN = new Area(3210, 3212, 3206, 3217, 0);

    public static final int COOKS_ASSISTANT_QUEST_PARENT_WIDGET = 275;
    public static final int BUCKET_OF_MILK = 1927;
    public static final int BUCKET = 1925;
    public static final int POT_OF_FLOUR = 1933;
    public static final int EGG = 1944;
    public static final int WHEAT = 1947;
    public static final int POT = 1931;
    public static final int[] REQUIRED_ITEMS_FOR_COMPLETION = new int[]{BUCKET_OF_MILK, POT_OF_FLOUR, EGG};
    public static final Tile BUCKET_PICKUP_TILE = new Tile(3216, 9624, 0);
    public static final Tile POT_PICKUP_TILE = new Tile(3209, 3213, 0);
    public static final Tile WHEAT_FIELD_GATE_TILE = new Tile(3162, 3290, 0);
    public static final Area LUMBRIDGE_EAST_FARM_CHICKENS = new Area(3228, 3298, 3233, 3295, 0);
    public static final Area LUMBRIDGE_EAST_COW_PEN = new Area(3252, 3283, 3258, 3275, 0);
    public static final Area LUMBRIDGE_WHEAT_FIELD = new Area(3154, 3303, 3162, 3296, 0);
    public static final Area LUMBRIDGE_MILL_CONTROL_AREA = new Area(3165, 3308, 3168, 3305, 2);
    public static final Area LUMBRIDGE_CASTLE_ENTRANCE_AREA = new Area(3213, 3217, 3216, 3213, 0);
    boolean hasCheckedBank = false;
    private int bankStep;
    private boolean hasEmptiedInventory;
    private boolean hasAlreadyRetrievedMilk;
    private boolean hasAlreadyRetrievedFlour;
    private boolean hasAlreadyRetrievedEgg;

    public CookAssistant() {
        super(Quest.COOKS_ASSISTANT, LUMBRIDGE_CASTLE_KITCHEN);

    }

    public enum QuestStep {
        START_QUEST,
        COOK_DIALOGUE_START_QUEST,
        CHECK_ITEMS,
        GATHER_TOOLS,
        PICK_UP_TOOLS,
        CHECK_BANK,
        TURN_IN_ITEMS,
        GATHER_EGG,
        GATHER_MILK,
        GATHER_FLOUR,
        GATHER_WHEAT,
        GRIND_WHEAT,
        /*
        The walking api can't handle going into lumbridge castle basement at this time. This will be a solution until this is resolved.
         */
        WALKING_BUG_FIX_TAKE_BUCKET,
        /*
        another issue with walking to tiles when on the 3rd floor of the castle pathfinding was not taking tile Z in consideration. 
         */
        RESOLVE_ITEM_PICK_UP_BANK_ISSUE,
        PULL_MILL_LEVER,
        PICKUP_FLOUR,
        PICKING_TOOL,
        FINISHED
    }

    @Override
    public boolean meetsRequirements(AbstractScript script) {
        return true;
    }

    @Override
    public void questLoop(AbstractScript script) {
        if (script.getLocalPlayer().isMoving()) {
            return;
        }
        switch (currentStep) {
            case START_QUEST:
                NPC cook = script.getNpcs().closest("Cook");
                if (cook == null) {
                    script.log("Something is seriously broken can't find the cook");
                    currentStep = FINISHED;
                    return;
                }
                if (!cook.isOnScreen()) {

                    script.getCamera().rotateToEntityEvent(cook);
                } else {

                    cook.interact("Talk-to");
                    this.currentStep = COOK_DIALOGUE_START_QUEST;
                }
                return;
            case COOK_DIALOGUE_START_QUEST:
                Dialogues dialogue = script.getDialogues();
                if (!dialogue.inDialogue()) {
                    currentStep = START_QUEST;
                    return;
                } else if (dialogue.canContinue()) {
                    dialogue.continueDialogue();
                } else {

                    dialogue.chooseOption(dialogue.getOptionIndexContaining("I know where to") > -1 ? 4 : 1);
                }
                return;
            case CHECK_ITEMS:
                if (!script.getTabs().isOpen(Tab.INVENTORY)) {
                    script.getTabs().open(Tab.INVENTORY);
                    break;
                }
                Inventory inventory = script.getInventory();
                if (inventory.containsAll(REQUIRED_ITEMS_FOR_COMPLETION)) {
                    this.currentStep = TURN_IN_ITEMS;
                    return;
                }

                if (!inventory.contains(BUCKET_OF_MILK) && !hasAlreadyRetrievedMilk || !inventory.contains(POT_OF_FLOUR) && !hasAlreadyRetrievedFlour || !inventory.contains(EGG) && !hasAlreadyRetrievedEgg) {
                    this.currentStep = hasCheckedBank ? GATHER_TOOLS : CHECK_BANK;
                    return;
                } else {
                    this.currentStep = TURN_IN_ITEMS;
                }
                break;
            case GATHER_TOOLS:
                inventory = script.getInventory();
                if (!inventory.contains(BUCKET_OF_MILK, BUCKET) && !hasAlreadyRetrievedMilk) {
                    currentStep = WALKING_BUG_FIX_TAKE_BUCKET;

                    return;
                }
                if (!inventory.contains(POT_OF_FLOUR, POT) && !hasAlreadyRetrievedFlour) {

                    pickupGroundItem(script, POT_PICKUP_TILE, POT);

                    return;
                }

                ArrayList<QuestStep> stepsToTake = new ArrayList<QuestStep>();
                if (inventory.contains(BUCKET) && !inventory.contains(BUCKET_OF_MILK) && !hasAlreadyRetrievedMilk) {
                    stepsToTake.add(GATHER_MILK);
                }
                if (inventory.contains(POT) && !inventory.contains(POT_OF_FLOUR) && !hasAlreadyRetrievedFlour) {
                    stepsToTake.add(GATHER_FLOUR);
                }
                if (!inventory.contains(EGG) && !hasAlreadyRetrievedEgg) {
                    stepsToTake.add(GATHER_EGG);
                }

                if (stepsToTake.isEmpty()) {
                    currentStep = CHECK_ITEMS;
                    return;
                }
                currentStep = stepsToTake.get(Calculations.getRandom().nextInt(stepsToTake.size()));
                return;
            case WALKING_BUG_FIX_TAKE_BUCKET:
                Tile current = script.getLocalPlayer().getTile();
                inventory = script.getInventory();
                if (current.getY() < 9000 && !inventory.contains(BUCKET)) {
                    GameObject trapDoor = script.getGameObjects().closest("Trapdoor");
                    if (trapDoor == null) {
                        this.WalkToArea(script.getLocalPlayer().getTile(), questStartArea, script.getWalking());

                        return;
                    }
                    if (!trapDoor.isOnScreen()) {

                        script.getCamera().rotateToEntityEvent(trapDoor);
                    }
                    trapDoor.interact("Climb-down");
                    return;
                }
                if (current.getY() > 9000 && !inventory.contains(BUCKET)) {
                    pickupGroundItem(script, BUCKET_PICKUP_TILE, BUCKET);
                }
                if (current.getY() > 9000 && inventory.contains(BUCKET)) {
                    GameObject ladder = script.getGameObjects().closest("Ladder");
                    if (!ladder.isOnScreen()) {

                        script.getCamera().rotateToEntityEvent(ladder);
                    }
                    ladder.interact("Climb-up");
                }
                if (current.getY() < 9000 && inventory.contains(BUCKET)) {
                    currentStep = GATHER_TOOLS;
                }
                return;

            case GATHER_EGG:
                inventory = script.getInventory();
                if (inventory.contains(EGG)) {
                    currentStep = CHECK_ITEMS;
                    return;
                }
                GroundItem item = script.getGroundItems().closest(EGG);

                if (!LUMBRIDGE_EAST_FARM_CHICKENS.contains(script.getLocalPlayer().getTile())) {
                    this.WalkToArea(script.getLocalPlayer().getTile(), LUMBRIDGE_EAST_FARM_CHICKENS, script.getWalking());
                    return;
                }
                if (item == null) {
                    return;
                }
                item.interact("Take");

                return;
            case GATHER_MILK:
                inventory = script.getInventory();
                if (inventory.contains(BUCKET_OF_MILK)) {
                    currentStep = CHECK_ITEMS;
                    return;
                }
                GameObject dairyCow = script.getGameObjects().closest("Dairy cow");

                if (!LUMBRIDGE_EAST_COW_PEN.contains(script.getLocalPlayer().getTile())) {

                    this.WalkToArea(script.getLocalPlayer().getTile(), LUMBRIDGE_EAST_COW_PEN, script.getWalking());
                    return;
                }
                if (dairyCow == null) {
                    return;
                }

                if (!dairyCow.isOnScreen()) {
                    script.getCamera().rotateToEntity(dairyCow);
                    return;
                }
                if (Calculations.random(1, 10) % 2 == 0) {
                    dairyCow.interact("Milk");
                } else {
                    script.getInventory().get(BUCKET).useOn(dairyCow);
                }
                return;
            case GATHER_FLOUR:
                inventory = script.getInventory();
                if (inventory.contains(POT_OF_FLOUR)) {
                    currentStep = CHECK_ITEMS;
                    return;
                }
                if (!inventory.contains(WHEAT)) {
                    currentStep = GATHER_WHEAT;
                    return;
                }
                currentStep = GRIND_WHEAT;
                return;
            case GRIND_WHEAT:
                GameObject hopper = script.getGameObjects().closest("Hopper");
                if (hopper == null) {
                    GameObject largeDoor = script.getGameObjects().closest("Large Door");
                    if (LUMBRIDGE_MILL_CONTROL_AREA.getRandomTile().distance(script.getLocalPlayer().getTile()) < 10 && largeDoor != null && largeDoor.hasAction("Open")) {
                        largeDoor.interact("Open");
                        return;
                    }
                    if (LUMBRIDGE_MILL_CONTROL_AREA.getRandomTile().distance(script.getLocalPlayer().getTile()) > 10) {

                        this.WalkToArea(script.getLocalPlayer().getTile(), LUMBRIDGE_MILL_CONTROL_AREA, script.getWalking());
                        return;
                    }
                    if (script.getLocalPlayer().getZ() != 2) {
                        GameObject ladder = script.getGameObjects().closest("Ladder");
                        if (ladder != null) {
                            ladder.interact("Climb-up");
                            return;
                        }
                    }
                }
                if (!hopper.isOnScreen()) {
                    script.getCamera().rotateToEntity(hopper);
                    return;
                }
                script.getInventory().get(WHEAT).useOn(hopper);
                currentStep = PULL_MILL_LEVER;
                return;
            case PULL_MILL_LEVER:
                GameObject hopperControls = script.getGameObjects().closest("Hopper controls");
                if (hopperControls == null) {
                    currentStep = GATHER_FLOUR;
                    return;
                }
                if (!hopperControls.isOnScreen()) {
                    script.getCamera().rotateToEntity(hopperControls);
                    return;
                }
                hopperControls.interact("Operate");
                currentStep = PICKUP_FLOUR;

                return;
            case PICKUP_FLOUR:
                if (script.getInventory().contains(POT_OF_FLOUR)) {
                    currentStep = CHECK_ITEMS;
                    return;
                }
                current = script.getLocalPlayer().getTile();
                if (current.getZ() != 0) {

                    GameObject ladder = script.getGameObjects().closest("Ladder");
                    if (ladder != null) {
                        ladder.interact("Climb-down");
                        return;
                    }

                }
                GameObject flourBin = script.getGameObjects().closest("Flour Bin");
                if (flourBin == null) {
                    currentStep = GATHER_FLOUR;
                    return;
                }

                if (!flourBin.isOnScreen()) {
                    script.getCamera().rotateToEntity(flourBin);
                    return;
                }
                if (Calculations.random(1, 10) % 2 == 0) {
                    flourBin.interact("Empty");
                } else {
                    script.getInventory().get(POT).useOn(flourBin);
                }
                return;
            case GATHER_WHEAT:
                inventory = script.getInventory();
                if (inventory.contains(WHEAT)) {
                    currentStep = GATHER_FLOUR;
                    return;
                }

                if (WHEAT_FIELD_GATE_TILE.distance(script.getLocalPlayer().getTile()) > 1) {
                    if (script.getWalking().getDestinationDistance() == -1) {

                        script.getWalking().walk(WHEAT_FIELD_GATE_TILE);
                        return;
                    }

                    return;
                }
                GameObject wheat = script.getGameObjects().closest("Wheat");
                if (wheat == null) {
                    return;
                }

                //will hang on entering field without specifying the gate directly
                GameObject[] gate = script.getGameObjects().getObjectsOnTile(WHEAT_FIELD_GATE_TILE);
                if (gate != null && gate[0].hasAction("Open")) {
                    gate[0].interact("Open");
                    return;
                }

                if (!wheat.isOnScreen()) {
                    script.getCamera().rotateToEntity(wheat);
                    return;
                }

                wheat.interact("Pick");
                return;

            case TURN_IN_ITEMS:
                Widget questCompelte = script.getWidgets().getWidget(277);
                if (questCompelte != null && questCompelte.isVisible()) {
                    questCompelte.getChild(15).interact();
                    this.setQuestSolverState(END);
                    return;
                }
                cook = script.getNpcs().closest("Cook");
                if (!questStartArea.contains(script.getLocalPlayer().getTile())) {
                    this.WalkToArea(script.getLocalPlayer().getTile(), questStartArea, script.getWalking());

                    return;
                }
                if (!cook.isOnScreen()) {

                    script.getCamera().rotateToEntityEvent(cook);
                }
                if (!script.getDialogues().inDialogue()) {
                    cook.interact("Talk-to");
                }
                if (script.getDialogues().canContinue()) {
                    script.getDialogues().continueDialogue();
                }

                return;
            case CHECK_BANK:
                Bank bank = script.getBank();
                if (!bank.isOpen()) {
                    GameObject booth = script.getGameObjects().closest("Bank booth");
                    BankLocation bankLocation = BankLocation.LUMBRIDGE;
                    if (!bankLocation.getArea(5).contains(script.getLocalPlayer().getTile())) {

                        this.WalkToArea(script.getLocalPlayer().getTile(), bankLocation.getArea(5), script.getWalking());
                        return;
                    }
                    if (!booth.isOnScreen()) {
                        script.getCamera().rotateToEntityEvent(booth);
                        return;
                    }
                    booth.interact("Bank");
                    return;
                }

                if (!script.getInventory().isEmpty() && !hasEmptiedInventory) {
                    bank.depositAllItems();

                }
                if (!hasEmptiedInventory) {
                    hasEmptiedInventory = true;
                    return;
                }
                if (bankStep == 0 && !bank.withdraw(BUCKET_OF_MILK, 1)) {
                    bank.withdraw(BUCKET);
                }
                if (bankStep == 1 && !bank.withdraw(POT_OF_FLOUR, 1)) {
                    bank.withdraw(POT);
                }
                if (bankStep == 2) {
                    bank.withdraw(EGG, 1);

                }
                if (bankStep > 2) {
                    bank.close();
                    hasCheckedBank = true;
                    bankStep = 0;
                    currentStep = RESOLVE_ITEM_PICK_UP_BANK_ISSUE;
                    break;
                }
                bankStep++;
                break;
            case RESOLVE_ITEM_PICK_UP_BANK_ISSUE:
                if (!LUMBRIDGE_CASTLE_ENTRANCE_AREA.contains(script.getLocalPlayer().getTile())) {
                    this.WalkToArea(script.getLocalPlayer().getTile(), LUMBRIDGE_CASTLE_ENTRANCE_AREA, script.getWalking());

                    return;
                }
                currentStep = CHECK_ITEMS;
                return;

        }

    }

    @Override
    public void setStep(AbstractScript script) {
        //This part really isn't nessesary for this particular quest as it only requires one step however I wrote it just to get myself familiar with the api. I it will be used when you have you start a quest thats partially completed.

        Widgets widgets = script.getWidgets();

        if (widgets.getWidget(COOKS_ASSISTANT_QUEST_PARENT_WIDGET) == null || !widgets.getWidget(COOKS_ASSISTANT_QUEST_PARENT_WIDGET).isVisible()) {
            if (!script.getTabs().isOpen(Tab.QUEST)) {
                script.getTabs().open(Tab.QUEST);
                return;
            }
            quest.getWidgetChild(script.getClient()).interact();
            return;
        }
        Widget w = widgets.getWidget(COOKS_ASSISTANT_QUEST_PARENT_WIDGET);
        if (w != null && w.isVisible()) {
            script.log(w.getChild(7).getText());
            if (w.getChild(7).getText().contains("<str>")) {
                hasAlreadyRetrievedMilk = true;
            }
            if (w.getChild(8).getText().contains("<str>")) {
                hasAlreadyRetrievedFlour = true;
            }
            if (w.getChild(9).getText().contains("<str>")) {
                hasAlreadyRetrievedEgg = true;
            }
            this.currentStep = CHECK_ITEMS;
            this.setQuestSolverState(QUESTING);
            w.close();

        }
    }

    @Override
    public String getCurrentQuestStep() {
        return currentStep.toString();
    }

    @Override
    public String toString() {
        return "Cook's Assistant";
    }

}
