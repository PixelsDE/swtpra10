/*
 * Copyright Notice for Swtpra10
 * Copyright (c) at ThunderGames | SwtPra10 2021
 * File created on 18.11.21, 10:33 by Carina Latest changes made by Carina on 18.11.21, 09:41
 * All contents of "GameMasterClientThread" are protected by copyright. The copyright law, unless expressly indicated otherwise, is
 * at ThunderGames | SwtPra10. All rights reserved
 * Any type of duplication, distribution, rental, sale, award,
 * Public accessibility or other use
 * requires the express written consent of ThunderGames | SwtPra10.
 */

package de.thundergames.gameplay.ausrichter.networking;

import de.thundergames.gameplay.ausrichter.GameMasterClient;
import de.thundergames.gameplay.player.networking.Client;
import de.thundergames.gameplay.player.networking.ClientThread;
import java.io.IOException;
import java.net.Socket;
import org.jetbrains.annotations.NotNull;

public class GameMasterClientThread extends ClientThread {

  public GameMasterClientThread(@NotNull Socket socket, int id, Client client) throws IOException {
    super(socket, id, client);
  }

  public GameMasterClient getGameMasterClient() {
    return (GameMasterClient) client;
  }

  @Override
  public void disconnect() {
    super.disconnect();
  }
}
