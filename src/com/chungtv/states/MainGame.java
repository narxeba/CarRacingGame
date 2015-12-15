package com.chungtv.states;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;


public class MainGame extends StateBasedGame {

    public MainGame(String name) {
        super(name);

    }

    @Override
    public void initStatesList(GameContainer gameContainer) throws SlickException {
        addState(new WaitingState(1));
        addState(new PlayState(2));
    }

    public static void main(String[] args){
        try {
            AppGameContainer container = new AppGameContainer(new MainGame("Racing Game"), 800, 600, false);
            container.setShowFPS(false);
            container.setTargetFrameRate(60);
            container.setAlwaysRender(true);
            container.setFullscreen(true);
            container.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }

}
