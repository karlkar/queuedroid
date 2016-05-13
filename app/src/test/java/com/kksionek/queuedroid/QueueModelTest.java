package com.kksionek.queuedroid;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class QueueModelTest {

    private QueueModel mModel;

    @Before
    public void setUp() {
        mModel = new QueueModel();
        mModel.addPlayer(new Player("Mietek"));
        mModel.addPlayer(new Player("Roman"));
    }

    @Test
    public void addPlayer() {
        assertEquals(mModel.getPlayersCount(), 2);
        mModel.addPlayer(new Player("Henryk"));
        assertEquals(mModel.getPlayersCount(), 3);
        mModel.addPlayer(new Player("Zbigniew"));
        assertEquals(mModel.getPlayersCount(), 4);
    }

    @Test
    public void nextTurn() {
        assertEquals("Mietek", mModel.getCurrentPlayer());
        mModel.nextTurn(1);
        assertEquals("Roman", mModel.getCurrentPlayer());
        mModel.nextTurn(1);
        assertEquals("Mietek", mModel.getCurrentPlayer());
        mModel.nextTurn(1);
        assertEquals("Roman", mModel.getCurrentPlayer());
    }

    @Test
    public void getPoints() {
        List<Integer> points = mModel.getPoints();
        assertEquals(points.size(), 2);
        assertEquals(0, (long)points.get(0));
        assertEquals(0, (long)points.get(1));
        mModel.nextTurn(10);
        points = mModel.getPoints();
        assertEquals(points.size(), 2);
        assertEquals(10, (long)points.get(0));
        assertEquals(0, (long)points.get(1));
        mModel.nextTurn(9);
        mModel.nextTurn(19);
        mModel.nextTurn(1);
        mModel.nextTurn(11);
        points = mModel.getPoints();
        assertEquals(points.size(), 2);
        assertEquals(40, (long)points.get(0));
        assertEquals(10, (long)points.get(1));
    }

    @Test
    public void nextGame() {
        mModel.nextTurn(9);
        mModel.nextTurn(19);
        mModel.nextTurn(1);
        mModel.nextTurn(11);
        mModel.newGame();
        assertEquals(0, (long)mModel.getPoints().get(0));
        assertEquals(0, (long)mModel.getPoints().get(1));
    }

    @Test
    public void resetScoreboard() {
        mModel.resetScoreboard();
        assertEquals(0, mModel.getPlayersCount());
    }
}