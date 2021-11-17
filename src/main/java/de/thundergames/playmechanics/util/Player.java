/*
 *
 *  *     / **
 *  *      *   Copyright Notice                                             *
 *  *      *   Copyright (c) SwtPra10 | ThunderGames 2021                         *
 *  *      *   Created: 05.05.2018 / 11:59                                  *
 *  *      *   All contents of this source text are protected by copyright. *
 *  *      *   The copyright law, unless expressly indicated otherwise, is  *
 *  *      *   at SwtPra10 | ThunderGames. All rights reserved                    *
 *  *      *   Any type of duplication, distribution, rental, sale, award,  *
 *  *      *   Public accessibility or other use                            *
 *  *      *   Requires the express written consent of SwtPra10 | ThunderGames.   *
 *  *      **
 *  *
 */
package de.thundergames.playmechanics.util;

import de.thundergames.MoleGames;
import de.thundergames.networking.server.ServerThread;
import de.thundergames.networking.util.Packet;
import de.thundergames.networking.util.Packets;
import de.thundergames.playmechanics.game.Game;
import de.thundergames.playmechanics.game.GameLogic;
import de.thundergames.playmechanics.map.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class Player {

  private final ArrayList<Mole> moles = new ArrayList<>();
  private final ServerThread serverClient;
  private final Game game;
  private final List<Integer> cards;
  private int drawCard = 0;
  private Timer timer;
  private boolean timerIsRunning = false;
  private boolean canDraw = false;
  private boolean hasMoved = true;
  private PlayerStates playerState;

  /**
   * @param client the serverClient connection established by the Server
   * @param game   the Game a player joined
   * @author Carina
   * @use will only be created on joining a Game
   * @see Game
   * @see ServerThread
   */
  public Player(@NotNull final ServerThread client, @NotNull final Game game) {
    this.serverClient = client;
    this.game = game;
    this.cards = new ArrayList<>(game.getSettings().getCards());
  }

  /**
   * @author Carina
   * @use will be called when a player wants to draw a card and it takes the current first card and puts it afterwards to the back of the list
   * @see Settings
   */
  public void nextCard() {
    drawCard = cards.get(0);
    var card = drawCard;
    cards.remove(0);
    cards.add(card);
  }

  /**
   * @author Carina
   * @use the player object that got created / instanciated after the constructor to get everything ready
   * @see Player
   */
  public synchronized Player create() {
    playerState = PlayerStates.JOIN;
    var moleIDs = new ArrayList<Integer>();
    for (int i = 0; i < game.getSettings().getMoleAmount(); i++) {
      var mole = new Mole(game.getMoleID(), this);
      moles.add(mole);
      game.getMoleMap().put(this, mole);
      moleIDs.add(game.getMoleID());
      game.getMoleIDMap().put(mole.getMoleID(), mole);
      game.setMoleID(game.getMoleID() + 1);
    }
    MoleGames.getMoleGames().getPacketHandler().sendMoleIDs(serverClient, moleIDs);
    return this;
  }

  /**
   * @author Carina
   * @use startes the time a player got for its move
   * @see Settings
   */
  public void startThinkTimer() {
    playerState = PlayerStates.MOVE;
    hasMoved = false;
    timer = new Timer();
    canDraw = true;
    timerIsRunning = true;
    timer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            canDraw = false;
            hasMoved = true;
            playerState = PlayerStates.WAIT;
            getServerClient().sendPacket(new Packet(new JSONObject().put("type", Packets.TURNOVER.getPacketType())));
            timerIsRunning = false;
            game.nextPlayer();
          }
        },
        game.getSettings().getTimeToThink() * 1000L);
  }

  /**
   * @return the canDraw
   * @author carina
   * @use when called draws a card and returns it depending on
   * @see Settings if the card should be taken in order or randomly if cards a empty refill by the oder
   */
  public void drawACard() {
    playerState = PlayerStates.DRAW;
    if (!game.getCurrentPlayer().equals(this) || hasMoved || !canDraw) {
      return;
    }
    if (!game.getSettings().isRandomDraw()) {
      nextCard();
    } else {
      drawCard = cards.get(0);
    }
    MoleGames.getMoleGames().getPacketHandler().drawnPlayerCardPacket(serverClient, drawCard);
  }

  /**
   * @param moleID  the mole that will be moved
   * @param x_start the x-coordinate of the start field
   * @param y_start the y-coordinate of the start field
   * @param x_end   the x-coordinate of the end field
   * @param y_end   the y-coordinate of the end field
   * @return true if the move is valid
   * @author Carina
   * @use will check if a field is valid and if the player has the right to move the mole
   * @see Mole
   * @see Player
   * @see Field
   * @see GameLogic
   */
  public void moveMole(
      final int moleID, final int x_start, final int y_start, final int x_end, final int y_end) {
    if (!game.getCurrentPlayer().equals(this) || hasMoved || getMole(moleID) == null) {
      return;
    }
    playerState = PlayerStates.MOVE;
    if (MoleGames.getMoleGames()
        .getGameLogic()
        .wasLegalMove(
            List.of(x_start, y_start),
            List.of(x_end, y_end),
            drawCard,
            game.getMap())) { // TODO: drawCard - 3
      Objects.requireNonNull(getMole(moleID)).setField(game.getMap().getFloor().getFieldMap().get(List.of(x_end, y_end)));
      game.getMap()
          .getFloor()
          .getOccupied()
          .remove(game.getMap().getFloor().getFieldMap().get(List.of(x_start, y_start)));
      game.getMap()
          .getFloor()
          .getOccupied()
          .add(game.getMap().getFloor().getFieldMap().get(List.of(x_end, y_end)));
      game.getMap()
          .getFloor()
          .getFieldMap()
          .get(List.of(x_start, y_start))
          .setOccupied(false, -1);
      game.getMap()
          .getFloor()
          .getFieldMap()
          .get(List.of(x_end, y_end))
          .setOccupied(true, moleID);
      MoleGames.getMoleGames()
          .getServer()
          .sendToAllGameClients(
              game,
              MoleGames.getMoleGames()
                  .getPacketHandler()
                  .playerMovesMolePacket(moleID, x_end, y_end));
      System.out.println(
          "Player with id: "
              + serverClient.getConnectionId()
              + " has moved his mole from: x="
              + x_start
              + " y="
              + y_start
              + " to x="
              + x_end
              + " y="
              + y_end
              + " with a card=" + drawCard + "." + "\n\n");
      canDraw = false;
      hasMoved = true;
      playerState = PlayerStates.WAIT;
      for (var connection : game.getAIs()) {
        game.getMap().sendMap(connection);
      }
      if (timerIsRunning) {
        timer.purge();
        timer.cancel();
        getServerClient().sendPacket(new Packet(new JSONObject().put("type", Packets.TURNOVER.getPacketType())));
        game.nextPlayer();
      }

    } else {
      System.out.println(
          "Client with id: "
              + serverClient.getConnectionId()
              + " has done in invalid move Punishment: "
              + game.getSettings().getPunishment() +
              " player tried to move from X,Y: [" + x_start + "," + y_start + "] to X,Y: [" + x_end + "," + y_end + "] with a card of " + drawCard + "\n\n");
      serverClient.sendPacket(MoleGames.getMoleGames().getPacketHandler().invalidMovePacket());
      timer.purge();
      timer.cancel();
      getServerClient().sendPacket(new Packet(new JSONObject().put("type", Packets.TURNOVER.getPacketType())));
      game.nextPlayer();

    }
  }

  /**
   * @param x      the x cordinate where a mole will be placed
   * @param y      the y cordinate where a mole will be placed
   * @param moleID the moleID that will be placed on the map
   * @author Carina
   * @use will check if a field is free than set the mole on this field
   * @see Mole
   * @see Player
   * @see Field
   */
  public void placeMole(final int x, final int y, final int moleID) {
    if (!game.getCurrentPlayer().equals(this) || hasMoved || getMole(moleID) == null) {
      return;
    }
    playerState = PlayerStates.MOVE;
    if (!game.getMap().getFloor().getFieldMap().containsKey(List.of(x, y))) {
      serverClient.sendPacket(
          new Packet(new JSONObject().put("type", Packets.OCCUPIED.getPacketType()).put("values", new JSONObject().put("moleID", moleID).toString())));
      return;
    }
    if (game.getMap().getFloor().getFieldMap().get(List.of(x, y)).isOccupied()
        || game.getMap().getFloor().getFieldMap().get(List.of(x, y)).isHole()) {
      serverClient.sendPacket(
          new Packet(new JSONObject().put("type", Packets.OCCUPIED.getPacketType()).put("values", new JSONObject().put("moleID", moleID).toString())));
    } else {
      Objects.requireNonNull(getMole(moleID))
          .setField(game.getMap().getFloor().getFieldMap().get(List.of(x, y)));
      game.getMap()
          .getFloor()
          .getOccupied()
          .add(game.getMap().getFloor().getFieldMap().get(List.of(x, y)));
      game.getMap().getFloor().getFieldMap().get(List.of(x, y)).setOccupied(true, moleID);
      MoleGames.getMoleGames()
          .getServer()
          .sendToAllGameClients(
              game,
              MoleGames.getMoleGames().getPacketHandler().playerPlacesMolePacket(moleID, x, y));
      game.getMap().printMap();
      canDraw = false;
      hasMoved = true;
      playerState = PlayerStates.WAIT;
      for (var connection : game.getAIs()) {
        game.getMap().sendMap(connection);
      }
      if (timerIsRunning) {
        timer.purge();
        timer.cancel();
        getServerClient().sendPacket(new Packet(new JSONObject().put("type", Packets.TURNOVER.getPacketType())));
        game.nextPlayer();
      }

      System.out.println(
          "Player with id: "
              + serverClient.getConnectionId()
              + " has placed his mole on x="
              + x
              + " y="
              + y
              + "." + "\n\n");
    }

  }

  /**
   * @param moleID the mole that will be gotten by its ID
   * @return the mole with the given ID
   * @author Carina
   * @see Mole
   */
  private Mole getMole(final int moleID) {
    Mole mole;
    for (Mole value : moles) {
      if (value.getMoleID() == moleID) {
        mole = value;
        return mole;
      }
    }
    return null;
  }

  public Game getGame() {
    return game;
  }

  public ArrayList<Mole> getMoles() {
    return moles;
  }

  public ServerThread getServerClient() {
    return serverClient;
  }
}
