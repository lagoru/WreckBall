package com.wreckballs.framework;

public abstract class Screen {
    protected final GameInterface game;

    public Screen(GameInterface game) {
        this.game = game;
    }

    public abstract void update(float deltaTime);

    public abstract void paint(float deltaTime);

    public abstract void pause();

    public abstract void resume();

    public abstract void dispose();
   
    public abstract void backButton();
}
