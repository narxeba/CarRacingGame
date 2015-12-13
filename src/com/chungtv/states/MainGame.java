package com.chungtv.states;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;


public class MainGame extends StateBasedGame {

    public MainGame(String name) {
        super(name);
        addState(new PlayState(0));
    }

    @Override
    public void initStatesList(GameContainer gameContainer) throws SlickException {
        getState(0).init(gameContainer, this);
    }

    public static void main(String[] args){
        try {
            AppGameContainer container = new AppGameContainer(new MainGame("Racing Game"), 800, 600, false);
            container.setShowFPS(true);
            container.setTargetFrameRate(60);
            container.setAlwaysRender(true);
            container.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }

}
