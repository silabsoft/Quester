/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silabsoft.dreambot.quest;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.Quest;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.wrappers.items.GroundItem;
import static org.silabsoft.dreambot.quest.QuestSolver.State.*;

/**
 *
 * @author Silabsoft
 */
public abstract class QuestSolver {

    protected final Area questStartArea;
    protected final Quest quest;
    private State currentState = START;
    public static final int QUEST_WIDGET = 274;

    /**
     * not currently implemented will be used for quest item purchasing option.
     */
    public static final Area GRAND_EXCHANGE = null;

    public QuestSolver(Quest quest, Area startArea) {
        this.quest = quest;
        this.questStartArea = startArea;

    }

    public enum State {

        /**
         * Preparing to start the quest
         */
        START,
        /**
         * solving the quest
         */
        QUESTING,
        /*
        Will be used for quests where we do not meet the level requirements to complete. 
         */
        TASKING,
        /**
         * finished the quest
         */
        END
    }

    /**
     *
     * @return true if the player has the requirements to start the quest
     */
    public abstract boolean meetsRequirements(AbstractScript script);

    /**
     * individual quest logic loop
     *
     * @param script
     */
    public abstract void questLoop(AbstractScript script);

    /**
     * set the quest logic step in the event the quest is already previously
     * started
     *
     * @param script
     */
    public abstract void setStep(AbstractScript script);

    /**
     *
     * @param script
     * @return
     */
    public int questSolverLoop(AbstractScript script) {

        switch (currentState) {
            case START:
                if (!script.getTabs().isOpen(Tab.QUEST)) {
                    script.getTabs().open(Tab.QUEST);
                    break;
                }
                if (!script.getQuests().isStarted(quest)) {
                    Tile current = script.getLocalPlayer().getTile();

                    if (!questStartArea.contains(current)) {
                        if (script.getWalking().getDestinationDistance() == -1) {
                            script.getWalking().walk(questStartArea.getRandomTile());
                        }
                    } else {
                        this.currentState = QUESTING;
                    }

                } else {

                    this.setStep(script);

                }
                break;
            case QUESTING:
                script.log(this.getCurrentQuestStep());
                this.questLoop(script);
                break;

        }
        return Calculations.random(1000, 2000);
    }

    protected void setQuestSolverState(State state) {
        currentState = state;
    }

    public void WalkToArea(Tile current, Area area, Walking walking) {
        if (!area.contains(current)) {

            if (walking.getDestinationDistance() == -1) {

                walking.walk(area.getRandomTile());
            }
        }

    }

    public void pickupGroundItem(AbstractScript script, Tile destination, int itemId) {

        Tile current = script.getLocalPlayer().getTile();
        //why is it not checking the Z when comparing the tiles?
        if (!current.equals(destination)) {
            if (script.getWalking().getDestinationDistance() == -1) {

                script.getWalking().walk(destination);

            }
            return;
        }

        GroundItem item = script.getGroundItems().closest(itemId);
        if (item != null) {
            item.interact("Take");
        }

    }

    public State getCurrentState() {
        return currentState;
    }

    public Quest getQuest() {
        return quest;
    }

    


    public abstract String getCurrentQuestStep();
}
