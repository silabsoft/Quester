/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silabsoft.dreambot;

/**
 *
 * @author Silabsoft
 */
import java.util.List;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.Category;
import org.silabsoft.dreambot.quest.impl.CookAssistant;
import org.silabsoft.dreambot.quest.QuestSolver;
import static org.silabsoft.dreambot.quest.QuestSolver.State.END;
import org.silabsoft.dreambot.quest.impl.SheepShearer;

@ScriptManifest(author = "Silabsoft", name = "Quester", version = 1.0, description = "Runescape quests made easy", category = Category.QUEST)
public class Quester extends AbstractScript {
    
    private QuestSolver currentQuest = null;
    public final QuestSolver[] availableQuests = new QuestSolver[]{
        new CookAssistant(),
        new SheepShearer(),};
    private List<QuestSolver> questsToSolve;
    private QuestSettings setting;
    
    public void onStart() {
        log("A pile of Silab called Quester is starting.");
        setting = new QuestSettings(this);
        setting.setVisible(true);
        
    }
    
    private enum State {
        RUNNING,
        IDLE,
    };
    
    private State getState() {
        if (currentQuest != null) {
            return State.RUNNING;
        }
        return State.IDLE;
    }
    
    public void onExit() {
        
    }
    
    public void startQuesting(List<QuestSolver> solve) {
        questsToSolve = solve;
        
    }
    
    @Override
    public int onLoop() {
        
        if (currentQuest != null) {
            if (setting != null) {
                setting.getQuestStateLabel().setText(currentQuest.getCurrentState().toString());
                setting.getCurrentQuestLabel().setText(currentQuest.toString());
                setting.getCurrentStepLabel().setText(currentQuest.getCurrentQuestStep());
            }
            if (currentQuest.getCurrentState() == END) {
                if (setting != null) {
                    setting.getLog().append("Finished Quest: " + currentQuest.toString() + "\n");
                }
                currentQuest = null;
                return 0;
            }
            return currentQuest.questSolverLoop(this);
        } else if (setting != null) {
            setting.getCurrentQuestLabel().setText("");
            setting.getCurrentStepLabel().setText("");
        }
        if (questsToSolve == null) {
            return Calculations.random(1000, 1500);
        }
        if (questsToSolve.isEmpty() && setting != null && !setting.getStartButton().isEnabled()) {
            setting.getStartButton().setEnabled(true);
            setting.getStopButton().setEnabled(false);
        }
        if (!questsToSolve.isEmpty()) {
            if (!this.getTabs().isOpen(Tab.QUEST)) {
                this.getTabs().open(Tab.QUEST);
                return 1000;
            }
            QuestSolver q = questsToSolve.remove(0);
            
            if (this.getQuests().isFinished(q.getQuest())) {
                if (setting != null) {
                    setting.getLog().append("Finished Quest: " + q.toString() + "\n");
                }
                return 0;
            }
            if (q.meetsRequirements(this)) {
                setting.getLog().append("Starting Quest: " + q.toString() + "\n");
                currentQuest = q;
            }
        }
        return Calculations.random(1000, 1500);
    }
    
    public QuestSolver[] getAvailableQuests() {
        return availableQuests;
    }
    
    public QuestSolver getCurrentQuest() {
        return currentQuest;
    }
    
    public void setCurrentQuest(QuestSolver currentQuest) {
        this.currentQuest = currentQuest;
    }
    
    public List<QuestSolver> getQuestsToSolve() {
        return questsToSolve;
    }
    
    public void setQuestsToSolve(List<QuestSolver> questsToSolve) {
        this.questsToSolve = questsToSolve;
    }
    
}
