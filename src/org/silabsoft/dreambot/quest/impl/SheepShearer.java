/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silabsoft.dreambot.quest.impl;

import java.util.List;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.quest.Quest;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.silabsoft.dreambot.quest.QuestSolver;
import static org.silabsoft.dreambot.quest.QuestSolver.State.QUESTING;
import static org.silabsoft.dreambot.quest.impl.SheepShearer.QuestStep.*;
import static org.silabsoft.dreambot.util.Constants.*;
import org.silabsoft.dreambot.util.Navigation;
import static org.silabsoft.dreambot.util.Navigation.LumbridgeCastleSpinningWheelNavigationOptions.GO_TO_ENTRANCE_FROM_WHEEL;

/**
 *
 * @author Silabsoft
 */
public class SheepShearer extends QuestSolver {

    private QuestStep currentStep;
    private static final String[] TALK_OPTIONS = new String[]{"looking for a quest", "Yes okay.", "Of course!", "expert", "I'm back!"};
    private int ballsOfWoolRequired = 20;
    private boolean spinning;
    private int spinErrorCheck;
    private int spinErrorCheckLast;

    public enum QuestStep {
        CHECK_INVENTORY,
        WALK_TO_FREDS_GATE,
        WALK_TO_LUMBRIDGE_CASTLE,
        FREDS_HOUSE,
        TALK_WITH_FRED,
        TAKE_SHEARS,
        EXIT_FREDS_HOUSE,
        WALK_TO_BANK,
        WALK_TO_SPINNING_WHEEL,
        BANKING,
        EXIT_BANK,
        SPINNING,
        EXIT_SPINNING,
        WALK_TO_SHEEP_PEN,
        ENTER_SHEEP_PEN,
        SHEAR_SHEEP,
        EXIT_SHEEP_PEN,

    }

    public SheepShearer() {
        super(Quest.SHEEP_SHEARER, FARMER_FREDS_GATE);
        currentStep = CHECK_INVENTORY;
    }

    @Override
    public boolean meetsRequirements(AbstractScript script) {
        return true;
    }

    @Override
    public void questLoop(AbstractScript script) {
        if (script.getLocalPlayer().isMoving() || script.getLocalPlayer().isAnimating()) {
            return;
        }
        switch (currentStep) {
            case CHECK_INVENTORY:

                if (!script.getQuests().isStarted(quest)) {
                    currentStep = WALK_TO_FREDS_GATE;
                    return;
                }
                if (!script.getTabs().isOpen(Tab.INVENTORY)) {
                    script.getTabs().open(Tab.INVENTORY);
                    return;
                }
                if (script.getInventory().count(BALL_OF_WOOL) >= ballsOfWoolRequired) {
                    currentStep = WALK_TO_FREDS_GATE;
                    return;
                }
                int spaceRequired = script.getInventory().contains(SHEARS) ? ballsOfWoolRequired : ballsOfWoolRequired + 1;
                if (script.getInventory().emptySlotCount() < spaceRequired || script.getInventory().count(WOOL) >= ballsOfWoolRequired) {
                    currentStep = WALK_TO_LUMBRIDGE_CASTLE;
                    return;
                }

                if (!script.getInventory().contains(SHEARS)) {
                    currentStep = WALK_TO_FREDS_GATE;
                    return;
                }
                currentStep = WALK_TO_SHEEP_PEN;
                return;
            case WALK_TO_SHEEP_PEN:
                if (LUMBRIDGE_SHEEP_PEN_ENTRANCE.contains(script.getLocalPlayer().getTile())) {
                    currentStep = ENTER_SHEEP_PEN;
                }
                script.getWalking().walk(LUMBRIDGE_SHEEP_PEN_ENTRANCE.getRandomTile());
                return;
            case ENTER_SHEEP_PEN:
                if (Navigation.navigateSheepPen(script, true)) {
                    currentStep = SHEAR_SHEEP;
                }
                return;
            case EXIT_SHEEP_PEN:
                if (Navigation.navigateSheepPen(script, false)) {
                    currentStep = CHECK_INVENTORY;
                }
                return;
            case SHEAR_SHEEP:
                if (script.getInventory().count(WOOL) >= ballsOfWoolRequired) {
                    currentStep = EXIT_SHEEP_PEN;
                }
                List<NPC> sheep = script.getNpcs().all("Sheep");

                for (NPC npc : sheep) {
                    if (LUMBRIDGE_SHEEP_PEN.contains(npc) && !npc.hasAction("Talk-to") && npc.hasAction("Shear")) {
                        if (npc.isOnScreen()) {
                            npc.interact("Shear");
                            return;
                        }
                    }
                }
                script.getWalking().walk(LUMBRIDGE_SHEEP_PEN.getRandomTile());
                return;

            case WALK_TO_FREDS_GATE:
                if (FARMER_FREDS_HOUSE.contains(script.getLocalPlayer().getTile())) {
                    currentStep = FREDS_HOUSE;
                    return;
                }
                if (FARMER_FREDS_GATE.contains(script.getLocalPlayer().getTile())) {
                    currentStep = FREDS_HOUSE;
                    return;
                }
                script.getWalking().walk(FARMER_FREDS_GATE.getRandomTile());
                return;
            case FREDS_HOUSE:
                if (Navigation.navigateFredsHouse(script, true)) {

                    if (!script.getQuests().isStarted(quest)) {
                        currentStep = TALK_WITH_FRED;
                        return;
                    }
                    if (script.getInventory().count(BALL_OF_WOOL) >= ballsOfWoolRequired) {
                        currentStep = TALK_WITH_FRED;
                        return;
                    }
                    if (!script.getInventory().contains(SHEARS)) {
                        currentStep = TAKE_SHEARS;
                        return;
                    }
                    currentStep = EXIT_FREDS_HOUSE;
                }
                return;
            case TALK_WITH_FRED:
                if (!script.getTabs().isOpen(Tab.QUEST)) {
                    script.getTabs().open(Tab.QUEST);
                    return;
                }
                if (script.getQuests().isFinished(quest)) {
                    this.setQuestSolverState(State.END);
                    return;
                }
                if (script.getQuests().isStarted(quest) && script.getInventory().count(BALL_OF_WOOL) < this.ballsOfWoolRequired) {
                    currentStep = FREDS_HOUSE;
                    return;
                }

                if (talkWithFred(script)) {

                    currentStep = FREDS_HOUSE;

                }
                return;
            case TAKE_SHEARS:
                if (!Navigation.navigateFredsHouse(script, true)) {
                    return;
                }
                if (!script.getTabs().isOpen(Tab.INVENTORY)) {
                    script.getTabs().open(Tab.INVENTORY);
                    return;
                }
                if (!script.getInventory().contains(SHEARS)) {

                    GroundItem i = script.getGroundItems().closest(SHEARS);
                    if (i != null && isEntityInView(script.getCamera(), i)) {
                        i.interact("Take");
                    }
                    return;
                }
                currentStep = FREDS_HOUSE;
                return;

            case EXIT_FREDS_HOUSE:
                if (!Navigation.navigateFredsHouse(script, false)) {
                    return;
                }
                currentStep = CHECK_INVENTORY;
                return;
            case WALK_TO_LUMBRIDGE_CASTLE:
                if (LUMBRIDGE_CASTLE_ENTRANCE_AREA.contains(script.getLocalPlayer().getTile())) {

                    currentStep = script.getInventory().contains(WOOL) ? WALK_TO_SPINNING_WHEEL : WALK_TO_BANK;
                    return;
                }
                script.getWalking().walk(LUMBRIDGE_CASTLE_ENTRANCE_AREA.getRandomTile());
                return;
            case WALK_TO_BANK:
                if (Navigation.navigateLumbridgeCastleBank(script, true)) {
                    currentStep = BANKING;
                }
                return;
            case WALK_TO_SPINNING_WHEEL:
                if (Navigation.navigateLumbridgeCastleSpinningWheel(script, Navigation.LumbridgeCastleSpinningWheelNavigationOptions.GO_TO_WHEEL_FROM_ENTRANCE)) {
                    currentStep = SPINNING;
                }
                return;
            case BANKING:
                if (!script.getBank().isOpen()) {
                    GameObject bank = script.getGameObjects().closest("Bank booth");
                    if (isEntityInView(script.getCamera(), bank)) {
                        bank.interact("Bank");
                    }
                    return;
                }
                if (script.getInventory().getEmptySlots() < 27) {
                    script.getBank().depositAllItems();
                    return;
                }
                if (script.getInventory().contains(SHEARS)) {
                    currentStep = EXIT_BANK;
                    return;
                }
                if (script.getBank().contains(SHEARS)) {
                    script.getBank().withdraw(SHEARS, 1);
                    return;
                }
            case EXIT_BANK:
                if (Navigation.navigateLumbridgeCastleBank(script, false)) {
                    currentStep = CHECK_INVENTORY;
                }
                return;
            case SPINNING:
                Widget widget = script.getWidgets().getWidget(459);
                WidgetChild enterAmount = script.getWidgets().getWidget(162).getChild(32);
                if (!script.getInventory().contains(WOOL)) {
                    currentStep = EXIT_SPINNING;
                    return;
                }
                if (enterAmount != null && enterAmount.isVisible()) {

                    spinning = true;
                    script.getKeyboard().type(script.getInventory().count(WOOL));
                    return;
                }
                if (spinning && script.getInventory().contains(WOOL)) {
                    int currentWoolCount = script.getInventory().count(WOOL);
                    if (currentWoolCount == 0) {
                        spinErrorCheck = 0;
                        spinning = false;
                        return;
                    }
                    if (spinErrorCheckLast == currentWoolCount && spinErrorCheck++ > 10) {
                        spinning = false;
                    } else {
                        spinErrorCheck = 0;
                    }
                    return;
                }
                if (widget == null || !widget.isVisible()) {
                    GameObject bank = script.getGameObjects().closest("Spinning wheel");
                    if (isEntityInView(script.getCamera(), bank)) {
                        bank.interact("Spin");
                    }
                    return;
                }

                if (script.getInventory().contains(WOOL) && widget.isVisible()) {

                    widget.getChild(97).interact("Make X");
                    return;
                }
                currentStep = EXIT_SPINNING;
                return;
            case EXIT_SPINNING:
                if (Navigation.navigateLumbridgeCastleSpinningWheel(script, GO_TO_ENTRANCE_FROM_WHEEL)) {
                    currentStep = CHECK_INVENTORY;
                }
                return;
        }
    }

    @Override
    public void setStep(AbstractScript script
    ) {

        Widget w = this.openQuestWidget(script, 275);
        if (w != null && w.isVisible()) {
            String woolNeeded = w.getChild(7).getText();
            if (woolNeeded.contains("I have enough")) {
                ballsOfWoolRequired = 0;
            } else {
                try {
                    ballsOfWoolRequired = Integer.parseInt(woolNeeded.substring(woolNeeded.indexOf("collect ") + 8, woolNeeded.indexOf(" more")));
                    script.log("I need: " + ballsOfWoolRequired);
                } catch (Exception e) {
                    script.log("opps: " + e.getMessage());
                }
            }

            this.currentStep = CHECK_INVENTORY;
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
        return "Sheep Shearer";
    }

    private boolean talkWithFred(AbstractScript script) {
        Dialogues d = script.getDialogues();
        if (!d.inDialogue()) {
            NPC fred = script.getNpcs().closest(FRED_THE_FARMER);
            if (isEntityInView(script.getCamera(), fred)) {
                fred.interact("Talk-to");
                return false;
            }
            return false;
        }
        if (d.canContinue()) {
            d.continueDialogue();
            return false;
        } else {
            int index = -1;
            for (String option : TALK_OPTIONS) {
                index = d.getOptionIndexContaining(option);
                if (index > -1) {
                    break;
                }
            }
            d.chooseOption(index);
        }
        return false;
    }

    @Override
    public int[] getQuestListScrollBounds() {
        return new int[]{249, 264};
    }

}
