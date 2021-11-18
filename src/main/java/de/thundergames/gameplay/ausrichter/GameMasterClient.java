/*
 * Copyright Notice for Swtpra10
 * Copyright (c) at ThunderGames | SwtPra10 2021
 * File created on 18.11.21, 10:33 by Carina Latest changes made by Carina on 18.11.21, 09:41
 * All contents of "GameMasterClient" are protected by copyright. The copyright law, unless expressly indicated otherwise, is
 * at ThunderGames | SwtPra10. All rights reserved
 * Any type of duplication, distribution, rental, sale, award,
 * Public accessibility or other use
 * requires the express written consent of ThunderGames | SwtPra10.
 */

package de.thundergames.gameplay.ausrichter;

import de.thundergames.gameplay.ausrichter.networking.GameMasterClientThread;
import de.thundergames.gameplay.ausrichter.networking.GameMasterPacketHandler;
import de.thundergames.gameplay.player.networking.Client;
import java.io.IOException;
import java.net.Socket;
import org.jetbrains.annotations.NotNull;

public class GameMasterClient extends Client {

  private static GameMasterClient clientInstance;
  private static int gamesID = 0;

  public GameMasterClient(int port, @NotNull String ip) {
    super(port, ip, "Ausrichter");
    clientPacketHandler = new GameMasterPacketHandler();
  }

  public static GameMasterClient getClientInstance() {
    return clientInstance;
  }

  public static void setClientInstance(GameMasterClient clientInstance) {
    GameMasterClient.clientInstance = clientInstance;
  }

  @Override
  public void connect() {
    try {
      socket = new Socket(ip, port);
      clientThread = new GameMasterClientThread(socket, 0, this);
      clientThread.start();
      clientPacketHandler.loginPacket(clientThread, getName());
    } catch (IOException exception) {
      System.out.println("Is the server running?!");
    }
  }

  public int getGameID() {
    return gamesID;
  }

  public void setSystemGameID(int gameID) {
    GameMasterClient.gamesID = gameID;
  }

  public GameMasterClientThread getMasterClientThread() {
    return (GameMasterClientThread) clientThread;
  }

  public GameMasterPacketHandler getClientMasterPacketHandler() {
    return (GameMasterPacketHandler) clientPacketHandler;
  }
}
