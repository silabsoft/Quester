/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silabsoft.dreambot.util;

import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.wrappers.interactive.Entity;
import org.dreambot.api.wrappers.interactive.GameObject;

/**
 * common bot methods to use in my scripts or
 *
 * @author Silabsoft
 */
public class CommonBotMethods {

    /**
     *
     * @param camera
     * @param object
     * @return returns true when the entity is in view of the camera, otherwise
     * will adjust camera to be in view and return false.
     */
    public static boolean isEntityInView(Camera camera, Entity entity) {
        if (entity.isOnScreen()) {
            return true;
        }
        camera.rotateToEntityEvent(entity);
        return false;
    }

}
