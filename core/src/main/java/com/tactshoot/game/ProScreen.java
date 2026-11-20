package com.tactshoot.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/** "TactShoot Pro" purchase / restore screen. */
public class ProScreen extends ScreenAdapter {

    private final TactShootGame game;
    private final Assets assets;
    private final Entitlements ent;
    private final java.util.function.Supplier<Screen> back;

    private Stage stage;
    private Label status;
    private String statusText = "";
    private boolean disposed;

    private final Entitlements.Listener listener = new Entitlements.Listener() {
        @Override
        public void onChanged(boolean pro, String message) {
            final String text = message == null ? "" : message;
            // Deferred so the stage is never rebuilt from inside one of its own click handlers.
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    if (disposed) return;
                    statusText = text;
                    rebuild();
                }
            });
        }
    };

    public ProScreen(TactShootGame game, java.util.function.Supplier<Screen> back) {
        this.game = game;
        this.assets = game.assets;
        this.ent = game.entitlements;
        this.back = back;
        stage = new Stage(new ScreenViewport());
        buildUi();
    }

    private void rebuild() {
        stage.clear();
        buildUi();
        Gdx.input.setInputProcessor(stage);
    }

    private void buildUi() {
        final float us = assets.uiScale;
        final boolean pro = ent.isPro();

        Table root = new Table();
        root.setFillParent(true);
        Table panel = new Table();
        panel.setBackground(assets.skin.getDrawable("panel"));
        panel.pad(30 * us);

        Label title = new Label("TACTSHOOT PRO", assets.skin, "default");
        title.setFontScale(1.9f);
        title.setColor(Palette.ACCENT);
        panel.add(title).padBottom(6 * us).row();
        Label sub = new Label(pro ? "UNLOCKED - THANK YOU FOR SUPPORTING THE GAME!"
                : "ONE-TIME PURCHASE - NO SUBSCRIPTION", assets.skin, "small");
        panel.add(sub).padBottom(14 * us).row();

        String[] perks = {
                "All 3 maps in Practice: Dustyard, Facility, Arena",
                "All primary weapons in Practice: Assault Rifle, SMG, Shotgun",
                "Supports further development: new maps and weapons",
                "Online play is always free and fair - Pro gives no online advantage",
        };
        for (String perk : perks) {
            Label l = new Label("+  " + perk, assets.skin, "default");
            l.setWrap(true);
            panel.add(l).width(560 * us).left().padBottom(6 * us).row();
        }

        status = new Label(statusText, assets.skin, "accent");
        status.setWrap(true);
        panel.add(status).width(560 * us).padTop(8 * us).padBottom(8 * us).row();

        if (!pro) {
            TextButton buy = new TextButton("BUY PRO", assets.skin, "accent");
            buy.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    assets.play(Assets.S_CLICK);
                    statusText = "Opening Cafe Bazaar...";
                    status.setText(statusText);
                    ent.buy(listener);
                }
            });
            panel.add(buy).width(420 * us).height(70 * us).pad(4).row();

            TextButton restore = new TextButton("RESTORE PURCHASE", assets.skin);
            restore.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    assets.play(Assets.S_CLICK);
                    statusText = "Checking with Cafe Bazaar...";
                    status.setText(statusText);
                    ent.refresh(listener);
                }
            });
            panel.add(restore).width(420 * us).height(56 * us).pad(4).row();
        }

        TextButton backBtn = new TextButton("BACK", assets.skin);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                assets.play(Assets.S_CLICK);
                game.setScreen(back.get());
            }
        });
        panel.add(backBtn).width(420 * us).height(56 * us).pad(4);

        ScrollPane scroll = new ScrollPane(panel, assets.skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        root.add(scroll).expand().fill().pad(8 * us);
        stage.addActor(root);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(Palette.BG.r, Palette.BG.g, Palette.BG.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        disposed = true;
        stage.dispose();
    }
}
