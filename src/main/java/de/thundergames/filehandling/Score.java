/*
 * Copyright Notice for Swtpra10
 * Copyright (c) at ThunderGames | SwtPra10 2021
 * File created on 02.12.21, 15:53 by Carina latest changes made by Carina on 02.12.21, 15:53
 * All contents of "Score" are protected by copyright. The copyright law, unless expressly indicated otherwise, is
 * at ThunderGames | SwtPra10. All rights reserved
 * Any type of duplication, distribution, rental, sale, award,
 * Public accessibility or other use
 * requires the express written consent of ThunderGames | SwtPra10.
 */
package de.thundergames.filehandling;

import de.thundergames.networking.util.interfaceitems.NetworkPlayer;

import java.util.HashMap;
import java.util.HashSet;

public class Score {

  private final HashSet<NetworkPlayer> players = new HashSet<>();
  private final HashMap<Integer, Integer> points = new HashMap<>();
  private final HashSet<NetworkPlayer> winner = new HashSet<>();

  public HashSet<NetworkPlayer> getPlayers() {
    return players;
  }

  public HashMap<Integer, Integer> getPoints() {
    return points;
  }

  public HashSet<NetworkPlayer> getWinners() {
    return winner;
  }
}
