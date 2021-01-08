package com.kksionek.queuedroid;

import com.kksionek.queuedroid.data.Player;
import com.kksionek.queuedroid.model.QueueModel;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class QueueModelTest {

    private QueueModel mModel;

    @Before
    public void setUp() {
        mModel = new QueueModel();
        ArrayList<Player> list = new ArrayList<>();
        list.add(new Player("1", "Mietek", null, Player.Type.CONTACTS));
        list.add(new Player("2", "Roman", null, Player.Type.CONTACTS));
        mModel.newGame(list);
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
        List<Integer> points = mModel.getPointList();
        assertEquals(points.size(), 2);
        assertEquals(0, (long)points.get(0));
        assertEquals(0, (long)points.get(1));
        mModel.nextTurn(10);
        points = mModel.getPointList();
        assertEquals(points.size(), 2);
        assertEquals(10, (long)points.get(0));
        assertEquals(0, (long)points.get(1));
        mModel.nextTurn(9);
        mModel.nextTurn(19);
        mModel.nextTurn(1);
        mModel.nextTurn(11);
        points = mModel.getPointList();
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
        assertEquals(0, (long)mModel.getPointList().get(0));
        assertEquals(0, (long)mModel.getPointList().get(1));
    }

    @Test
    public void resetScoreboard() {
        mModel.resetScoreboard();
        assertEquals(0, mModel.getPlayersCount());
    }
}